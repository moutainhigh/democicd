package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.MarketingFlowDTO;
import com.uwallet.pay.main.model.dto.MarketingManagementDTO;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.entity.MarketingManagement;
import com.uwallet.pay.main.service.MarketingFlowService;
import com.uwallet.pay.main.service.MarketingManagementService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 商户营销码
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 商户营销码
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:29:48
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/marketingManagement")
@Slf4j
@Api("商户营销码")
public class MarketingManagementController extends BaseController<MarketingManagement> {

    @Autowired
    private MarketingManagementService marketingManagementService;

    @Autowired
    private MarketingFlowService marketingFlowService;

    @Autowired
    private StaticDataService staticDataService;

    @ActionFlag(detail = "MarketingManagement_list")
    @ApiOperation(value = "分页查询商户营销码", notes = "分页查询商户营销码以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-商户营销码列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = marketingManagementService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MarketingManagementDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = marketingManagementService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询商户营销码", notes = "通过id查询商户营销码")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get marketingManagement Id:{}", id);
        return R.success(marketingManagementService.findMarketingManagementById(id));
    }

    @ApiOperation(value = "通过查询条件查询商户营销码一条数据", notes = "通过查询条件查询商户营销码一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询商户营销码一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get marketingManagement findOne params:{}", params);
        int total = marketingManagementService.count(params);
        if (total > 1) {
            log.error("get marketingManagement findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MarketingManagementDTO marketingManagementDTO = null;
        if (total == 1) {
            marketingManagementDTO = marketingManagementService.findOneMarketingManagement(params);
        }
        return R.success(marketingManagementDTO);
    }

    @ActionFlag(detail = "MarketingManagement_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增商户营销码", notes = "新增商户营销码")
    public Object create(@RequestBody MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) {
        log.info("add marketingManagement DTO:{}", marketingManagementDTO);
        try {
            marketingManagementService.saveMarketingManagement(marketingManagementDTO, request);
        } catch (BizException e) {
            log.error("add marketingManagement failed, marketingManagementDTO: {}, error message:{}, error all:{}", marketingManagementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

//    @ActionFlag(detail = "MarketingManagement_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改商户营销码", notes = "修改商户营销码")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) {
        log.info("put modify id:{}, marketingManagement DTO:{}", id, marketingManagementDTO);
        try {
            marketingManagementService.updateMarketingManagement(id, marketingManagementDTO, request);
        } catch (BizException e) {
            log.error("update marketingManagement failed, marketingManagementDTO: {}, error message:{}, error all:{}", marketingManagementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MarketingManagement_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除商户营销码", notes = "删除商户营销码")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete marketingManagement, id:{}", id);
        try {
            marketingManagementService.logicDeleteMarketingManagement(id, request);
        } catch (BizException e) {
            log.error("delete failed, marketingManagement id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "分页查询商户营销码", notes = "分页查询商户营销码以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/marketingCodeList")
    public Object marketingCodeList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = marketingManagementService.marketingCodeCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MarketingManagementDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = marketingManagementService.marketingCodeList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "保存营销码", notes = "保存营销码")
    @PostMapping("/saveMarketingCode")
    public Object saveMarketingCode(@RequestBody MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) {
        log.info("save marketing code, data:{}", marketingManagementDTO);
        try {
            marketingManagementService.saveMarketingCode(marketingManagementDTO, request);
            } catch (Exception e) {
            log.info("save marketing code, data:{}, error message:{}, e:{}", marketingManagementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "保存营销码", notes = "保存营销码")
    @DeleteMapping("/deleteMarketingCode/{id}")
    public Object deleteMarketingCode(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete marketing code, id:{}", id);
        try {
            marketingManagementService.deleteMarketingCode(id, request);
        } catch (Exception e) {
            log.info("delete marketing code, id:{}, error message:{}, e:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "保存营销码", notes = "保存营销码")
    @GetMapping("/codeUseLog/{code}")
    public Object codeUseLog(@PathVariable("code") String code, HttpServletRequest request) {
        List<JSONObject> resultList = null;
        try {
            resultList = marketingManagementService.codeUseLog(code, request);
        } catch (Exception e) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(resultList);
    }


    @ApiOperation(value = "分页查询营销码消费记录", notes = "分页查询营销码消费记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/promotionCodeUsedList")
    public Object promotionCodeUsedList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);

        if (null == params.get("createdDate")) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        String createdDate = (String) params.get("createdDate");
        HashMap<String, Object> queryMap = Maps.newHashMapWithExpectedSize(1);
        queryMap.put("code", "promotionTime");
        StaticDataDTO staticData = staticDataService.findOneStaticData(queryMap);
        if(null != staticData && null !=  staticData.getId() && StringUtils.isNotBlank(staticData.getValue())){
            queryMap.clear();
            queryMap.put("s", params.get("s"));
            queryMap.put("p", params.get("p"));
            queryMap.put("state", 1);

            if(Long.valueOf(createdDate).longValue() < Long.valueOf(staticData.getValue()).longValue()){
                queryMap.put("direction", 0);
                queryMap.put("transType", 25);
                queryMap.put("code", params.get("code"));
            }else{
                queryMap.put("direction", 1);
                queryMap.put("transType", 21);
                queryMap.put("payState", 31);
                queryMap.put("marketingManageId", params.get("marketingManageId"));
            }
        }
        int total = marketingFlowService.countUsedLog(queryMap);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MarketingFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = marketingFlowService.getMarketingCodeUsedLog(queryMap, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "查询邀请码信息", notes = "查询邀请码信息")
    @GetMapping(value = "/selectOneInviteCode", name="查询邀请码信息")
    public Object selectOneInviteCode() {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("type", StaticDataEnum.MARKETING_TYPE_2.getCode());
        params.put("status",StaticDataEnum.STATUS_1.getCode());

        return R.success(marketingManagementService.findOneMarketingManagement(params));
    }

    @PutMapping("/updateInvitationCode")
    @ApiOperation(value = "修改邀请码", notes = "修改邀请码")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateInvitationCode(@RequestBody MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) {
        if (null == marketingManagementDTO.getId()) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.null", getLang(request)));
        }
        try {
            marketingManagementService.updateInvitationMarketingManagement(marketingManagementDTO, request);
        } catch (BizException e) {
            log.error("update marketingManagement failed, marketingManagementDTO: {}, error message:{}, error all:{}", marketingManagementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PassToken
    @PutMapping("/updateActivityState/{id}")
    @ApiOperation(value = "修改营销码活动状态", notes = "修改营销码活动状态")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateActivityState(@PathVariable("id") Long id, @RequestBody MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) {
        log.info("修改营销码活动状态:{}, marketingManagement DTO:{}", id, marketingManagementDTO);
        try {
            marketingManagementService.updateMarketingManagementActivityState(id, marketingManagementDTO, request);
        } catch (Exception e) {
            log.error("修改营销码活动状态, marketingManagementDTO: {}, error message:{}, error all:{}", marketingManagementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PassToken
    @PutMapping("/updateNumber/{id}")
    @ApiOperation(value = "修改营销券个数", notes = "修改营销券个数")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateNumber(@PathVariable("id") Long id, @RequestBody MarketingManagementDTO marketingManagementDTO, HttpServletRequest request) {
        log.info("put updateNumber id:{}, marketingManagement DTO:{}", id, marketingManagementDTO);
        try {

            marketingManagementService.updateNumber(id, marketingManagementDTO, request);
        } catch (BizException e) {
            log.error("updateNumber failed, marketingManagementDTO: {}, error message:{}, error all:{}", marketingManagementDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }



}
