package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.DonationFlowClearDTO;
import com.uwallet.pay.main.model.dto.DonationFlowDTO;
import com.uwallet.pay.main.model.dto.DonationUserListDTO;
import com.uwallet.pay.main.model.entity.DonationFlow;
import com.uwallet.pay.main.service.DonationFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p>
 * 捐赠流水
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 捐赠流水
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:37:50
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/donationFlow")
@Slf4j
@Api("捐赠流水")
public class DonationFlowController extends BaseController<DonationFlow> {

    @Autowired
    private DonationFlowService donationFlowService;
    @Value("${export.excel.marketingActivities}")
    private String marketingActivities;
    @Resource
    private RedisUtils redisUtils;

    @ActionFlag(detail = "DonationFlow_list")
    @ApiOperation(value = "分页查询捐赠流水", notes = "分页查询捐赠流水以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-捐赠流水列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = donationFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<DonationFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = donationFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询捐赠流水", notes = "通过id查询捐赠流水")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get donationFlow Id:{}", id);
        return R.success(donationFlowService.findDonationFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询捐赠流水一条数据", notes = "通过查询条件查询捐赠流水一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询捐赠流水一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get donationFlow findOne params:{}", params);
        int total = donationFlowService.count(params);
        if (total > 1) {
            log.error("get donationFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        DonationFlowDTO donationFlowDTO = null;
        if (total == 1) {
            donationFlowDTO = donationFlowService.findOneDonationFlow(params);
        }
        return R.success(donationFlowDTO);
    }

    @ActionFlag(detail = "DonationFlow_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增捐赠流水", notes = "新增捐赠流水")
    public Object create(@RequestBody DonationFlowDTO donationFlowDTO, HttpServletRequest request) {
        log.info("add donationFlow DTO:{}", donationFlowDTO);
        try {
            donationFlowService.saveDonationFlow(donationFlowDTO, request);
        } catch (BizException e) {
            log.error("add donationFlow failed, donationFlowDTO: {}, error message:{}, error all:{}", donationFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "DonationFlow_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改捐赠流水", notes = "修改捐赠流水")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody DonationFlowDTO donationFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, donationFlow DTO:{}", id, donationFlowDTO);
        try {
            donationFlowService.updateDonationFlow(id, donationFlowDTO, request);
        } catch (BizException e) {
            log.error("update donationFlow failed, donationFlowDTO: {}, error message:{}, error all:{}", donationFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "DonationFlow_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除捐赠流水", notes = "删除捐赠流水")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete donationFlow, id:{}", id);
        try {
            donationFlowService.logicDeleteDonationFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, donationFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    @ActionFlag(detail = "DonationFlow_list")
    @PostMapping("/getDonationOrderList")
    @ApiOperation(value = "查询用户捐赠订单列表", notes = "查询用户捐赠订单列表")
    public Object getDonationOrderList( @RequestBody JSONObject param, HttpServletRequest request) {
        log.info("查询用户捐赠订单列表, Param:{}", param);
        Map<String, Object> params = getConditionsMap(request);
        int total = donationFlowService.countOrderByUserId(param);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<JSONObject> list = new ArrayList<>();
        pc = getPagingContext(param, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = donationFlowService.findOrderByUserId(param, scs, pc);
        }
        return R.success(list, pc);
    }
    @ActionFlag(detail = "DonationFlow_list")
    @PostMapping("/updateSettlementState")
    @ApiOperation(value = "转换结算状态", notes = "转换结算状态")
    public Object updateSettlementState(@RequestBody JSONObject param, HttpServletRequest request){
        log.info("转换结算状态,data:{}",param);
        donationFlowService.updateSettlementState(param.getJSONObject("data"),request);
        return R.success();
    }
    @ActionFlag(detail = "DonationFlow_list")
    @PostMapping("/exportUserOrder")
    @ApiOperation(value = "导出明细", notes = "导出明细")
    public void exportUserOrder(@RequestBody DonationFlowClearDTO param,HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        log.info("导出明细,data:{}",param);
//        JSONObject param=new JSONObject();
//        param.put("userIdList",userIdList);
        if (param==null){
            return;
        }
        Workbook workbook = donationFlowService.exportUserOrder(param, request);
        if(workbook == null) {
            return;
        }
        response.reset();
        String dateStr =null;
        try {
            dateStr = URLEncoder.encode(marketingActivities, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        boolean donation_all_export = redisUtils.hasKey("donation_all_export");
        int number=1;
        if (donation_all_export){
            Object donation_all = redisUtils.get("donation_all_export");
            if (donation_all!=null){
                number=Integer.parseInt(donation_all.toString());
                if (number==10000){
                   number=1;
                }else {
                    number++;
                }
            }
        }
        SimpleDateFormat sm=new SimpleDateFormat("HHmmssddMMyyyy");
        redisUtils.set("donation_all_export",number);
        String fileName="donation_all_"+(sm.format(System.currentTimeMillis()))+"_"+number+".xlsx";
        // 指定下载的文件名--设置响应头
        response.setHeader("Content_Disposition", fileName);
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.addHeader("FileName", fileName);
        // 写出数据输出流到页面
        try {
            OutputStream output = response.getOutputStream();
            BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
            workbook.write(bufferedOutPut);
            bufferedOutPut.flush();
            bufferedOutPut.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ActionFlag(detail = "DonationFlow_list")
    @ApiOperation(value = "分页查询捐赠流水", notes = "分页查询捐赠流水以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(value = "/getUserDonationList",name = "查询-捐赠流水列表")
    public Object getUserDonationList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = donationFlowService.getUserDonationListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        Map<String,Object> result = new HashMap<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            result = donationFlowService.getUserDonationList(params, scs, pc);
        }else{
            result.put("list",null);
            result.put("totalUnSettledCount",0);
            result.put("totalUnSettledAmount", BigDecimal.ZERO);
            result.put("totalSettledCount",0);
            result.put("totalSettledAmount", BigDecimal.ZERO);
            result.put("totalDelayedSettledCount",0);
            result.put("totalDelayedSettledAmount", BigDecimal.ZERO);
        }
        return R.success(result, pc);
    }


    @ApiOperation(value = "用户结算", notes = "分页查询捐赠流水以及排序功能")
    @PostMapping(value = "/clear",name = "查询-捐赠流水列表")
    public Object clear(@RequestBody DonationFlowClearDTO donationFlowClearDTO , HttpServletRequest request) {
        log.info("clear donationFlow, requestInfo:{}", donationFlowClearDTO);
        try {
            donationFlowService.clear(donationFlowClearDTO, request);
        } catch (Exception e) {
            log.error("clear failed, requestInfo: {}, error message:{}, error all:{}", donationFlowClearDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
}
