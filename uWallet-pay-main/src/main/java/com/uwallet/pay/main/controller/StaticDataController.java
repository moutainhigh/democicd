package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.MerchantAppHomePageDTO;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.main.service.StaticDataService;
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
import java.util.*;


/**
 * <p>
 * 数据字典
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 数据字典
 * @author: Strong
 * @date: Created in 2019-12-13 15:35:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@RestController
@RequestMapping("/staticData")
@Slf4j
@Api("数据字典")
public class StaticDataController extends BaseController<StaticData> {

    @Autowired
    private StaticDataService staticDataService;

    @ApiOperation(value = "分页查询数据字典", notes = "分页查询数据字典以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-数据字典列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = staticDataService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<StaticDataDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = staticDataService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询数据字典", notes = "通过id查询数据字典")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get staticData Id:{}", id);
        return R.success(staticDataService.findStaticDataById(id));
    }

    @ApiOperation(value = "通过查询条件查询数据字典一条数据", notes = "通过查询条件查询数据字典一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询数据字典一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get staticData findOne params:{}", params);
        int total = staticDataService.count(params);
        if (total > 1) {
            log.error("get staticData findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        StaticDataDTO staticDataDTO = null;
        if (total == 1) {
            staticDataDTO = staticDataService.findOneStaticData(params);
        }
        return R.success(staticDataDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增数据字典", notes = "新增数据字典")
    public Object create(@RequestBody StaticDataDTO staticDataDTO, HttpServletRequest request) {
        log.info("add staticData DTO:{}", staticDataDTO);
        try {
            staticDataService.saveStaticData(staticDataDTO, request);
        } catch (BizException e) {
            log.error("add staticData failed, staticDataDTO: {}, error message:{}, error all:{}", staticDataDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改数据字典", notes = "修改数据字典")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody StaticDataDTO staticDataDTO, HttpServletRequest request) {
        log.info("put modify id:{}, staticData DTO:{}", id, staticDataDTO);
        try {
            staticDataService.updateStaticData(id, staticDataDTO, request);
        } catch (BizException e) {
            log.error("update staticData failed, staticDataDTO: {}, error message:{}, error all:{}", staticDataDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除数据字典", notes = "删除数据字典")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete staticData, id:{}", id);
        try {
            staticDataService.logicDeleteStaticData(id, request);
        } catch (BizException e) {
            log.error("delete failed, staticData id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "通过多个code，查询数据字典数据", notes = "分页查询数据字典以及排序功能")
    @GetMapping(value = "/findByCodeList",name = "通过多个code，查询数据字典数据")
    public Object findByCodeList(HttpServletRequest request) {
        String code = request.getParameter("code");
        if(StringUtils.isEmpty(code)) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),I18nUtils.get("parameters.error", getLang(request)));
        }
        final String[] codeList = code.split(",");
        Map<String, List<StaticData>> result = staticDataService.findByCodeList(codeList);
        return R.success(result);
    }

    @ApiOperation(value = "通过多个code，查询数据字典数据", notes = "分页查询数据字典以及排序功能")
    @PostMapping(value = "/findByCodeListForWesite",name = "通过多个code，查询数据字典数据")
    @PassToken
    public Object findByCodeListForWesite(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        String code = requestInfo.getString("code");
        if(StringUtils.isEmpty(code)) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(),I18nUtils.get("parameters.error", getLang(request)));
        }
        final String[] codeList = code.split(",");
        Map<String, List<StaticData>> result = staticDataService.findByCodeList(codeList);
        return R.success(result);
    }

    @ApiOperation(value = "通过多个code，查询数据字典数据", notes = "分页查询数据字典以及排序功能")
    @GetMapping(value = "/findAreaForWeb",name = "通过多个code，查询数据字典数据")
    @PassToken
    public Object findAreaForWeb(HttpServletRequest request) {
        return R.success(staticDataService.findAreaForWeb());
    }


    /**
     * 根据名称模糊查询城市列表
     *
     * @param keywords 查询参数
     * @param request
     * @return
     */
    @ApiOperation(value = "根据名称模糊查询城市列表", notes = "根据名称模糊查询城市列表")
    @GetMapping("/getCityListByKeywords")
    public Object getCityListByKeywords(@RequestParam("keywords") String keywords, HttpServletRequest request) {
        log.info("根据名称模糊查询城市列表, data:{}", keywords);
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("searchKeyword", keywords);
        paramMap.put("code", "city");
        return R.success(staticDataService.find(paramMap, null, null));
    }

}
