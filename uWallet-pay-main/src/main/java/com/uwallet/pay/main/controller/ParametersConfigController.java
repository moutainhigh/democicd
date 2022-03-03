package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.MathUtils;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.ParametersConfigDTO;
import com.uwallet.pay.main.model.entity.ParametersConfig;
import com.uwallet.pay.main.service.ParametersConfigService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * <p>
 * 系统配置表增加小额免密金额
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 系统配置表增加小额免密金额
 * @author: zhoutt
 * @date: Created in 2019-12-23 16:55:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/parametersConfig")
@Slf4j
@Api("系统配置表增加小额免密金额")
public class ParametersConfigController extends BaseController<ParametersConfig> {

    @Autowired
    private ParametersConfigService parametersConfigService;

    @ActionFlag(detail = "management_list")
    @ApiOperation(value = "分页查询系统配置表增加小额免密金额", notes = "分页查询系统配置表增加小额免密金额以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-系统配置表增加小额免密金额列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = parametersConfigService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ParametersConfigDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = parametersConfigService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询系统配置表增加小额免密金额", notes = "通过id查询系统配置表增加小额免密金额")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get parametersConfig Id:{}", id);
        return R.success(parametersConfigService.findParametersConfigById(id));
    }

    @ApiOperation(value = "通过查询条件查询系统配置表增加小额免密金额一条数据", notes = "通过查询条件查询系统配置表增加小额免密金额一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询系统配置表增加小额免密金额一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get parametersConfig findOne params:{}", params);
        int total = parametersConfigService.count(params);
        if (total > 1) {
            log.error("get parametersConfig findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ParametersConfigDTO parametersConfigDTO = null;
        if (total == 1) {
            parametersConfigDTO = parametersConfigService.findOneParametersConfig(params);
            parametersConfigDTO.setServiceCharge(MathUtils.multiply(parametersConfigDTO.getServiceCharge(), new BigDecimal(100)));
            parametersConfigDTO.setDiscountRate(MathUtils.multiply(parametersConfigDTO.getDiscountRate(), new BigDecimal(100)));
            parametersConfigDTO.setMerchantDiscountRatePlatformProportion(parametersConfigDTO.getMerchantDiscountRatePlatformProportion().multiply(new BigDecimal("100")));
            parametersConfigDTO.setExtraDiscountPayPlatform(MathUtils.multiply(parametersConfigDTO.getExtraDiscountPayPlatform(), new BigDecimal(100)));
            parametersConfigDTO.setExtraDiscountCreditPlatform(MathUtils.multiply(parametersConfigDTO.getExtraDiscountCreditPlatform(), new BigDecimal(100)));
        }
        return R.success(parametersConfigDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增系统配置表增加小额免密金额", notes = "新增系统配置表增加小额免密金额")
    public Object create(@RequestBody ParametersConfigDTO parametersConfigDTO, HttpServletRequest request) {
        log.info("add parametersConfig DTO:{}", parametersConfigDTO);
        try {
            parametersConfigService.saveParametersConfig(parametersConfigDTO, request);
        } catch (BizException e) {
            log.error("add parametersConfig failed, parametersConfigDTO: {}, error message:{}, error all:{}", parametersConfigDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改系统配置表增加小额免密金额", notes = "修改系统配置表增加小额免密金额")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ParametersConfigDTO parametersConfigDTO, HttpServletRequest request) {
        log.info("put modify id:{}, parametersConfig DTO:{}", id, parametersConfigDTO);
        try {
            parametersConfigService.updateParametersConfig(id, parametersConfigDTO, request);
        } catch (BizException e) {
            log.error("update parametersConfig failed, parametersConfigDTO: {}, error message:{}, error all:{}", parametersConfigDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除系统配置表增加小额免密金额", notes = "删除系统配置表增加小额免密金额")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete parametersConfig, id:{}", id);
        try {
            parametersConfigService.logicDeleteParametersConfig(id, request);
        } catch (BizException e) {
            log.error("delete failed, parametersConfig id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
