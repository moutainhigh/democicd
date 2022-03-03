package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.MerchantApplication;
import com.uwallet.pay.main.service.MerchantApplicationService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.service.MerchantService;
import com.uwallet.pay.main.service.UserService;
import com.uwallet.pay.main.service.WholeSalesFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 商户申请表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 商户申请表
 * @author: zhoutt
 * @date: Created in 2021-04-14 11:28:05
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/merchantApplication")
@Slf4j
@Api("商户申请表")
public class MerchantApplicationController extends BaseController<MerchantApplication> {

    @Autowired
    private MerchantApplicationService merchantApplicationService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @ActionFlag(detail = "MerchantApplication_list")
    @ApiOperation(value = "分页查询商户申请表", notes = "分页查询商户申请表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-商户申请表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantApplicationService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantApplicationDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantApplicationService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询商户申请表", notes = "通过id查询商户申请表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get merchantApplication Id:{}", id);
        return R.success(merchantApplicationService.findMerchantApplicationById(id));
    }

    @ApiOperation(value = "通过查询条件查询商户申请表一条数据", notes = "通过查询条件查询商户申请表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询商户申请表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get merchantApplication findOne params:{}", params);
        int total = merchantApplicationService.count(params);
        if (total > 1) {
            log.error("get merchantApplication findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MerchantApplicationDTO merchantApplicationDTO = null;
        if (total == 1) {
            merchantApplicationDTO = merchantApplicationService.findOneMerchantApplication(params);
        }
        return R.success(merchantApplicationDTO);
    }

    @ActionFlag(detail = "MerchantApplication_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增商户申请表", notes = "新增商户申请表")
    public Object create(@RequestBody MerchantApplicationDTO merchantApplicationDTO, HttpServletRequest request) {
        log.info("add merchantApplication DTO:{}", merchantApplicationDTO);
        try {
            merchantApplicationService.saveMerchantApplication(merchantApplicationDTO, request);
        } catch (BizException e) {
            log.error("add merchantApplication failed, merchantApplicationDTO: {}, error message:{}, error all:{}", merchantApplicationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MerchantApplication_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改商户申请表", notes = "修改商户申请表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MerchantApplicationDTO merchantApplicationDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchantApplication DTO:{}", id, merchantApplicationDTO);
        try {
            merchantApplicationService.updateMerchantApplication(id, merchantApplicationDTO, request);
        } catch (BizException e) {
            log.error("update merchantApplication failed, merchantApplicationDTO: {}, error message:{}, error all:{}", merchantApplicationDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MerchantApplication_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除商户申请表", notes = "删除商户申请表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete merchantApplication, id:{}", id);
        try {
            merchantApplicationService.logicDeleteMerchantApplication(id, request);
        } catch (BizException e) {
            log.error("delete failed, merchantApplication id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "保存新商户申请信息", notes = "保存新商户申请信息")
    @PostMapping("/saveNewMerchant")
    public Object saveNewMerchant(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("saveNewMerchant, requestInfo:{}", requestInfo);
        try {
            String id = merchantApplicationService.saveNewMerchantMessage(requestInfo, request);
            return R.success(id);
        } catch (Exception e) {
            log.error("saveNewMerchant requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }

    }
    @ApiOperation(value = "审核拒绝", notes = "审核拒绝")
    @PostMapping("/reject")
    public Object reject(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("merchantApplication.reject, requestInfo:{}", requestInfo);
        try {
            merchantApplicationService.reject(requestInfo, request);
        } catch (Exception e) {
            log.error("merchantApplication.reject requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @ApiOperation(value = "新商户申请信息提交", notes = "新商户申请信息提交")
    @PostMapping("/newMerchantSubmitAudit")
    public Object newMerchantSubmitAudit(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("newMerchantSubmitAudit, requestInfo:{}", requestInfo);
        try {
            merchantApplicationService.newMerchantSubmitAudit(requestInfo, request);
        } catch (Exception e) {
            log.error("newMerchantSubmitAudit requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ApiOperation(value = "发送邮箱验证码", notes = "发送邮箱验证码")
    @PostMapping("/sendEmailCode")
    public Object sendEmailCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("发送邮箱验证码, requestInfo:{}", requestInfo);
        Long id=null;
        try {
            String email = requestInfo.getString("email");
            if (!Validator.isEmail(email)){
                throw new BizException(I18nUtils.get("username.contains.invalid.characters", getLang(request)));
            }
            if (email == null || email.length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(email))) {
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("email.format.error", getLang(request)));
            }
            // 插入一条记录
            userService.sendSecurityCode("0",email, StaticDataEnum.USER_TYPE_20.getCode(),request);
            id = merchantApplicationService.newAccountSubmitAudit(requestInfo, request);
        } catch (Exception e) {
            log.error("发送邮箱验证码 requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(id.toString());
    }

    @ApiOperation(value = "添加账户", notes = "添加账户")
    @PostMapping("/saveAccount")
    public Object saveAccount(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("添加账户, requestInfo:{}", requestInfo);
        try {
            // 修改状态
            merchantApplicationService.updateMerchantApplicationNew(requestInfo,request);
        } catch (Exception e) {
            log.error("添加账户 requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ApiOperation(value = "模糊查询商户列表", notes = "模糊查询商户列表")
    @GetMapping("/getMerchantList")
    public Object getMerchantList( String name, HttpServletRequest request) {
        log.info("模糊查询商户列表,");
        PagingContext  pagingContext = new PagingContext();
        JSONObject merchantList=null;
        try {
            merchantList = merchantService.getMerchantList(null, name, request, null);
            Object merchantListOld = merchantList.get("merchantList");
            if (merchantListOld instanceof List){
                List<Map<String, Object>> listOld=(List<Map<String, Object>>)merchantListOld;
                for (Map<String, Object> map : listOld) {
                    // long会数据丢失，单独处理id转String
                    map.put("id",map.get("id").toString());
                }
            }
        } catch (Exception e) {
            log.error("模糊查询商户列表  error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(merchantList,pagingContext);
    }


    @ApiOperation(value = "保存整体出售订单", notes = "保存整体出售订单")
    @PostMapping("/saveWholesale")
    public Object saveWholesale(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("保存整体出售订单, requestInfo:{}", requestInfo);
        PagingContext pc=new PagingContext();
        try {
            BigDecimal amount = requestInfo.getBigDecimal("amount");
            Long merchantId = requestInfo.getLong("merchantId");
            if (amount==null||merchantId==null){
                throw new BizException(I18nUtils.get("parameters.error", getLang(request)));
            }
            MerchantDTO merchantById = merchantService.findMerchantById(merchantId);
            if (merchantById==null){
                throw new BizException(I18nUtils.get("merchant.is.not.exist", getLang(request)));
            }
            // 封装需要数据
            WholeSalesFlowDTO wholeSalesFlowDTO=new WholeSalesFlowDTO();
            BigDecimal bigDecimal = new BigDecimal(0.00);
            wholeSalesFlowDTO.setMerchantDiscount(bigDecimal);
            wholeSalesFlowDTO.setCustomerDiscount(bigDecimal);
            wholeSalesFlowDTO.setAmount(amount);
            wholeSalesFlowDTO.setMerchantId(merchantId);
            wholeSalesFlowDTO.setUserId(merchantById.getUserId());
            wholeSalesFlowDTO.setFromWhere(1);
            Long aLong = wholeSalesFlowService.wholeSaleApply(wholeSalesFlowDTO, request);
            MerchantApplicationDTO merchantApplicationDTO = new MerchantApplicationDTO();
            merchantApplicationDTO.setType(StaticDataEnum.MERCHANT_APPLICATION_TYPE_2.getCode());
            merchantApplicationDTO.setState(StaticDataEnum.APPROVE_STATE_2.getCode());
            merchantApplicationDTO.setData(requestInfo.toString());
            merchantApplicationDTO.setWholeSaleId(aLong);
            UserDTO userById = userService.findUserById(merchantById.getUserId());
            if (userById!=null){
                merchantApplicationDTO.setEmail(userById.getEmail());
            }
            merchantApplicationDTO.setPracticalName(merchantById.getPracticalName());
            merchantApplicationService.saveMerchantApplication(merchantApplicationDTO,request);
        } catch (Exception e) {
            log.error("保存整体出售订单 requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "查询新商户信息", notes = "查询新商户信息")
    @GetMapping("/getNewMerchantMessage/{id}")
    public Object getNewMerchantMessage(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("查询新商户信息, requestInfo id:{}", id);
        JSONObject result;
        try {
            result= merchantApplicationService.getMerchantMessage(id, request);

        } catch (Exception e) {
            log.error("查询新商户信息 requestInfo id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @ApiOperation(value = "模糊查询商户邮箱列表", notes = "模糊查询商户邮箱列表")
    @PostMapping("/getMerchantEmailList")
    public Object getMerchantEmailList(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("模糊查询商户邮箱列表, requestInfo:{}", requestInfo);
        List<MerchantLoginDTO>  result;
        try {
            result = merchantApplicationService.getMerchantEmails(requestInfo.getString("email"));
        } catch (Exception e) {
            log.error("模糊查询商户邮箱列表 requestInfo: {}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }
}
