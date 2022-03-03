package com.uwallet.pay.main.service;


import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.BaseService;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.model.dto.AdminDTO;
import com.uwallet.pay.main.model.dto.AdminOnlyDTO;
import com.uwallet.pay.main.model.dto.AdminWithBorrowVerifyCountDTO;
import com.uwallet.pay.main.model.entity.Admin;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 管理员账户表
 * </p>
 *
 * @package: com.loancloud.rloan.main.admin.service
 * @description: 管理员账户表
 * @author: liming
 * @date: Created in 2019-09-09 15:24:15
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: liming
 */
public interface AdminService extends BaseService {

    /**
     * 保存一条 Admin 数据
     *
     * @param adminDTO 待保存的数据
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveAdmin(AdminDTO adminDTO, HttpServletRequest request) throws BizException;

    /**
     * 新添组员
     *
     * @param adminDTO 组员基本信息
     * @param roleId   组长角色 id
     * @param request
     * @throws BizException 保存失败异常
     */
    void saveGroupMembers(AdminDTO adminDTO, Long roleId, HttpServletRequest request) throws BizException;

    /**
     * 修改组员信息
     *
     * @param id 组员唯一id
     * @param adminDTO 待修改信息
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateGroupMembers(Long id, AdminDTO adminDTO, HttpServletRequest request) throws BizException;

    /**
     * 保存多条 Admin 数据
     *
     * @param adminList 待保存的数据列表
     * @throws BizException 保存失败异常
     */
    void saveAdminList(List<Admin> adminList) throws BizException;

    /**
     * 修改一条 Admin 数据
     *
     * @param id       数据唯一id
     * @param adminDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateAdmin(Long id, AdminDTO adminDTO, HttpServletRequest request) throws BizException;

    /**
     * 判断当前修改数据的 is_jobing 的状态是否在职，不在职的话释放案件
     *
     * @param id 数据唯一id
     * @param adminDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateOneAdmin(Long id, AdminDTO adminDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据Id部分更新实体 admin
     *
     * @param dataMap      需要更新的键值对
     * @param conditionMap where语句后的条件筛选的键值对
     */
    void updateAdminSelective(Map<String, Object> dataMap, Map<String, Object> conditionMap);

    /**
     * 根据id逻辑删除一条 Admin
     *
     * @param id      数据唯一id
     * @param request
     * @throws BizException 逻辑删除异常
     */
    void logicDeleteAdmin(Long id, HttpServletRequest request) throws BizException;

    /**
     * 根据id物理删除一条 Admin
     *
     * @param id 数据唯一id
     * @throws BizException 物理删除异常
     */
    void deleteAdmin(Long id) throws BizException;

    /**
     * 根据id查询一条 Admin
     *
     * @param id 数据唯一id
     * @return 查询到的 Admin 数据
     */
    AdminDTO findAdminById(Long id);

    /**
     * 根据条件查询得到第一条 admin
     *
     * @param params 查询条件
     * @return 符合条件的一个 admin
     */
    AdminDTO findOneAdmin(Map<String, Object> params);

    /**
     * 根据查询条件得到数据列表，包含分页排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<AdminWithBorrowVerifyCountDTO> findAdminWithBorrowVerifyCountDTO(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<AdminDTO> find(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params  查询条件
     * @param columns 需要查询的字段信息
     * @param scs     排序信息
     * @param pc      分页信息
     * @return 查询结果的数据集合
     * @throws BizException 查询异常
     */
    List<Map> findMap(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException;

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int count(Map<String, Object> params);
    /**
     * 审批小组-根据组长id统计同一小组成员
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int selectAdminAndRoleCount(Map<String, Object> params);

    /**
     * 根据条件查询总数
     * @param params
     * @return
     */
    int selectAdminWithBorrowVerifyCount(Map<String, Object> params);

    /**
     * 统计符合条件的数据条数
     *
     * @param params 统计的过滤条件
     * @return 统计结果
     */
    int countList(Map<String, Object> params);

    /**
     * 根据给定字段以及查询条件进行分组查询，并统计id的count
     *
     * @param group      分组的字段
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的统计条数
     */
    Map<String, Integer> groupCount(String group, Map<String, Object> conditions);

    /**
     * 根据给定字段查询统计字段的sum结果
     *
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 返回sum计算的结果值
     */
    Double sum(String sumField, Map<String, Object> conditions);

    /**
     * 根据给定字段以及查询条件进行分组查询，并sum统计Field
     *
     * @param group      分组的字段。
     * @param sumField   sumField 统计的字段名
     * @param conditions 查询的where条件
     * @return 查询结果 key为查询字段的值，value为查询字段的求和
     */
    Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions);

    /**
     * 根据id查询一条 Admin和roleName
     *
     * @param id 数据唯一id
     * @return 查询到的 Admin 数据
     */
    AdminOnlyDTO findOneAndRoleName(Long id);

    /**
     * 登陆
     * @param userName 账号
     * @param password 密码
     * @param redisUtils
     * @param request
     * @return
     * @throws BizException
     */
    String jwtLogin(String userName, String password, RedisUtils redisUtils, HttpServletRequest request) throws BizException;

    /**
     * 根据管理员账号查询 actionName权限标识,url,urlType。
     *
     * @param username 数据唯一username。
     * @return 查询到的 Action 数据。
     */
    AdminDTO findActionByAdmin(String username);

    /**
     * 登陆成功根据管理员名称返回需要的Admin信息。
     *
     * @param username 数据唯一username。
     * @return 查询到的 Admin 数据。
     * @author: Strong
     */
    AdminDTO findAdminPartOfDataByUsername(String username);

    /**
     * 根据管理员账号 只查询用户按钮权限。
     *
     * @param username 数据唯一username。
     * @return 查询到的 Action 数据。
     */
    List<String> findAction(String username);

    /**
     * 根据管理员账号 只查询用户菜单权限。
     *
     * @param username 数据唯一username。
     * @return 查询到的 Action 数据。
     */
    List<String> findMenuAction(String username);

    /**
     * 修改登陆密码
     *
     * @param adminDTO
     * @param request
     * @throws BizException
     */
    void modifyPassword(AdminDTO adminDTO, HttpServletRequest request) throws BizException;

    /**
     * 根据查询条件得到数据列表，包含分页和排序信息
     *
     * @param params 查询条件
     * @param scs    排序信息
     * @param pc     分页信息
     * @return 查询结果的数据集合
     */
    List<AdminOnlyDTO> findListAndRoleName(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc);

    /**
     * 修改Admin可用状态
     *
     * @param id 数据唯一id
     * @param adminDTO 待修改的数据
     * @param request
     * @throws BizException 修改失败异常
     */
    void updateState(Long id, AdminDTO adminDTO, HttpServletRequest request) throws BizException;

    /**
     *  模糊统计查询
     *
     * @param params
     * @return
     */
    int likeCount(Map<String, Object> params);

    /**
     * 变更组长时，清空原组员的 parentId
     * @param adminId
     * @param request
     * @throws BizException
     */
    void clearParentId(Long adminId, HttpServletRequest request) throws BizException;

    /**
     * 更换组长时，绑定组员
     *
     * @param roleId
     * @param adminId
     * @param request
     * @throws BizException
     */
    void replaceTheGroupLeader(Long roleId, Long adminId, HttpServletRequest request) throws BizException;

    /**
     * app管理员登录
     * @param data
     * @param request
     * @return
     * @throws Exception
     */
    JSONObject appAdminLogin(JSONObject data, HttpServletRequest request) throws  Exception;
}
