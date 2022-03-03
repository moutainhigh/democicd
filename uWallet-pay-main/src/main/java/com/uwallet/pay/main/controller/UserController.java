package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.AnonymityUtil;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.filter.AdminActionButton;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.User;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * <p>
 * 用户
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户
 * @author: baixinyue
 * @date: Created in 2019-12-10 17:57:14
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api("用户")
public class UserController extends BaseController<User> {

    @Autowired
    private UserService userService;
    @Autowired
    private ServerService serverService;
    @Autowired
    private UserInfoUpdateLogService userInfoUpdateLogService;
    @Autowired
    private RefundFlowService refundFlowService;

    @Value("${latpay.merchantRefundPassword}")
    private String merchantRefundPassword;


    @Autowired
    private LatPayService latPayService;

    @Resource
    private QrPayFlowService qrPayFlowService;

    @ApiOperation(value = "分页查询用户", notes = "分页查询用户以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户", notes = "通过id查询用户")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get user Id:{}", id);
        return R.success(userService.findUserById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户一条数据", notes = "通过查询条件查询用户一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get user findOne params:{}", params);
        int total = userService.count(params);
        if (total > 1) {
            log.error("get user findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserDTO userDTO = null;
        if (total == 1) {
            userDTO = userService.findOneUser(params);
        }
        return R.success(userDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户", notes = "新增用户")
    public Object create(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        log.info("add user DTO:{}", userDTO);
        try {
            userService.saveUser(userDTO, request);
        } catch (Exception e) {
            log.error("add user failed, userDTO: {}, error message:{}, error all:{}", userDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户", notes = "修改用户")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserDTO userDTO, HttpServletRequest request) {
        log.info("put modify id:{}, user DTO:{}", id, userDTO);
        try {
            userService.updateUser(id, userDTO, request);
        } catch (BizException e) {
            log.error("update user failed, userDTO: {}, error message:{}, error all:{}", userDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户", notes = "删除用户")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete user, id:{}", id);
        try {
            userService.logicDeleteUser(id, request);
        } catch (BizException e) {
            log.error("delete failed, user id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @ApiOperation(value = "账户信息查询（调用账户信息查询）", notes = "账户信息查询（调用账户信息查询）")
    @GetMapping("/selectAccountUser/{id}")
    public Object selectAccountUser(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("get selectAccountUser:{}", id);
        Object accountUser;
        try {
            accountUser = serverService.selectAccountUser(id, request);
        } catch (Exception e) {
            log.error("get selectAccountUser failed, card: {}, error message:{}", id, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        return R.success(accountUser);
    }

    @ActionFlag(detail = "userManagement_list")
    @ApiOperation(value = "用户管理(营销活动)", notes = "用户管理(营销活动)")
    @PostMapping("/selectUserAndCard")
    public Object selectUserAndCard(HttpServletRequest request, @RequestBody JSONObject object) {
        log.info("selectUserAndCard:{}", object);
        Object accountUser;
        try {
            accountUser = serverService.selectUserAndCard(object);
        } catch (Exception e) {
            log.error("selectUserAndCard failed, card: {}, error message:{}", request, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        return accountUser;
    }

    @ApiOperation(value = "用户管理(营销活动下挂卡),id为用户系统的accountId", notes = "用户管理(营销活动下挂卡),id为用户系统的accountId")
    @GetMapping("/selectAccountCard/{id}")
    public Object selectAccountCard(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("get selectAccountUser:{}", id);
        JSONArray accountCard;
        try {
            accountCard = serverService.selectAccountCard(id, request);
        } catch (Exception e) {
            log.error("get selectAccountUser failed, card: {}, error message:{}", id, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed", getLang(request)));
        }
        for (int i = 0; i < accountCard.size(); i ++) {
            JSONObject cardInfo = accountCard.getJSONObject(i);
            if (cardInfo.getInteger("type").equals(StaticDataEnum.TIE_CARD_0.getCode())) {
                cardInfo.put("cardNo", AnonymityUtil.hideCardNo(cardInfo.getString("cardNo")));
            }
        }
        return R.success(accountCard);
    }

    @ApiOperation(value = "获取用户id", notes = "获取用户id")
    @GetMapping("/findUserInfo/{userId}")
    public Object findUserInfo(@PathVariable("userId") Long userId, HttpServletRequest request) {
        JSONObject userInfo = null;
        try {
            userInfo = userService.findOneUserInfo(userId);
        } catch (Exception e) {
            log.info("find user info fail, userId,:{}, error message:{}, e:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(userInfo);
    }

    @ApiOperation(value = "分期付重新认证", notes = "分期付重新认证")
    @PostMapping("/installmentRecertification/{userId}")
    public Object installmentRecertification(@PathVariable("userId") Long userId, HttpServletRequest request) {
        try {
            userService.installmentRecertification(userId, request);
        } catch (Exception e) {
            log.info("installemnt recertification fail, userId,:{}, error message:{}, e:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }

        return R.success();
    }

    @ApiOperation(value = "用户整合新列表", notes = "用户整合新列表")
    @PostMapping("/userList")
    public Object userList(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            int total = userService.countUserList(param);
            PagingContext pc;
            Vector<SortingContext> scs;
            List<UserListDTO> data=null;
            pc = getPagingContext(param, total);
            if (total > 0) {
                param.put("pc",pc);
                scs = getSortingContext(request);
                data = userService.findUserList(param, request);
            }
            return R.success(data, pc);
        } catch (Exception e) {
            log.info("installemnt recertification fail, userId,:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @ApiOperation(value = "查询一条配置", notes = "查询一条配置")
    @PostMapping("/findConfig")
    public JSONObject getOneConfigById(HttpServletRequest request){
        return serverService.getOneConfigById(369299727996338888L);
    }


    @ApiOperation(value = "风控等级列表", notes = "风控等级列表")
    @PostMapping("/riskScoreGradeList")
    public Object riskScoreGradeList(@RequestBody(required = false) JSONObject param, HttpServletRequest request) {
        try {
            return R.success(serverService.getRiskScoreGradeList(param, request));
        } catch (Exception e) {
            log.info("riskScoreGradeList fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "冻结用户", notes = "冻结用户")
    @PostMapping("/frozenUser")
    public Object frozenUser(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            log.info("冻结/解冻用户,{}",param);
            userService.frozenUser(param, request);
            return R.success();
        } catch (Exception e) {
            log.info("frozenUser fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "查询用户信息修改记录", notes = "查询用户信息修改记录")
    @GetMapping("/findUserUpdateLog")
    public Object findUserUpdateLog(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("查询用户信息修改记录,{}",params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserInfoUpdateLogDTO> list = new ArrayList<>();
        try {
            int total = userInfoUpdateLogService.count(params);
            pc = getPagingContext(request, total);
            if (total > 0) {
                scs = getSortingContext(request);
                list = userInfoUpdateLogService.findUpdateList(params, scs, pc);
            }
        }catch (Exception e){
            log.info("查询用户信息修改记录 fail, param:{}, error message:{}, e:{}", params, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(list, pc);
    }



    @ApiOperation(value = "用户详情", notes = "用户详情")
    @PostMapping("/getUserDetail/{userId}")
    public Object getUserDetail(@PathVariable("userId") Long userId, HttpServletRequest request) {
        try {
            return R.success(userService.getUserDetailData(userId, request));
        } catch (Exception e) {
            log.info("获取用户详情 fail, param:{}, error message:{}, e:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }





    @ApiOperation(value = "用户详情-卡、账户列表", notes = "用户详情-卡、账户列表")
    @PostMapping("/getUserCardAndAccount/{userId}")
    public Object getUserCardAndAccount(@PathVariable("userId") Long userId, HttpServletRequest request) {
        try {
            return R.success(userService.getCardAndAccountList(userId, request));
        } catch (Exception e) {
            log.info("获取用户详情 fail, param:{}, error message:{}, e:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @ApiOperation(value = "分页查询 用户详情-订单列表", notes = "分页查询 用户详情-支付订单列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/orderList")
    public Object getUserCardPayOrderList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        Long userId = Long.parseLong((String) params.get("userId")) ;
        if (params.get("transType")!=null){
            Integer transType =Integer.parseInt((String) params.get("transType")) ;

        }
        if(null == userId ){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        int total = qrPayFlowService.countUserDetail(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDetailPayOrderDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrPayFlowService.listUserDetailOrder(params, scs, pc);
        }
        return R.success(list, pc);
    }



    @ApiOperation(value = "用户详情-分期付订单还款计划", notes = "用户详情-分期付订单还款计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/repayListById/{borrowId}")
    public Object getRepayListById(@PathVariable("borrowId") Long borrowId, HttpServletRequest request) {
        try {
            return R.success(userService.getUserDetailRepayListById(borrowId,request));
        } catch (Exception e) {
            log.info("用户详情-分期付订单还款计划 fail, param:{}, error message:{}, e:{}", borrowId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @ApiOperation(value = "修改用户信息", notes = "修改用户信息")
    @PostMapping("/updateUserInfo")
    public Object updateUserInfo(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            userService.updateUserInfo(param, request);
            return R.success();
        } catch (Exception e) {
            log.info("获取用户详情 fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @PostMapping(name = "重置用户主动还款失败累计次数" ,value = "/resetUserRepayTimes/{userId}")
    @ApiOperation(value = "重置用户主动还款失败累计次数", notes = "重置用户主动还款失败累计次数")
    public Object resetUserRepayTimes(@PathVariable("userId") Long userId, HttpServletRequest request) {
        log.info("resetUserRepayTimes userId:{}", userId);
        try {
            UserDTO userDTO = userService.findUserById(userId);
            if(userDTO == null || userDTO.getId() == null){
                throw new BizException(I18nUtils.get("user.isEmpty", getLang(request)));
            }
            serverService.updateUserRepayTimes(userId,request);
        } catch (Exception e) {
            log.error("resetUserRepayTimes failed, userId: {}, error message:{}, error all:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ApiOperation(value = "查询用户APP使用", notes = "查询用户APP使用")
    @PostMapping("/findUserUseAPP/{userId}")
    public Object findUserUseAPP(@PathVariable Long userId, HttpServletRequest request) {
        try {
           JSONObject data= userService.findUserUseAPP(userId, request);
            return R.success(data);
        } catch (Exception e) {
            log.info("查询用户APP使用 fail, userId:{}, error message:{}, e:{}", userId, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }


    @ApiOperation(value = "分页查询 用户详情-推荐用户相关列表", notes = "分页查询 用户详情-推荐用户相关列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/inviteUserList")
    public Object inviteUserList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        //操作类型
        String operationTypeStr = params.get("operationType").toString();
        if(StringUtils.isBlank(operationTypeStr)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }

        //用户ID
        String userId = params.get("userId").toString();
        if(StringUtils.isBlank(userId)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }

        int total = userService.inviteUserCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDetailInviteUserDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.inviteUserList(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @ApiOperation(value = "分页查询 -红包使用情况列表", notes = "分页查询 用户详情-红包使用情况列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/usedPayoMoneyList")
    public Object usedPayoMoneyList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        //用户ID
        String userId = params.get("userId").toString();
        if(StringUtils.isBlank(userId)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        int total = userService.usedPayoMoneyCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDetailUsedPayoMoneyDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.usedPayoMoneyList(params, scs, pc);
        }
        return R.success(list, pc);
    }


    @ApiOperation(value = "分页查询 用户详情-还款记录", notes = "分页查询 用户详情-还款记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/repaymentHistoryList")
    public Object repaymentHistoryList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        //用户ID
        String userId = params.get("userId").toString();
        if(StringUtils.isBlank(userId)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        int total = userService.repaymentHistoryCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDetailRepaymentHistoryDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.repaymentHistoryList(params, scs, pc);
        }
        return R.success(list, pc);
    }



    @ApiOperation(value = "分页查询 用户详情-还款记录", notes = "分页查询 用户详情-还款记录列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/repaymentHistoryDetail")
     public Object repaymentHistoryDetail(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        String transactionId = params.get("transactionId").toString();
        if(StringUtils.isBlank(transactionId)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        int total = userService.repaymentHistoryDetailCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDetailRepaymentHistoryDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.repaymentHistoryDetailList(params, scs, pc);
        }
        return R.success(list, pc);
    }



    @ApiOperation(value = "分页查询 用户详情-分期付订单 欠款列表", notes = "分页查询 用户详情-分期付订单 欠款列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/repayList")
    public Object creditRepayList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        String userId = params.get("userId").toString();
        if(StringUtils.isBlank(userId)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        int total = userService.userDetailRepayDateCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserDetailRepaymentDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.userDetailRepayListGroupByDate(params, scs, pc);
        }
        return R.success(list, pc);
    }
    @AdminActionButton(value = 2)
    @ApiOperation(value = "协助注册用户", notes = "协助注册用户")
    @PostMapping("/AssistRegistrationUser")
    public Object AssistRegistrationUser(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            userService.AssistRegistrationUser(param, request);
            return R.success();
        } catch (Exception e) {
            log.info("协助注册用户 fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "用户提升/降低额度", notes = "用户提升/降低额度")
    @PostMapping("/updateUserAmount")
    public Object updateUserAmount(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            userService.updateUserAmount(param, request);
            return R.success();
        } catch (Exception e) {
            log.info("用户提升/降低额度 fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @ApiOperation(value = "修改用户修改记录备注", notes = "修改用户修改记录备注")
    @PostMapping("/updateUserInfoRemarks")
    public Object updateUserInfoRemarks(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            userService.updateUserInfoRemarks(param, request);
            return R.success();
        } catch (Exception e) {
            log.info("修改用户修改记录备注 fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @AdminActionButton(value = 3)
    @ApiOperation(value = "协助KYC", notes = "协助KYC")
    @PostMapping("/assistKyc")
    public Object assistKyc(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            userService.riskCheckParamsCheckBySystem(param,request);
            JSONObject result=userService.assistKyc(param, request);
            return R.success(result);
        } catch (Exception e) {
            log.info("协助KYC fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @AdminActionButton(value = 4)
    @ApiOperation(value = "延迟还款", notes = "延迟还款")
    @PostMapping("/latePayment")
    public Object latePayment(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            userService.latePayment(param, request);
            return R.success();
        } catch (Exception e) {
            log.info("延迟还款 fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
    @ApiOperation(value = "获取权限列表", notes = "获取权限列表")
    @PostMapping("/getActionButton")
    public Object getActionButton(@RequestBody JSONObject param, HttpServletRequest request) {
        try {
            return R.success(userService.getUserDetail(param,request));
        } catch (Exception e) {
            log.info("获取权限列表 fail, param:{}, error message:{}, e:{}", param, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "获取KYC记录列表", notes = "获取KYC记录列表")
    @GetMapping("/findKycLogList")
    public Object findKycLogList(HttpServletRequest request){
        try{
            Map<String, Object> params = getConditionsMap(request);
            String userId = params.get("userId").toString();
            if(StringUtils.isBlank(userId)){
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
            }
            int total = userService.findKycLogListCount(params);
            PagingContext pc;
            Vector<SortingContext> scs;
            List<UserStepLogDTO> list = new ArrayList<>();
            pc = getPagingContext(request, total);
            if (total > 0) {
                scs = getSortingContext(request);
                list = userService.findKycLogList(params, scs, pc);
            }
            return R.success(list, pc);
        }catch (Exception e){
            log.error("获取KYC记录列表异常:{}",e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "分页查询 -卡券列表", notes = "分页查询 用户详情-卡券情况列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/userPromotionList")
    public Object userPromotionList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        //用户ID
        String userId = params.get("userId").toString();
        if(StringUtils.isBlank(userId)){
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        int total = userService.userPromotionCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserPromotionDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userService.userPromotionList(params, scs, pc);
        }
        return R.success(list, pc);
    }


    /**
     *
     * @author zhangzeyuan
     * @date 2021/11/17 10:19
     * @param request
     * @return java.lang.Object
     */
    /*@GetMapping("/updateOneUserRedBalance/{userId}")
    @PassToken
    public Object updateOneUserRedBalance(@PathVariable("userId") Long userId, HttpServletRequest request) {
        try{
            userService.updateAllUserMarketing(userId);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "Success";
    }*/


    /**
     *
     * @author zhangzeyuan
     * @date 2021/11/17 10:19
     * @param request
     * @return java.lang.Object
     */
    /*@GetMapping("/updateNotSplitUser")
    @PassToken
    public Object updateNotSplitUser(HttpServletRequest request) {
        try{
            userService.updateNotSplitUser();
        }catch (Exception e){
            log.error("拆分未拆分的用户券失败" + e.getMessage());
        }
        return R.success();
    }
*/


    /**
     *
     * @author zhangzeyuan
     * @date 2021/11/17 10:19
     * @param request
     * @return java.lang.Object
     */
    /*@GetMapping("/updateMarketingReceiveAndUsedNumber")
    @PassToken
    public Object updateMarketingReceiveAndUsedNumber(HttpServletRequest request) {
        try{
            userService.updateMarketingReceiveAndUsedNumber();
        }catch (Exception e){
            log.error("拆分未拆分的用户券失败" + e.getMessage());
        }
        return R.success();
    }*/


}
