package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.JwtUtils;
import com.uwallet.pay.core.util.POIUtils;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.AdminDTO;
import com.uwallet.pay.main.model.dto.AdminOnlyDTO;
import com.uwallet.pay.main.model.dto.AdminWithBorrowVerifyCountDTO;
import com.uwallet.pay.main.model.entity.Admin;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.service.AdminService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * <p>
 * 管理员账户表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.controller
 * @description: 管理员账户表
 * @author: liming
 * @date: Created in 2019-09-09 15:24:15
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: liming
 */
@RestController
@RequestMapping("/admin")
@Slf4j
@Api("管理员账户表")
public class AdminController extends BaseController<Admin> {

    @Autowired
    private AdminService adminService;
    @Autowired
    private RedisUtils redisUtils;

    @ApiOperation(value = "分页查询管理员账户", notes = "分页查询管理员账户表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query"),
            @ApiImplicitParam(name = "state", value = "在职状态", paramType = "query"),
            @ApiImplicitParam(name = "inJobIng", value = "在岗状态", paramType ="query"),
            @ApiImplicitParam(name = "realName", value = "人员姓名", paramType = "query")
    })
    @GetMapping(value = "/list/{roleId}", name = "查询-管理员账户列表")
    public Object adminList(@PathVariable("roleId") Long roleId, HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        params.put("groupLeaderRoleId", roleId);
        params.put("groupMembersRoleId", roleId * 10L);
        int total = adminService.selectAdminWithBorrowVerifyCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AdminWithBorrowVerifyCountDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = adminService.findAdminWithBorrowVerifyCountDTO(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "admin_list")
    @ApiOperation(value = "管理员表列表带角色名称", notes = "管理员表列表带角色名称以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(value = "/listAndRoleName", name="查询-管理员表列表带角色名称")
    public Object listAndRoleName(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        if (params.get(Constant.USERNAME) != null) {
            params.put("userName",params.get("userName").toString().trim());
        }
        if (params.get(Constant.REALNAME) != null) {
            params.put("realName",params.get("realName").toString().trim());
        }
        int total = adminService.countList(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AdminOnlyDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = adminService.findListAndRoleName(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "分页查询管理员账户", notes = "分页查询管理员账户以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-管理员账户表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = adminService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AdminDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = adminService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询管理员账户", notes = "通过id查询管理员账户")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get admin Id:{}", id);
        return R.success(adminService.findAdminById(id));
    }

    @ApiOperation(value = "通过id查询管理员表带角色名称", notes = "通过id查询管理员表带角色名称")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/findOne/{id}", name="详情")
    public Object findOneAndRoleName(@PathVariable("id") Long id) {
        log.info("get admin Id:{}", id);
        return R.success(adminService.findOneAndRoleName(id));
    }

    @ApiOperation(value = "通过查询条件查询管理员账户一条数据", notes = "通过查询条件查询管理员账户一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询管理员账户一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get admin findOne params:{}", params);
        int total = adminService.count(params);
        if (total > 1) {
            log.error("get admin findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("AccessManageController.rule._95", getLang(request)));
        }
        AdminDTO adminDTO = null;
        if (total == 1) {
            adminDTO = adminService.findOneAdmin(params);
        }
        return R.success(adminDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增管理员账户", notes = "新增管理员账户")
    public Object create(@RequestBody AdminDTO adminDTO, HttpServletRequest request) {
        log.info("add admin DTO:{}", adminDTO);
        try {
            adminService.saveAdmin(adminDTO, request);
        } catch (BizException e) {
            log.error("add admin failed, adminDTO: {}, error message:{}, error all:{}", adminDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改管理员账户", notes = "修改管理员账户")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AdminDTO adminDTO, HttpServletRequest request) {
        log.info("put modify id:{}, admin DTO:{}", id, adminDTO);
        try {
            adminService.updateAdmin(id, adminDTO, request);
        } catch (BizException e) {
            log.error("update admin failed, adminDTO: {}, error message:{}, error all:{}", adminDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除管理员账户", notes = "删除管理员账户")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete admin, id:{}", id);
        try {
            adminService.logicDeleteAdmin(id, request);
        } catch (BizException e) {
            log.error("delete failed, admin id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    /**
     * 登陆页面
     *
     * @return
     */
    @PassToken
    @GetMapping(value = "/login", name = "管理员登陆")
    @ApiOperation(value = "管理员登陆", notes = "管理员登陆")
    public Object login(AdminDTO adminDTO, HttpServletRequest request) {
        log.info("background login, data:{}", adminDTO);
        //1.执行登陆方法
        String token;
        try {
            token = adminService.jwtLogin(adminDTO.getUserName(), DigestUtils.md5Hex(adminDTO.getPassword()), redisUtils, request);
            Map<String, Object> params = new HashMap<>(1);
            params.put("token", token);
            return R.success(params);
        } catch (BizException e) {
            log.info("background login failed, data:{}, error message:{}, e:{}", adminDTO, e.getMessage(), e);
            //登陆失败或第一次登陆没有token或token失效或没有权限访问该页面或token验证码生成失败
            return R.fail(ErrorCodeEnum.LOGIN__ERROR.getCode(),e.getMessage());
        }
    }

    /**
     * 登陆成功,返回需要的基本用户信息
     *
     * @return
     */
    @PostMapping(value = "/user", name = "登陆成功,返回需要的基本用户信息")
    @ApiOperation(value = "登陆成功,返回需要的基本用户信息", notes = "登陆成功,返回需要的基本用户信息")
    public Object user(HttpServletRequest request) {
        String headTokenValue = request.getHeader("Authorization");
        String loginName = JwtUtils.getUsername(headTokenValue.replace("Bearer ", ""));
        AdminDTO admin = adminService.findAdminPartOfDataByUsername(loginName);
        return R.success(admin);
    }

    /**
     * 登陆成功,将角色权限查出返回给前端，用于展示
     * @param request
     * @return
     */
    @PostMapping(value = "/menu", name = "登陆成功,将角色权限查出返回给前端，用于展示")
    @ApiOperation(value = "登陆成功,将角色权限查出返回给前端，用于展示", notes = "登陆成功,将角色权限查出返回给前端，用于展示")
    public Object action(HttpServletRequest request) {
        //登陆成功,将角色权限查出返回给前端，用于展示
        String headTokenValue = request.getHeader("Authorization");
        String loginName = JwtUtils.getUsername(headTokenValue.replace("Bearer ", ""));
        List<String> menu = adminService.findMenuAction(loginName);
        return R.success(menu);
    }

    @PostMapping(value = "/changePassword", name = "修改登陆密码")
    @ApiOperation(value = "修改登陆密码", notes = "修改登陆密码")
    public Object modifyPassword(@RequestBody AdminDTO adminDTO, HttpServletRequest request) {
        log.info("modifyPassword adminDTO DTO:{}", adminDTO);
        try {
            // 从请求头中获取token的值
            String headTokenValue = request.getHeader("Authorization");
            String loginName = JwtUtils.getUsername(headTokenValue.replace("Bearer ", ""));
            adminDTO.setUserName(loginName);
            adminService.modifyPassword(adminDTO, request);
        } catch (Exception e) {
            log.error("changePassword failed,  userDTO: {}, error message:{}", adminDTO,
                    e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success(I18nUtils.get("AdminController.rule._400", getLang(request)));
    }

    @PutMapping("/state/{id}")
    @ApiOperation(value = "修改管理员列表可用状态(不参与角色调整)", notes = "修改管理员列表可用状态(不参与角色调整)")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateState(@PathVariable("id") Long id, @RequestBody AdminDTO adminDTO, HttpServletRequest request) {
        log.info("put modify id:{}, admin DTO:{}", id, adminDTO);
        try {
            adminService.updateState(id, adminDTO, request);
        } catch (BizException e) {
            log.error("update admin failed, adminDTO: {}, error message:{}, error all:{}", adminDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping("/state")
    public Object updateState(MultipartFile multipartFile) {
        try {
            List<User> objects = POIUtils.readExcel(multipartFile, User.class);
            System.out.println(objects);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success();
    }


}
