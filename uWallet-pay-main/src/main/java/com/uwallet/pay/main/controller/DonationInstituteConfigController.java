package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.DonationInstituteConfigDTO;
import com.uwallet.pay.main.model.entity.DonationInstituteConfig;
import com.uwallet.pay.main.service.DonationInstituteConfigService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * <p>
 * 捐赠机构配置
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 捐赠机构配置
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:26
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/donationInstituteConfig")
@Slf4j
@Api("捐赠机构配置")
public class DonationInstituteConfigController extends BaseController<DonationInstituteConfig> {

    @Autowired
    private DonationInstituteConfigService donationInstituteConfigService;

    @ActionFlag(detail = "DonationInstituteConfig_list")
    @ApiOperation(value = "分页查询捐赠机构配置", notes = "分页查询捐赠机构配置以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-捐赠机构配置列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = donationInstituteConfigService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<DonationInstituteConfigDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = donationInstituteConfigService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询捐赠机构配置", notes = "通过id查询捐赠机构配置")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get donationInstituteConfig Id:{}", id);
        return R.success(donationInstituteConfigService.findDonationInstituteConfigById(id));
    }

    @ApiOperation(value = "通过查询条件查询捐赠机构配置一条数据", notes = "通过查询条件查询捐赠机构配置一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询捐赠机构配置一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get donationInstituteConfig findOne params:{}", params);
        int total = donationInstituteConfigService.count(params);
        if (total > 1) {
            log.error("get donationInstituteConfig findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        DonationInstituteConfigDTO donationInstituteConfigDTO = null;
        if (total == 1) {
            donationInstituteConfigDTO = donationInstituteConfigService.findOneDonationInstituteConfig(params);
        }
        return R.success(donationInstituteConfigDTO);
    }

    @ActionFlag(detail = "DonationInstituteConfig_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增捐赠机构配置", notes = "新增捐赠机构配置")
    public Object create(@RequestBody DonationInstituteConfigDTO donationInstituteConfigDTO, HttpServletRequest request) {
        log.info("add donationInstituteConfig DTO:{}", donationInstituteConfigDTO);
        try {
            donationInstituteConfigService.saveDonationInstituteConfig(donationInstituteConfigDTO, request);
        } catch (BizException e) {
            log.error("add donationInstituteConfig failed, donationInstituteConfigDTO: {}, error message:{}, error all:{}", donationInstituteConfigDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "DonationInstituteConfig_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改捐赠机构配置", notes = "修改捐赠机构配置")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody DonationInstituteConfigDTO donationInstituteConfigDTO, HttpServletRequest request) {
        log.info("put modify id:{}, donationInstituteConfig DTO:{}", id, donationInstituteConfigDTO);
        try {
            donationInstituteConfigService.updateDonationInstituteConfig(id, donationInstituteConfigDTO, request);
        } catch (BizException e) {
            log.error("update donationInstituteConfig failed, donationInstituteConfigDTO: {}, error message:{}, error all:{}", donationInstituteConfigDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "DonationInstituteConfig_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除捐赠机构配置", notes = "删除捐赠机构配置")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete donationInstituteConfig, id:{}", id);
        try {
            donationInstituteConfigService.logicDeleteDonationInstituteConfig(id, request);
        } catch (BizException e) {
            log.error("delete failed, donationInstituteConfig id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
