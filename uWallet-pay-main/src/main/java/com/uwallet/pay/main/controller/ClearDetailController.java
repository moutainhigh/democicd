package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.ClearDetailDTO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.dto.WholeSalesFlowDTO;
import com.uwallet.pay.main.model.entity.ClearDetail;
import com.uwallet.pay.main.service.ClearDetailService;
import com.uwallet.pay.main.service.QrPayFlowService;
import com.uwallet.pay.main.service.TipFlowService;
import com.uwallet.pay.main.service.WholeSalesFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * <p>
 * 清算表生成
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 清算表生成
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:53:36
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/clearDetail")
@Slf4j
@Api("清算表生成")
public class ClearDetailController extends BaseController<ClearDetail> {

    @Autowired
    private ClearDetailService clearDetailService;

    @Autowired
    private QrPayFlowService qrPayFlowService;

    @Autowired
    private WholeSalesFlowService wholeSalesFlowService;

    @Resource
    private TipFlowService tipFlowService;


    @ActionFlag(detail = "liquidationRecord_list")
    @ApiOperation(value = "分页查询清算表生成", notes = "分页查询清算表生成以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-清算表生成列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = clearDetailService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = clearDetailService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询清算表生成", notes = "通过id查询清算表生成")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get clearDetail Id:{}", id);
        return R.success(clearDetailService.findClearDetailById(id));
    }

    @ApiOperation(value = "通过查询条件查询清算表生成一条数据", notes = "通过查询条件查询清算表生成一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询清算表生成一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get clearDetail findOne params:{}", params);
        int total = clearDetailService.count(params);
        if (total > 1) {
            log.error("get clearDetail findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ClearDetailDTO clearDetailDTO = null;
        if (total == 1) {
            clearDetailDTO = clearDetailService.findOneClearDetail(params);
        }
        return R.success(clearDetailDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增清算表生成", notes = "新增清算表生成")
    public Object create(@RequestBody ClearDetailDTO clearDetailDTO, HttpServletRequest request) {
        log.info("add clearDetail DTO:{}", clearDetailDTO);
        try {
            clearDetailService.saveClearDetail(clearDetailDTO, request);
        } catch (BizException e) {
            log.error("add clearDetail failed, clearDetailDTO: {}, error message:{}, error all:{}", clearDetailDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PutMapping("/{id}")
    @ApiOperation(value = "修改清算表生成", notes = "修改清算表生成")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ClearDetailDTO clearDetailDTO, HttpServletRequest request) {
        log.info("put modify id:{}, clearDetail DTO:{}", id, clearDetailDTO);
        try {
            clearDetailService.updateClearDetail(id, clearDetailDTO, request);
        } catch (BizException e) {
            log.error("update clearDetail failed, clearDetailDTO: {}, error message:{}, error all:{}", clearDetailDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除清算表生成", notes = "删除清算表生成")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete clearDetail, id:{}", id);
        try {
            clearDetailService.logicDeleteClearDetail(id, request);
        } catch (BizException e) {
            log.error("delete failed, clearDetail id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


//    @ActionFlag(detail = "merchantClear_list")
    @ApiOperation(value = "商户结算记录列表查询", notes = "商户结算记录列表查询")
    @GetMapping(value = "/clearedDetailList", name="商户结算记录列表查询")
    public Object clearedDetailList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = clearDetailService.getClearedDetailListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = clearDetailService.getClearedDetailList(params, scs, pc);
        }
        return R.success(list, pc);
    }

//    @ActionFlag(detail = "merchantClear_list")
    /*@ApiOperation(value = "商户结算记录明细查询", notes = "商户结算记录明细查询")
    @GetMapping(value = "/clearedDetailList/{id}/{type}", name="商户结算记录明细查询")
    public Object clearedDetailTransFlowList(@PathVariable("id") Long id, @PathVariable("tyoe")Integer type,HttpServletRequest request) {

        PagingContext pc;
        Vector<SortingContext> scs;
        List<ClearDetailDTO> list = new ArrayList<>();

        if(type == StaticDataEnum.CLEAR_TYPE_0.getCode() || type == StaticDataEnum.CLEAR_TYPE_3.getCode()){
            List<QrPayFlowDTO> list = qrPayFlowService.clearedDetailTransFlowList(id);

            if (total > 0) {
                scs = getSortingContext(request);
                list = clearDetailService.getClearedDetailList(params, scs, pc);
            }
            return R.success(list, pc);

        }else if(type == StaticDataEnum.CLEAR_TYPE_2.getCode()){
            List<WholeSalesFlowDTO> list = wholeSalesFlowService.clearedDetailTransFlowList(id);
            return R.success(list, pc);
        }else{
            return R.success(list, pc);
        }
    }*/


    @ApiOperation(value = "商户结算记录列表查询", notes = "商户结算记录列表查询")
    @GetMapping(value = "/showClearDetailList/{id}/{type}", name="商户结算记录列表查询")
    public Object showClearDetailList(@PathVariable("id") Long id, @PathVariable("type")Integer type,HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        params.put("id",id);
        if(type == StaticDataEnum.CLEAR_TYPE_0.getCode() || type == StaticDataEnum.CLEAR_TYPE_3.getCode()){
            //正常出售
            Vector<SortingContext> scs;
            Integer count = qrPayFlowService.clearedDetailTransFlowCount(id);
            PagingContext pc = getPagingContext(request, count);
            List<QrPayFlowDTO> list = new ArrayList<>();
            if (count > 0) {
                scs = getSortingContext(request);
                list = qrPayFlowService.clearedDetailTransFlowList(params, scs, pc);
            }
            if(CollectionUtils.isEmpty(list)){
                return R.success(Collections.emptyList(), pc);

            }else {
                return R.success(list, pc);

            }
        }else if(type == StaticDataEnum.CLEAR_TYPE_2.getCode()){
            Vector<SortingContext> scs;
            Integer count = wholeSalesFlowService.clearedDetailTransFlowCount(id);
            PagingContext pc = getPagingContext(request, count);
            List<WholeSalesFlowDTO> list = new ArrayList<>();
            if (count > 0) {
                scs = getSortingContext(request);
                list = wholeSalesFlowService.clearedDetailTransFlowList(params, scs, pc);
            }
            if(CollectionUtils.isEmpty(list)){
                return R.success(Collections.emptyList(), pc);
            }else {
                return R.success(list, pc);
            }
        }else if(type == StaticDataEnum.CLEAR_TYPE_5.getCode()){
            //正常出售
            Vector<SortingContext> scs;
            Integer count = tipFlowService.clearedDetailTransFlowCount(id);
            PagingContext pc = getPagingContext(request, count);
            List<QrPayFlowDTO> list = new ArrayList<>();
            if (count > 0) {
                scs = getSortingContext(request);
                list = tipFlowService.clearedDetailTransFlowList(params, scs, pc);
            }
            if(CollectionUtils.isEmpty(list)){
                return R.success(Collections.emptyList(), pc);

            }else {
                return R.success(list, pc);
            }
        }else if(type == StaticDataEnum.CLEAR_TYPE_6.getCode()){
            //正常出售
            Vector<SortingContext> scs;
            List<QrPayFlowDTO> countList = clearDetailService.h5ClearCount(id);
            Integer count = countList == null ? 0 : countList.size();
            PagingContext pc = getPagingContext(request, count);
            List<QrPayFlowDTO> list = null;
            if (count > 0) {

                StringBuffer transNos = new StringBuffer();
                for(QrPayFlowDTO qrPayFlowDTO : countList ){
                    if(qrPayFlowDTO.getTransNo() != null ){
                        transNos.append("\"").append(qrPayFlowDTO.getTransNo()).append("\"").append(",");
                    }
                }
                ClearDetailDTO clearDetailDTO = clearDetailService.findClearDetailById(id);
                params.clear();
                params.put("settlementDate",clearDetailDTO.getCreatedDate());
                params.put("transNos",transNos.toString().substring(0, transNos.toString().length() - 1) );
                scs = getSortingContext(request);
                list = qrPayFlowService.h5ClearTransDetail(params, scs, pc);
            }
            if(CollectionUtils.isEmpty(list)){
                return R.success(Collections.emptyList(), pc);
            }else {
                return R.success(list, pc);
            }

        }else {
            PagingContext pc = getPagingContext(request, 0);
            return R.success(Collections.emptyList(), pc);
        }
    }

}
