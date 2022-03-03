package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AdsDTO;
import com.uwallet.pay.main.model.entity.Ads;
import com.uwallet.pay.main.service.AdsService;
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
 * 广告表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 广告表
 * @author: Strong
 * @date: Created in 2020-01-11 09:45:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Strong
 */
@RestController
@RequestMapping("/ads")
@Slf4j
@Api("广告表")
public class AdsController extends BaseController<Ads> {

    @Autowired
    private AdsService adsService;


    @ActionFlag(detail = "announcement_list")
    @ApiOperation(value = "分页查询广告表", notes = "分页查询广告表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-广告表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = adsService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AdsDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = adsService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询广告表", notes = "通过id查询广告表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get ads Id:{}", id);
        return R.success(adsService.findAdsById(id));
    }

    @ApiOperation(value = "通过查询条件查询广告表一条数据", notes = "通过查询条件查询广告表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询广告表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get ads findOne params:{}", params);
        int total = adsService.count(params);
        if (total > 1) {
            log.error("get ads findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AdsDTO adsDTO = null;
        if (total == 1) {
            adsDTO = adsService.findOneAds(params);
        }
        return R.success(adsDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增广告表", notes = "新增广告表")
    public Object create(@RequestBody AdsDTO adsDTO, HttpServletRequest request) {
        log.info("add ads DTO:{}", adsDTO);
        try {
            adsService.saveAds(adsDTO, request);
        } catch (BizException e) {
            log.error("add ads failed, adsDTO: {}, error message:{}, error all:{}", adsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改广告表", notes = "修改广告表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AdsDTO adsDTO, HttpServletRequest request) {
        log.info("put modify id:{}, ads DTO:{}", id, adsDTO);
        try {
            adsService.updateAds(id, adsDTO, request);
        } catch (BizException e) {
            log.error("update ads failed, adsDTO: {}, error message:{}, error all:{}", adsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除广告表", notes = "删除广告表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete ads, id:{}", id);
        try {
            adsService.logicDeleteAds(id, request);
        } catch (BizException e) {
            log.error("delete failed, ads id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
