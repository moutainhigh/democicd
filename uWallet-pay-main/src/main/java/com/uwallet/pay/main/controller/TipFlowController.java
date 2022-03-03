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
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.entity.TipFlow;
import com.uwallet.pay.main.service.DonationFlowService;
import com.uwallet.pay.main.service.TipClearFileRecordService;
import com.uwallet.pay.main.service.TipFlowService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * <p>
 * 小费流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 小费流水表
 * @author: zhangzeyuan
 * @date: Created in 2021-08-10 16:01:03
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@RestController
@RequestMapping("/tipFlow")
@Slf4j
@Api("小费流水表")
public class TipFlowController extends BaseController<TipFlow> {

    @Autowired
    private TipFlowService tipFlowService;
    @Resource
    private RedisUtils redisUtils;
    @Value("${export.excel.marketingActivities}")
    private String marketingActivities;
    @Autowired
    private TipClearFileRecordService tipClearFileRecordService;
    private final static String ZIP_FILE = "tip_settlement.zip";

    @Value("${spring.qrCodePath}")
    private String qrCodePath;

    @ActionFlag(detail = "TipFlow_list")
    @ApiOperation(value = "分页查询小费流水表", notes = "分页查询小费流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-小费流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = tipFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TipFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tipFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询小费流水表", notes = "通过id查询小费流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get tipFlow Id:{}", id);
        return R.success(tipFlowService.findTipFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询小费流水表一条数据", notes = "通过查询条件查询小费流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询小费流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get tipFlow findOne params:{}", params);
        int total = tipFlowService.count(params);
        if (total > 1) {
            log.error("get tipFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        TipFlowDTO tipFlowDTO = null;
        if (total == 1) {
            tipFlowDTO = tipFlowService.findOneTipFlow(params);
        }
        return R.success(tipFlowDTO);
    }

    @ActionFlag(detail = "TipFlow_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增小费流水表", notes = "新增小费流水表")
    public Object create(@RequestBody TipFlowDTO tipFlowDTO, HttpServletRequest request) {
        log.info("add tipFlow DTO:{}", tipFlowDTO);
        try {
            tipFlowService.saveTipFlow(tipFlowDTO, request);
        } catch (BizException e) {
            log.error("add tipFlow failed, tipFlowDTO: {}, error message:{}, error all:{}", tipFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "TipFlow_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改小费流水表", notes = "修改小费流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody TipFlowDTO tipFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, tipFlow DTO:{}", id, tipFlowDTO);
        try {
            tipFlowService.updateTipFlow(id, tipFlowDTO, request);
        } catch (BizException e) {
            log.error("update tipFlow failed, tipFlowDTO: {}, error message:{}, error all:{}", tipFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "TipFlow_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除小费流水表", notes = "删除小费流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete tipFlow, id:{}", id);
        try {
            tipFlowService.logicDeleteTipFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, tipFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }



    @PassToken
    @ApiOperation(value = "小费结算", notes = "小费结算")
    @GetMapping(name = "小费结算", value = "/tipsSettlement")
    public Object tipsSettlement(@RequestParam(value = "merchantIds") String merchantIds, HttpServletRequest request, HttpServletResponse response) {

        if(StringUtils.isBlank(merchantIds)){
            return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), "params error");
        }

        try {
            //生成结算文件
            ClearBatchDTO clearBatchDTO = tipFlowService.tipSettlement(merchantIds, request);

            //导出文件
            File file;
            FileInputStream fis;
            BufferedInputStream bis = null;
            try {

                file = new File(clearBatchDTO.getUrl());
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), "params error");
                }
                byte[] b = new byte[1024];
                String fileName = URLEncoder.encode(file.getName(), "UTF-8");
                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                response.setContentType("application/download;charset=UTF-8");
                response.setContentType("application/csv;charset=UTF-8");
                response.setHeader("FileName", fileName  );
                response.setHeader("Access-Control-Expose-Headers", "FileName");
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(b);
                while (i != -1) {
                    os.write(b, 0, i);
                    i = bis.read(b);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (BizException e) {
            return R.fail(ErrorCodeEnum.HTTP_SEND_ERROR.getCode(), e.getMessage());
        }
       return null;
    }
    @ActionFlag(detail = "TipFlow_list")
    @ApiOperation(value = "分页查询商户小费表", notes = "分页查询商户小费表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(value = "/getMerchantTip",name = "查询-小费流水表列表")
    public Object getMerchantTip(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = tipFlowService.countTipMerchant(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TipMerchantsDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tipFlowService.findTipMerchantData(params, scs, pc);
        }
        return R.success(list, pc);
    }
    @ActionFlag(detail = "TipFlow_list")
    @PostMapping("/getTipOrderList")
    @ApiOperation(value = "查询用户小费订单列表", notes = "查询用户捐赠订单列表")
    public Object getTipOrderList(@RequestBody JSONObject param, HttpServletRequest request) {
        log.info("查询用户小费订单列表, Param:{}", param);
        Map<String, Object> params = getConditionsMap(request);
        int total = tipFlowService.countOrderByMerchantId(param);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<JSONObject> list = new ArrayList<>();
        pc = getPagingContext(param, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tipFlowService.findOrderByUserId(param, scs, pc);
        }
        return R.success(list, pc);
    }
    @ActionFlag(detail = "TipFlow_list")
    @PostMapping("/updateSettlementState")
    @ApiOperation(value = "转换结算状态", notes = "转换结算状态")
    public Object updateSettlementState(@RequestBody JSONObject param, HttpServletRequest request){
        log.info("转换结算状态,data:{}",param);
        tipFlowService.updateSettlementState(param.getJSONObject("data"),request);
        return R.success();
    }
    @ActionFlag(detail = "DonationFlow_list")
    @PostMapping("/exportUserOrder")
    @ApiOperation(value = "导出明细", notes = "导出明细")
    public void exportUserOrder(@RequestBody TipFlowClearDTO param, HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        log.info("导出明细,data:{}",param);
        if (param==null){
            return;
        }
        Workbook workbook = tipFlowService.exportUserOrder(param, request);
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
        boolean donation_all_export = redisUtils.hasKey("tip_all_export");
        int number=1;
        if (donation_all_export){
            Object donation_all = redisUtils.get("tip_all_export");
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
        redisUtils.set("tip_all_export",number);
        String fileName="tip_all_"+(sm.format(System.currentTimeMillis()))+"_"+number+".xlsx";
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
    @ActionFlag(detail = "TipFlow_list")
    @ApiOperation(value = "分页查询小费清算文件记录", notes = "分页查询小费清算文件记录以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-小费清算文件记录列表",value = "/getTipFileList")
    public Object getTipFileList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = tipClearFileRecordService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TipClearFileRecordDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tipClearFileRecordService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }
    @ActionFlag(detail = "DonationFlow_list")
    @PostMapping("/downloadFile")
    @ApiOperation(value = "下载历史清算文件", notes = "下载历史清算文件")
    public void downloadFile(@RequestBody  JSONObject params, HttpServletRequest request, HttpServletResponse response) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, FileNotFoundException {
        log.info("下载历史清算文件,data:{}",params);
        if (params==null||params.getJSONArray("ids")==null){
            return;
        }
        BufferedOutputStream bos;
        ZipOutputStream zos;
        // 获取文件路径
        // 生成io流
        // 写入zip
        String zipFilePath = qrCodePath+ZIP_FILE;
        String fileName=ZIP_FILE;
        JSONObject param=new JSONObject();
        param.put("ids",params.getJSONArray("ids"));
        List<TipClearFileRecordDTO> tipClearFileRecordDTOS = tipClearFileRecordService.find(param, null, null);
        if(tipClearFileRecordDTOS == null||tipClearFileRecordDTOS.size()==0) {
            return;
        }
        bos = new BufferedOutputStream(new FileOutputStream(zipFilePath));
        zos = new ZipOutputStream(bos);
        ZipEntry zipEntry = null;
        try{
            for (TipClearFileRecordDTO tipClearFileRecordDTO : tipClearFileRecordDTOS) {
                String path = tipClearFileRecordDTO.getUrl();
                File file = new File(path);
                //跳过 压缩不存在的二维码文件
                if(!file.exists())continue;
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                int s = -1;
                while ((s = bis.read()) != -1) {
                    zos.write(s);
                }
                bis.close();
            }
            zos.flush();
            zos.close();
        }catch (Exception e){
            log.error("导出历史文件异常:{}",e);
        }

        String userAgent = request.getHeader("USER-AGENT");
        String finalFileName = null;
        try {
            if (StringUtils.contains(userAgent, "MSIE")||StringUtils.contains(userAgent,"Trident")) {
                //IE
                finalFileName = URLEncoder.encode(fileName,"UTF8");
            } else if (StringUtils.contains(userAgent, "Mozilla")) {
                //Chrome、火狐
                finalFileName = new String(fileName.getBytes(), "ISO8859-1");
            } else {
                //其他
                finalFileName = URLEncoder.encode(fileName,"UTF8");
            }
        } catch (UnsupportedEncodingException e) {
            log.error("qrcode download failed, error message:{}, error all:{}", e.getMessage(), e);
            e.printStackTrace();
        }
        //告知浏览器下载文件，而不是直接打开，浏览器默认为打开
        response.setContentType("application/x-download");
        response.setHeader("Content-Disposition" ,"attachment;filename=\"" +finalFileName+ "\"");
        response.setHeader("FileName", finalFileName);
        response.setHeader("Access-Control-Expose-Headers", "FileName");
        ServletOutputStream sos = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        File reportZip = null;
        try {
            sos = response.getOutputStream();
            dos = new DataOutputStream(sos);
            dis = new DataInputStream(new FileInputStream(zipFilePath));
            byte[] b = new byte[2048];
            reportZip = new File(zipFilePath);
            while ((dis.read(b)) != -1) {
                dos.write(b);
            }
            dos.flush();
            dos.close();
            sos.flush();
            sos.close();
            dis.close();
            reportZip.delete();
        } catch (IOException e) {
            log.error("qrcode download failed, error message:{}, error all:{}", e.getMessage(), e);
            e.printStackTrace();
        }
    }

}
