package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.GatewayDTO;
import com.uwallet.pay.main.model.entity.Gateway;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.GatewayService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;


/**
 * <p>
 * 支付渠道信息表

 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 支付渠道信息表

 * @author: Strong
 * @date: Created in 2019-12-12 10:16:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@RestController
@RequestMapping("/gateway")
@Slf4j
@Api("支付渠道信息表")
public class GatewayController extends BaseController<Gateway> {

    @Autowired
    private GatewayService gatewayService;

    @ActionFlag(detail = "channelManagement_list,user_list,payOrder_list,rechargeOrder_list," +
            "transferOrder_list,transactionManagement_list")
    @ApiOperation(value = "分页查询支付渠道信息表", notes = "分页查询支付渠道信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-支付渠道信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = gatewayService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<GatewayDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = gatewayService.find(params, scs, pc);
            if (list != null && list.size()>0 ) {
                for (GatewayDTO gatewayDTO : list) {
                    if( gatewayDTO.getRateType() != null && StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode() != gatewayDTO.getRateType() ){
                        gatewayDTO.setRate(gatewayDTO.getRate().multiply(new BigDecimal("100")));
                    }

                }
            }
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询支付渠道信息表", notes = "通过id查询支付渠道信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get gateway Id:{}", id);
        GatewayDTO gatewayDTO = gatewayService.findGatewayById(id);
        if(gatewayDTO!=null && gatewayDTO.getId()!=null && gatewayDTO.getRateType() != null && StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode() != gatewayDTO.getRateType()){
            gatewayDTO.setRate(gatewayDTO.getRate().multiply(new BigDecimal("100")));
        }
        return R.success(gatewayDTO);
    }

    @ApiOperation(value = "通过查询条件查询支付渠道信息表一条数据", notes = "通过查询条件查询支付渠道信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询支付渠道信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get gateway findOne params:{}", params);
        int total = gatewayService.count(params);
        if (total > 1) {
            log.error("get gateway findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        GatewayDTO gatewayDTO = null;
        if (total == 1) {
            gatewayDTO = gatewayService.findOneGateway(params);
        }
        return R.success(gatewayDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增支付渠道信息表", notes = "新增支付渠道信息表")
    public Object create(@RequestBody GatewayDTO gatewayDTO, HttpServletRequest request) {
        log.info("add gateway DTO:{}", gatewayDTO);
        try {
            // 参数校验
            Object x = checkParameter(gatewayDTO, request);
            if (x != null) {
                return x;
            }
            // 当前渠道类型下一个支付通道只允许有一条
            Map<String, Object> params = new HashMap<>(1);
            params.put("gatewayType", gatewayDTO.getGatewayType());
            List<GatewayDTO> list = gatewayService.find(params, null, null);
            for (GatewayDTO hasDTO : list) {
                if (hasDTO.getType().longValue() == gatewayDTO.getType().longValue()) {
                    return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("channel.exists", getLang(request)));
                }
            }
            if(StaticDataEnum.CHANNEL_RATE_TYPE_1.getCode() == gatewayDTO.getRateType()){
                gatewayDTO.setRate(gatewayDTO.getRate().divide(new BigDecimal("100")));
            }
            gatewayService.saveGateway(gatewayDTO, request);
        } catch (BizException e) {
            log.error("add gateway failed, gatewayDTO: {}, error message:{}, error all:{}", gatewayDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改支付渠道信息表", notes = "修改支付渠道信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody GatewayDTO gatewayDTO, HttpServletRequest request) {
        log.info("put modify id:{}, gateway DTO:{}", id, gatewayDTO);
        try {
            Object x = checkParameter(gatewayDTO, request);
            if (x != null) {
                return x;
            }
            // 当前渠道类型下一个支付通道只允许有一条
            GatewayDTO oldDTO = gatewayService.findGatewayById(id);
            if (oldDTO.getType().longValue() != gatewayDTO.getType().longValue()) {
                Map<String, Object> params = new HashMap<>(1);
                params.put("gatewayType", gatewayDTO.getGatewayType());
                List<GatewayDTO> list = gatewayService.find(params, null, null);
                for (GatewayDTO hasDTO : list) {
                    if (hasDTO.getType().longValue() == gatewayDTO.getType().longValue()) {
                        return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("channel.exists", getLang(request)));
                    }
                }
            }
            if(StaticDataEnum.CHANNEL_RATE_TYPE_1.getCode() == gatewayDTO.getRateType()){
                gatewayDTO.setRate(gatewayDTO.getRate().divide(new BigDecimal("100")));
            }
            gatewayService.updateGateway(id, gatewayDTO, request);
        } catch (BizException e) {
            log.error("update gateway failed, gatewayDTO: {}, error message:{}, error all:{}", gatewayDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    private Object checkParameter(@RequestBody GatewayDTO gatewayDTO, HttpServletRequest request) {
        if (StringUtils.isBlank(gatewayDTO.getChannelMerchantId())) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("channelId.null", getLang(request)));
        }
        if (StringUtils.isBlank(gatewayDTO.getChannelName())) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("channelName.null", getLang(request)));
        }
        if (gatewayDTO.getRate() == null) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("rate.null", getLang(request)));
        }
        if (gatewayDTO.getRateType() == null || (gatewayDTO.getRateType() != StaticDataEnum.CHANNEL_RATE_TYPE_0.getCode() && gatewayDTO.getRateType() != StaticDataEnum.CHANNEL_RATE_TYPE_1.getCode())) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("rate.null", getLang(request)));
        }
        // 渠道ID 1-100位字符
        if (gatewayDTO.getChannelMerchantId().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("channelId.length", getLang(request)));
        }
        // 渠道名称1-100位中英文
        if (gatewayDTO.getChannelName().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("channelName.length", getLang(request)));
        }
        // 产品URL应为1-100位
        if ( (!StringUtils.isBlank(gatewayDTO.getCertificatePath())) && gatewayDTO.getCertificatePath().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("certificatePath.length", getLang(request)));
        }
        if(gatewayDTO.getRateType() == StaticDataEnum.CHANNEL_RATE_TYPE_1.getCode()){
            // 渠道费率 比例 0-100
            if (gatewayDTO.getRate() .compareTo(BigDecimal.ZERO) < 0 || gatewayDTO.getRate() .compareTo(new BigDecimal("100"))>0) {
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("rate.range", getLang(request)));
            }
        }else{
            // 渠道费率 固定值 >0
            if (gatewayDTO.getRate() .compareTo(BigDecimal.ZERO) < 0 ) {
                return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("rate.range", getLang(request)));
            }
        }

        // 单笔限额最大金额 单笔限额最小金额 日累计限额不能为空
        if (gatewayDTO.getSingleMaxAmount() == null || gatewayDTO.getSingleMaxAmount().signum() == -1 || (gatewayDTO.getSingleMinAmount().compareTo(gatewayDTO.getSingleMaxAmount())) == 1) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("singleMaxAmount.error", getLang(request)));
        }
        if (gatewayDTO.getSingleMinAmount() == null || gatewayDTO.getSingleMinAmount().signum() == -1 || (gatewayDTO.getSingleMinAmount().compareTo(new BigDecimal("10000000"))) == 1) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("singleMinAmount.error", getLang(request)));
        }
        if (gatewayDTO.getDailyTotalAmount() == null || gatewayDTO.getDailyTotalAmount().signum() == -1 || (gatewayDTO.getDailyTotalAmount().compareTo(new BigDecimal("10000000"))) == 1) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("dailyTotalAmount.error", getLang(request)));
        }
        // 备注 0-1000位字符
        if (gatewayDTO.getRemark() != null && gatewayDTO.getRemark().length() > Validator.TEXT_LENGTH_1000) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("remark.error", getLang(request)));
        }
//
//        BigDecimal bigDecimalRate = new BigDecimal(gatewayDTO.getRate());
//        BigDecimal bigDecimal = new BigDecimal("100");
//        gatewayDTO.setRate(bigDecimalRate.divide(bigDecimal,4,BigDecimal.ROUND_HALF_UP).doubleValue());
        return null;
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除支付渠道信息表", notes = "删除支付渠道信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete gateway, id:{}", id);
        try {
            gatewayService.logicDeleteGateway(id, request);
        } catch (BizException e) {
            log.error("delete failed, gateway id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }



    @ApiOperation(value = "查询支付方式列表", notes = "查询支付方式列表")
    @GetMapping(value = "/getGateWayType",name = "查询支付方式列表")
    public Object getGateWayType() {
        List<GatewayDTO> list = gatewayService.getPayType();
        return R.success(list);
    }

    @PostMapping(value = "/updateState/{id}", name = "修改支付渠道可用状态")
    @ApiOperation(value = "修改支付渠道可用状态", notes = "修改支付渠道可用状态")
    public Object updateState(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("updateState, id:{}", id);
        try {
            gatewayService.updateState(id, request);
        } catch (Exception e) {
            log.error("updateState failed, gateway id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
}
