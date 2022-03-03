package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.*;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.ActionDAO;
import com.uwallet.pay.main.dao.AdminDAO;
import com.uwallet.pay.main.dao.AdminRoleDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.Admin;
import com.uwallet.pay.main.model.entity.AdminRole;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * <p>
 * 管理员账户表
 * </p>
 *
 * @package: com.loancloud.rloan.main.admin.service.impl
 * @description: 管理员账户表
 * @author: liming
 * @date: Created in 2019-09-09 15:24:15
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: liming
 */
@Service
@Slf4j
public class AdminServiceImpl extends BaseServiceImpl implements AdminService {

    @Autowired
    private AdminDAO adminDAO;
    @Autowired
    private AdminRoleService adminRoleService;
    @Autowired
    private ActionDAO actionDAO;
    @Autowired
    private AdminRoleDAO adminRoleDAO;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private LoginMissService loginMissService;
    @Autowired
    @Lazy
    private UserService userService;
    @Autowired
    private RoleService roleService;

    @Value("${spring.login-lock-time}")
    private int loginLockTime;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdmin(@NonNull AdminDTO adminDTO, HttpServletRequest request) throws BizException {
        String password = adminDTO.getPassword();
        if (StringUtils.isBlank(password)) {
            throw new BizException(I18nUtils.get("user.rule.passwordIsNull", getLang(request)));
        }
        // 验证密码
        if (!Validator.isAdminPassword(adminDTO.getPassword())) {
            throw new BizException(I18nUtils.get("user.rule.password", getLang(request)));
        }
        this.checkNull(adminDTO, request);
        // 用户名唯一性验证
        adminDTO.setRealName(adminDTO.getRealName().trim());
        Map<String, Object> para = new HashMap<>(1);
        para.put("userName", adminDTO.getUserName());
        Admin adm = adminDAO.selectOne(para);
        if (adm != null) {
            throw new BizException(I18nUtils.get("user.rule.userNamePresence", getLang(request)));
        }
        Admin admin = BeanUtil.copyProperties(adminDTO, new Admin());
        admin.setState(1);
        admin.setLoginIp(getIp(request));
        admin.setLoginTime(System.currentTimeMillis());
        admin.setLoginTimes(0);
        admin.setParentId(0L);
        String passwordInputMd5 = DigestUtils.md5Hex(password);
        admin.setPassword(passwordInputMd5);
        log.info("save Admin:{}", admin);
        if (adminDAO.insert((Admin) this.packAddBaseProps(admin, request)) != 1) {
            log.error("insert error, data:{}", admin);
            throw new BizException("Insert admin Error!");
        }
        /*管理员添加 可以同时进行角色的添加，存入数据库
         (1)判断是否接收到角色，不可以为空
         (2)若角色不为空，则需要添加管理员角色关联表数据，若添加失败，则需要回滚事务，让管理员添加失败。
         */
        AdminRole adminRole;
        List<AdminRole> adminRoles = new ArrayList<>();
        for (Long roleId : adminDTO.getRoles()) {
            adminRole = new AdminRole();
            adminRole.setAdminId(admin.getId());
            adminRole.setRoleId(roleId);
            adminRoles.add((AdminRole) this.packAddBaseProps(adminRole, request));
        }
        adminRoleService.saveAdminRoleList(adminRoles);
    }

    private void checkNull(@NonNull AdminDTO adminDTO, HttpServletRequest request) throws BizException {
        if (StringUtils.isEmpty(adminDTO.getUserName())) {
            // 用户名不能为空
            throw new BizException(I18nUtils.get("user.rule.usernameIsNull", getLang(request)));
        }
        if (StringUtils.isEmpty(adminDTO.getRealName())) {
            // 人员姓名不能为空
            throw new BizException(I18nUtils.get("user.rule.realNameIsNull", getLang(request)));
        }
        if (adminDTO.getRoles() == null || adminDTO.getRoles().isEmpty()) {
            // 角色不可以为空
            throw new BizException(I18nUtils.get("user.rule.roleNameIsNull", getLang(request)));
        }
        // 验证用户名
        if (!Validator.isUserName(adminDTO.getUserName())) {
            throw new BizException(I18nUtils.get("user.rule.username", getLang(request)));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroupMembers(@NonNull AdminDTO adminDTO, @NonNull Long roleId, @NonNull HttpServletRequest request) throws BizException {
        // 校验当前管理员是否拥有该roleId角色
        Long userId = this.getUserId(request);
        Map<String, Object> params = new HashMap<>(1);
        params.put("adminId", userId);
        List<AdminRoleDTO> adminRoleDTOS = adminRoleService.find(params, null, null);
        boolean flag = true;
        for (AdminRoleDTO a : adminRoleDTOS) {
            if (roleId.equals(a.getRoleId())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            throw new BizException(I18nUtils.get("user.rule.roleIsNull", getLang(request)));
        }

        //用户名唯一性验证
        Map<String, Object> para = new HashMap<>(1);
        para.put("userName", adminDTO.getUserName());
        Admin adm = adminDAO.selectOne(para);
        if (adm != null) {
            throw new BizException(I18nUtils.get("user.rule.userNamePresence", getLang(request)));
        }
        Admin admin = BeanUtil.copyProperties(adminDTO, new Admin());
        admin.setPassword(DigestUtils.md5Hex(adminDTO.getPassword()));
        admin.setState(1);
        log.info("save Admin:{}", admin);
        admin = (Admin) this.packAddBaseProps(admin, request);
        AdminRoleDTO adminRoleDTO = new AdminRoleDTO();
        adminRoleDTO.setAdminId(admin.getId());
        // 组长与组员id关系
        adminRoleDTO.setRoleId(roleId * 10);
        if (adminDAO.insert(admin) != 1) {
            log.error("insert error, data:{}", admin);
            throw new BizException("Insert admin Error!");
        }
        adminRoleService.saveAdminRole(adminRoleDTO, request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupMembers(@NonNull Long id, @NonNull AdminDTO adminDTO, HttpServletRequest request) throws BizException {
        log.info("full update adminDTO:{}", adminDTO);
        if (StringUtils.isNotBlank(adminDTO.getPassword())) {
            String password = adminDTO.getPassword();
            String passwordInputMd5 = DigestUtils.md5Hex(password);
            adminDTO.setPassword(passwordInputMd5);
        }
        if (id.equals(adminDTO.getParentId())) {
            adminDTO.setParentId(0L);
        }
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AdminDTO oldAdminDTO = adminDAO.selectOneDTO(params);
        if (oldAdminDTO == null) {
            log.error("updateUser param error, id:{}", id);
            throw new BizException("param error!");
        }
        this.update(id, adminDTO, request);
    }

    @Override
    @Transactional(rollbackFor = BizException.class)
    public void saveAdminList(@NonNull List<Admin> adminList) throws BizException {
        if (adminList.size() == 0) {
            throw new BizException("参数长度不能为0");
        }
        int rows = adminDAO.insertList(adminList);
        if (rows != adminList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, adminList.size());
            throw new BizException("批量保存异常");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdmin(@NonNull Long id, @NonNull AdminDTO adminDTO, HttpServletRequest request) throws BizException {
        log.info("full update adminDTO:{}", adminDTO);
        checkNull(adminDTO, request);
        if (StringUtils.isNotBlank(adminDTO.getPassword())) {
            // 验证密码
            if (!Validator.isAdminPassword(adminDTO.getPassword())) {
                throw new BizException(I18nUtils.get("user.rule.password", getLang(request)));
            }
            String password = adminDTO.getPassword();
            String passwordInputMd5 = DigestUtils.md5Hex(password);
            adminDTO.setPassword(passwordInputMd5);
        }

        // 去掉空格
        adminDTO.setRealName(adminDTO.getRealName().trim());
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AdminDTO oldAdminDTO = adminDAO.selectOneDTO(params);
        if (oldAdminDTO == null) {
            log.error("updateUser param error, id:{}", id);
            throw new BizException("param error!");
        }
        //角色更新
        //批量插入 adminrole
        List<AdminRole> adminRoleList = new ArrayList<>(1);
        AdminRole adminRole;
        //根据adminid 删除所有adminrole对应关系
        adminRoleService.deleteByAdminId(id);
        for (Long roleId : adminDTO.getRoles()) {
            adminRole = new AdminRole();
            adminRole.setAdminId(id);
            adminRole.setRoleId(roleId);
            adminRoleList.add((AdminRole) this.packAddBaseProps(adminRole, request));
        }
        adminRoleService.saveAdminRoleList(adminRoleList);
        update(id, adminDTO, request);
        //清空redis缓存中的权限
        if (redisUtils.hasKey(Constant.ADMIN + adminDTO.getUserName() + Constant.ACTION)) {
            redisUtils.del(Constant.ADMIN + adminDTO.getUserName() + Constant.ACTION);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOneAdmin(@NonNull Long id, @NonNull AdminDTO adminDTO, HttpServletRequest request) throws BizException {
        log.info("full update adminDTO:{}", adminDTO);
        Admin admin = BeanUtil.copyProperties(adminDTO, new Admin());
        admin.setId(id);
        int cnt = adminDAO.update((Admin) this.packModifyBaseProps(admin, request));
        if (cnt != 1) {
            log.error("update error, data:{}", adminDTO);
            throw new BizException("update admin Error!");
        }
    }

    @Override
    public void updateAdminSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        adminDAO.updatex(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDeleteAdmin(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = adminDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException("删除失败");
        }
    }

    @Override
    public void deleteAdmin(@NonNull Long id) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = adminDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException("删除失败");
        }
    }

    @Override
    public AdminDTO findAdminById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AdminDTO adminDTO = adminDAO.selectOneDTO(params);
        return adminDTO;
    }

    @Override
    public AdminDTO findOneAdmin(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        Admin admin = adminDAO.selectOne(params);
        AdminDTO adminDTO = new AdminDTO();
        if (null != admin) {
            BeanUtils.copyProperties(admin, adminDTO);
        }
        return adminDTO;
    }

    @Override
    public List<AdminWithBorrowVerifyCountDTO> findAdminWithBorrowVerifyCountDTO(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AdminWithBorrowVerifyCountDTO> adminWithBorrowVerifyCountDTOList = adminDAO.selectAdminWithBorrowVerifyCountDTO(params);
        return adminWithBorrowVerifyCountDTOList;
    }

    @Override
    public int countList(@NonNull Map<String, Object> params) {
        return adminDAO.countList(params);
    }

    @Override
    public List<AdminDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AdminDTO> resultList = adminDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return adminDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return adminDAO.count(params);
    }

    @Override
    public int selectAdminAndRoleCount(Map<String, Object> params) {
        return adminDAO.selectAdminAndRoleCount(params);
    }

    @Override
    public int selectAdminWithBorrowVerifyCount(@NotNull Map<String, Object> params) {
        return adminDAO.selectAdminWithBorrowVerifyCount(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = adminDAO.groupCount(conditions);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("count");
            int count = 0;
            if (StringUtils.isNotBlank(value.toString())) {
                count = Integer.parseInt(value.toString());
            }
            map.put(key, count);
        }
        return map;
    }

    @Override
    public Double sum(String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("sumfield", sumField);
        return adminDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = adminDAO.groupSum(conditions);
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("sum");
            double sum = 0d;
            if (StringUtils.isNotBlank(value.toString())) {
                sum = Double.parseDouble(value.toString());
            }
            map.put(key, sum);
        }
        return map;
    }

    @Override
    public AdminOnlyDTO findOneAndRoleName(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AdminOnlyDTO admin = adminDAO.findOneAndRoleName(params);
        List<String> ids;
        List<AdminRoleDTO> adminRoleDTOS = admin.getAdminRoleDTO();
        ids = new ArrayList<>();
        for (AdminRoleDTO adminRoleDTO : adminRoleDTOS) {
            List<RoleDTO> roleDTOS = adminRoleDTO.getRoleDTO();
            for (RoleDTO roleDTO : roleDTOS) {
                ids.add(String.valueOf(roleDTO.getId()));
            }
        }
        admin.setIds(ids);
        admin.setAdminRoleDTO(null);
        return admin;
    }

    /**
     * 用户登录
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public String jwtLogin(String userName, String password, RedisUtils redisUtils, HttpServletRequest request) throws
            BizException {
        // 图片二维码校验
        String uuid = request.getParameter("uuid");
        String captchaCode = request.getParameter("captchaCode");
        if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(captchaCode)) {
            throw new BizException(I18nUtils.get("login.parameter.isNull", getLang(request)));
        }
        if (redisUtils.hasKey(uuid)) {
            // 获取redis里的保存的验证码
            String captchaCodeInRedis = (String) redisUtils.get(uuid);
            if (captchaCodeInRedis.equalsIgnoreCase(captchaCode)) {
                if (StringUtils.isEmpty(userName)) {
                    // 用户名不能为空
                    throw new BizException(I18nUtils.get("user.rule.usernameIsNull", getLang(request)));
                }
                if (StringUtils.isEmpty(password)) {
                    // 密码不能为空
                    throw new BizException(I18nUtils.get("user.rule.passwordIsNull", getLang(request)));
                }
                // 设置管理员标识 admin ,用于在过滤器进行判断时知道用户身份.
                String sign = Constant.ADMIN;
                Admin admin = null;
                Map<String, Object> params = new HashMap<>(1);
                if (userName != null) {
                    params.put("userName", userName);
                    // 在职状态
                    params.put("state", 1);
                    admin = adminDAO.selectOne(params);
                }
                if (admin != null) {
                    // 首先判断账号是否被锁定
                    if (redisUtils.hasKey(Constant.LOGIN_LOCK + admin.getUserName() + admin.getId())) {
                        // 允许登录的时间
                        long allowLoginTimeStamp = (long) redisUtils.get(Constant.LOGIN_LOCK + admin.getUserName() + admin.getId());
                        // 当前时间
                        long now = System.currentTimeMillis();
                        long seconds = (allowLoginTimeStamp - now) / 1000;
                        long minutes = seconds / 60;
                        if (seconds % 60 > 0) {
                            minutes++;
                        }
                        // 连续3次用户名与密码不匹配, 请于" + minutes + "分钟后登录"
                        throw new BizException(I18nUtils.get("wrong.rule.username",getLang(request)) + minutes + I18nUtils.get("minutes.rule.login",getLang(request)));
                    }
                    // 密码校验
                    if (!password.equals(admin.getPassword())) {
                        Integer loginErrCnt = admin.getErrorTimes();
                        if (loginErrCnt < 2) {
                            admin.setErrorTimes(++loginErrCnt);
                            adminDAO.update(admin);
                            // 密码错误,您还有" + (3 - loginErrCnt) + "次登录机会
                            throw new BizException(I18nUtils.get("wrong.rule.password",getLang(request)) + (3 - loginErrCnt) + I18nUtils.get("times.rule.left",getLang(request)));
                        } else {
                            // 连续5次手机号或用户名与密码不匹配时， 第六次在点击【登录】按钮时， 弹出提示:
                            // 您已连续五次输入密码错误,请于60分钟后登录
                            // 登录次数置为0
                            admin.setErrorTimes(0);
                            adminDAO.update(admin);
                            // redis里缓存账号锁定时间
                            long loginAllowTimeStamp = System.currentTimeMillis() + loginLockTime * 1000;
                            redisUtils.set(Constant.LOGIN_LOCK + admin.getUserName() + admin.getId(), loginAllowTimeStamp, loginLockTime);
                            // 连续3次用户名与密码不匹配, 您的账户已锁定，系统将于一小时后解锁
                            throw new BizException(I18nUtils.get("login.rule.lock", getLang(request)));
                        }
                    }
                    // 登陆成功
                    // (1)先取出字段login_date本次登录时间,然后赋值给字段last_login_date上次登录时间
                    // (2)先取出字段login_ip本次登录IP,然后赋值给字段last_login_ip上次登录IP
                    adminDAO.updateLastLoginDate(admin.getId());
                    // 登录次数+1
                    int loginCnt = admin.getLoginTimes();
                    Admin mer = new Admin();
                    mer.setLoginTimes(++loginCnt);
                    // (3)然后获取当前时间,存入字段login_date本次登录时间
                    // (4)然后获取当前登陆IP,存入字段login_ip本次登录IP
                    String clientIp = getIp(request);
                    long now = System.currentTimeMillis();
                    mer.setLoginIp(clientIp);
                    mer.setLoginTime(now);
                    mer.setId(admin.getId());
                    // 重置错误登陆次数为0
                    mer.setErrorTimes(0);
                    adminDAO.update(mer);
                    return JwtUtils.signs(userName.toLowerCase(), admin.getId(), sign, password, redisUtils);
                } else {
                    // 用户名或密码不正确
                    throw new BizException(I18nUtils.get("incorrect.name.or.password", getLang(request)));
                }
            } else {
                throw new BizException(I18nUtils.get("incorrect.verification.code", getLang(request)));
            }
        } else {
            throw new BizException(I18nUtils.get("verification.code.expired", getLang(request)));
        }
    }

    @Override
    public AdminDTO findActionByAdmin(String username) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("userName", username);
        AdminDTO actionName = adminDAO.selectOneByUsernameDTO(params);
        return actionName;
    }

    @Override
    public AdminDTO findAdminPartOfDataByUsername(String userName) {
        List<String> roleNames = new ArrayList<>();
        List<String> roleId = new ArrayList<>();
        Map<String, Object> params = new HashMap<>(1);
        params.put("userName", userName);
        AdminDTO adminDTO = adminDAO.selectAdminPartOfData(params);
        List<AdminDTO> adminDTOS = adminDAO.selectRoleName(params);
        if (adminDTOS != null) {
            for (AdminDTO admin : adminDTOS) {
                roleNames.add(admin.getRoleName());
                roleId.add(admin.getRoleId());
            }
            adminDTO.setRoleNames(roleNames);
            adminDTO.setRoleIds(roleId);
        }
        return adminDTO;
    }

    @Override
    public List<String> findAction(String username) {
        List<String> actionName = actionDAO.findAction(username);
        return getStrings(actionName);
    }

    private List<String> getStrings(List<String> actionName) {
        List<String> actions = new ArrayList<>();
        List<String> newAction = new ArrayList<>();
        for (String act : actionName) {
            if (!actions.contains(act)) {
                newAction.add(act);
                actions.add(act);
            }
        }
        return newAction;
    }

    @Override
    public List<String> findMenuAction(String username) {
        List<String> actionName = actionDAO.findMenuAction(username);
        return getStrings(actionName);
    }

    public static List<ActionOnlyDTO> treeMenuList(List<ActionOnlyDTO> actionList, Long parentId) {
        List<ActionOnlyDTO> actionDTOList = new ArrayList<>();
        for (ActionOnlyDTO actionDTO : actionList) {
            Long id = actionDTO.getMenu_id();
            Long pid = actionDTO.getParent_id();
            if (parentId.equals(pid)) {
                List<ActionOnlyDTO> menuDTOs = treeMenuList(actionList, id);
                actionDTO.setAuthority("admin");
                actionDTO.setHideChildrenInMenu(0);
                actionDTO.setChildren(menuDTOs);
                actionDTOList.add(actionDTO);
            }
        }
        return actionDTOList;
    }

    @Override
    public void modifyPassword(@NonNull AdminDTO adminDTO, HttpServletRequest request) throws BizException {
        log.info("full update userDTO:{}", adminDTO);
        // 根据电话查询用户
        if (StringUtils.isEmpty(adminDTO.getUserName())) {
            // 用户名不能为空
            throw new BizException(I18nUtils.get("user.rule.usernameIsNull", getLang(request)));
        }
        if (StringUtils.isEmpty(adminDTO.getOldPassword())) {
            // 旧登陆密码不能为空
            throw new BizException(I18nUtils.get("old.rule.password", getLang(request)));
        }
        if (StringUtils.isEmpty(adminDTO.getNewPassword())) {
            // 新登陆密码不能为空
            throw new BizException(I18nUtils.get("new.rule.password", getLang(request)));
        }
        if (StringUtils.isEmpty(adminDTO.getConfirmPassword())) {
            // 确认新登陆密码不能为空
            throw new BizException(I18nUtils.get("confirm.rule.password", getLang(request)));
        }
        // 验证新登陆密码
        if (!Validator.isAdminPassword(adminDTO.getNewPassword())) {
            throw new BizException(I18nUtils.get("user.rule.password", getLang(request)));
        }
        // 验证确认新登陆密码
        if (!Validator.isAdminPassword(adminDTO.getConfirmPassword())) {
            throw new BizException(I18nUtils.get("user.rule.password", getLang(request)));
        }
        HashMap<String, Object> params = new HashMap<>(1);
        params.put("userName", adminDTO.getUserName());
        Admin adm = adminDAO.selectOne(params);
        if (adm == null) {
            // 用户不存在
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        if (!adm.getPassword().equals(DigestUtils.md5Hex(adminDTO.getOldPassword()))) {
            // 旧登陆密码与原登陆密码不匹配,请重新输入旧登陆密码!
            throw new BizException(I18nUtils.get("old.password.notMatch", getLang(request)));
        }
        if (adm.getPassword().equals(DigestUtils.md5Hex(adminDTO.getNewPassword()))) {
            // 新登陆密码与原登陆密码相同,请重新输入新登陆密码!
            throw new BizException(I18nUtils.get("old.password.same", getLang(request)));
        }
        if (!adminDTO.getNewPassword().equals(adminDTO.getConfirmPassword())) {
            // 新旧密码不一致,请重新输入!
            throw new BizException(I18nUtils.get("confirmPassword.password.notMatch", getLang(request)));
        }
        adminDTO.setId(adm.getId());
        Admin admin = new Admin();
        admin.setId(adm.getId());
        // 支付密码MD5加密
        adminDTO.setPassword(DigestUtils.md5Hex(adminDTO.getNewPassword()));
        int cnt = adminDAO.update((Admin) this.packModifyBaseProps(BeanUtil.copyProperties(adminDTO, admin), request));
        if (cnt != 1) {
            log.error("update error, data:{}", adminDTO);
            throw new BizException("update user Error!");
        }
    }

    @Override
    public List<AdminOnlyDTO> findListAndRoleName
            (@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AdminOnlyDTO> resultList = adminDAO.findListAndRoleName(params);
        List<AdminOnlyDTO> adminOnlyDTOS = new ArrayList<>();
        if (resultList != null) {
            for (AdminOnlyDTO adminOnlyDTO : resultList) {
                Map<String, Object> param = new HashMap<>(1);
                param.put("id", adminOnlyDTO.getId());
                AdminOnlyDTO res = adminDAO.findOneAndRoleName(param);
                adminOnlyDTOS.add(res);
            }
        }
        if (adminOnlyDTOS != null) {
            for (AdminOnlyDTO admin : adminOnlyDTOS) {
                List<String> names;
                List<String> roleIds;
                if (admin != null) {
                    List<AdminRoleDTO> adminRoleDTOS = admin.getAdminRoleDTO();
                    names = new ArrayList<>();
                    roleIds = new ArrayList<>();
                    if (adminRoleDTOS != null) {
                        for (AdminRoleDTO adminRoleDTO : adminRoleDTOS) {
                            List<RoleDTO> roleDTOS = adminRoleDTO.getRoleDTO();
                            for (RoleDTO roleDTO : roleDTOS) {
                                names.add(roleDTO.getName());
                                roleIds.add(String.valueOf(roleDTO.getId()));
                            }
                        }
                        admin.setNames(names);
                        admin.setRoleIds(roleIds);
                        admin.setAdminRoleDTO(null);
                    }
                }
            }
        }
        return adminOnlyDTOS;
    }

    @Override
    public void updateState(@NonNull Long id, @NonNull AdminDTO adminDTO, HttpServletRequest request) throws
            BizException {
        log.info("full update adminDTO:{}", adminDTO);
        update(id, adminDTO, request);

    }

    private void update(@NonNull Long id, @NonNull AdminDTO adminDTO, HttpServletRequest request) throws
            BizException {
        Admin admin = BeanUtil.copyProperties(adminDTO, new Admin());
        admin.setId(id);
        int cnt = adminDAO.update((Admin) this.packModifyBaseProps(admin, request));
        if (cnt != 1) {
            log.error("update error, data:{}", adminDTO);
            throw new BizException("update admin Error!");
        }
    }

    @Override
    public int likeCount(@NotNull Map<String, Object> params) {
        return adminDAO.likeCount(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearParentId(@NonNull Long adminId, HttpServletRequest request) throws BizException {
        Map<String, Object> params = new HashMap<>();
        params.put("parentId", adminId);
        List<AdminDTO> adminDTOS = adminDAO.selectDTO(params);

        for (AdminDTO adminDTO : adminDTOS) {
            adminDTO.setParentId(1L);

            Admin admin = BeanUtil.copyProperties(adminDTO, new Admin());
            int cnt = adminDAO.update((Admin) this.packModifyBaseProps(admin, request));
            if (cnt != 1) {
                log.error("update error, data:{}", adminDTO);
                throw new BizException("update batch Error!");
            }
        }
    }

    @Override
    public void replaceTheGroupLeader(@NonNull Long roleId, @NonNull Long adminId, HttpServletRequest request) throws
            BizException {
        Long groupMembersId = roleId * 10L;
        Map<String, Object> params = new HashMap<>(1);
        params.put("roleId", groupMembersId);
        List<AdminRoleDTO> adminRoleDTOS = adminRoleDAO.selectDTO(params);
        if (adminRoleDTOS.size() == 0) {
            return;
        }
        for (int i = 0; i < adminRoleDTOS.size(); i++) {
            Admin admin = new Admin();
            admin.setId(adminRoleDTOS.get(i).getAdminId());
            admin.setParentId(adminId);
            int row = adminDAO.update((Admin) this.packModifyBaseProps(admin, request));
            if (row != 1) {
                log.error("update error, data:{}", admin);
                throw new BizException("update user Error!");
            }
        }
    }

    @Override
    public JSONObject appAdminLogin(JSONObject loginData, HttpServletRequest request) throws Exception {

        String loginName = loginData.getString("loginName");
        String loginPassword = loginData.getString("loginPassword");
        if(StringUtils.isEmpty(loginName) || StringUtils.isEmpty(loginPassword)){
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        loginPassword = MD5FY.MD5Encode(loginPassword);
        JSONObject msg = new JSONObject();
        String token ;
        String password ;
        Long id ;
        //查询用户是否存在
        Map<String, Object> params = new HashMap<>(2);
        params.put("userName", loginName);
        AdminDTO adminDTO = this.findOneAdmin(params);
        if(adminDTO == null ||adminDTO.getId() == null){
            throw new BizException(I18nUtils.get("user.rule.userNameNotPresence", getLang(request)));
        }
        password = adminDTO.getPassword();
        //查询登陆错误记录表,如果登陆时间与锁定时间相差仍在一小时内，则拒绝登陆
        id = adminDTO.getId();
        params.clear();
        params.put("userId",id);
        LoginMissDTO loginMissDTO = loginMissService.findOneLoginMiss(params);
        // 如果无记录，说明是首次登录，创建登录记录表
        if(loginMissDTO == null || loginMissDTO.getId() == null){
            loginMissDTO = new LoginMissDTO();
            loginMissDTO.setUserId(id);
            loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
            loginMissDTO.setId(loginMissService.saveLoginMiss(loginMissDTO,request));
        }
        Long lastErrorTime = loginMissDTO.getLastErrorTime();
        // 判断是否已经限制登录
        userService.countFrozenTime(loginMissDTO,request);
        //判断登陆密码是否正确
        if (password.equals(loginPassword)) {
            log.info("app admin login info , user data:{}, log in info:{}", id, loginData);
            // 查询用户角色
            params.clear();
            params.put("adminId",adminDTO.getId());
            AdminRoleDTO adminRoleDTO =adminRoleService.findOneAdminRole(params);
            RoleDTO roleDTO = roleService.findRoleById(adminRoleDTO.getRoleId());
            // 生成token
            token = JwtUtils.signApp(id, loginName, loginPassword, 20, redisUtils);

            //验证通过后重置登陆机会
            if (loginMissDTO.getChance() != StaticDataEnum.LOGIN_MISS_TIME.getCode()) {
                loginMissDTO.setChance(StaticDataEnum.LOGIN_MISS_TIME.getCode());
                loginMissService.updateLoginMiss(loginMissDTO.getId(), loginMissDTO, request);
            }

            //如果是商户登录，查询商户列表
            msg.put("loginName",loginName);
            msg.put("realName",adminDTO.getRealName());
            msg.put("token",token);
            msg.put("roleName",roleDTO.getName());
            msg.put("roleId",roleDTO.getId());
            msg.put("adminId",adminDTO.getId());
        } else {
            //判断登陆机会是否还有1次，不是则减少登陆机会重新登陆，是则将登陆机会重置为5次
            if (loginMissDTO.getChance() != StaticDataEnum.LOGIN_MISS_TIME_LEFT.getCode()) {
                loginMissService.loginMissRecord(loginMissDTO, request);
                log.info("app admin login info ,admin  data:{}, log in info:{}", adminDTO, loginData);
                throw new BizException(I18nUtils.get("incorrect.username.password", getLang(request),  new String[]{" " + (loginMissDTO.getChance()-1)}));
            } else {
                loginMissDTO.setChance(5);
                loginMissDTO.setLastErrorTime(System.currentTimeMillis());
                loginMissService.updateLoginMiss(loginMissDTO.getId(), loginMissDTO, request);
                log.info("app admin login info ,admin  data:{}, log in info:{}", adminDTO, loginData);
                throw new BizException(I18nUtils.get("login.lock", getLang(request), new String[]{(60 - (System.currentTimeMillis() - lastErrorTime)/(1000*60)) + " min"}));
            }
        }
        msg = JSONResultHandle.resultHandle(msg);
        return msg;
    }

}





















