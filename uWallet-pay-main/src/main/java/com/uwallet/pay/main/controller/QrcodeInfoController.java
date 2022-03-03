package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.QrcodeInfoDTO;
import com.uwallet.pay.main.model.dto.QrcodeListDTO;
import com.uwallet.pay.main.model.entity.QrcodeInfo;
import com.uwallet.pay.main.service.QrcodeInfoService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * <p>
 * 二维码信息、绑定
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 二维码信息、绑定
 * @author: baixinyue
 * @date: Created in 2019-12-10 14:39:07
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/qrcodeInfo")
@Slf4j
@Api("二维码信息、绑定")
public class QrcodeInfoController extends BaseController<QrcodeInfo> {

    //aaaa
    @Autowired
    private QrcodeInfoService qrcodeInfoService;

    private final static String ZIP_FILE = "QRCode.zip";

    @Value("${spring.qrCodePath}")
    private String qrCodePath;

    @ApiOperation(value = "分页查询二维码信息、绑定", notes = "分页查询二维码信息、绑定以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-二维码信息、绑定列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrcodeInfoService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrcodeInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrcodeInfoService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询二维码信息、绑定", notes = "通过id查询二维码信息、绑定")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get qrcodeInfo Id:{}", id);
        return R.success(qrcodeInfoService.findQrcodeInfoById(id));
    }

    @ApiOperation(value = "通过查询条件查询二维码信息、绑定一条数据", notes = "通过查询条件查询二维码信息、绑定一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询二维码信息、绑定一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get qrcodeInfo findOne params:{}", params);
        int total = qrcodeInfoService.count(params);
        if (total > 1) {
            log.error("get qrcodeInfo findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        QrcodeInfoDTO qrcodeInfoDTO = null;
        if (total == 1) {
            qrcodeInfoDTO = qrcodeInfoService.findOneQrcodeInfo(params);
        }
        return R.success(qrcodeInfoDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增二维码信息、绑定", notes = "新增二维码信息、绑定")
    public Object create(@RequestBody QrcodeInfoDTO qrcodeInfoDTO, HttpServletRequest request) {
        log.info("add qrcodeInfo DTO:{}", qrcodeInfoDTO);
        try {
            qrcodeInfoService.saveQrcodeInfo(qrcodeInfoDTO, request);
        } catch (BizException e) {
            log.error("add qrcodeInfo failed, qrcodeInfoDTO: {}, error message:{}, error all:{}", qrcodeInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改二维码信息、绑定", notes = "修改二维码信息、绑定")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody QrcodeInfoDTO qrcodeInfoDTO, HttpServletRequest request) {
        log.info("put modify id:{}, qrcodeInfo DTO:{}", id, qrcodeInfoDTO);
        try {
            qrcodeInfoService.updateQrcodeInfo(id, qrcodeInfoDTO, request);
        } catch (BizException e) {
            log.error("update qrcodeInfo failed, qrcodeInfoDTO: {}, error message:{}, error all:{}", qrcodeInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    //    @ActionFlag(detail = "QrcodeInfo_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除二维码信息、绑定", notes = "删除二维码信息、绑定")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete qrcodeInfo, id:{}", id);
        try {
            qrcodeInfoService.logicDeleteQrcodeInfo(id, request);
        } catch (BizException e) {
            log.error("delete failed, qrcodeInfo id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "code_list")
    @ApiOperation(value = "分页查询二维码信息、绑定", notes = "分页查询二维码信息、绑定以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantNo", value = "商户号", paramType = "query", type = "string", required = false),
            @ApiImplicitParam(name = "merchantName", value = "商户名", paramType = "query", type = "string", required = false),
            @ApiImplicitParam(name = "state", value = "是否关联商户", paramType = "query", type = "int", required = false),
            @ApiImplicitParam(name = "start", value = "关联时间区间开始", paramType = "query", type = "long", required = false),
            @ApiImplicitParam(name = "end", value = "关联时间区间结束", paramType = "query", type = "long", required = false),
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/list")
    public Object findQRCodeList(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = qrcodeInfoService.findQRCodeListCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<QrcodeListDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = qrcodeInfoService.findQRCodeList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PostMapping("/qrCodeCreate/{createNumber}")
    @ApiOperation(value = "批量生成二维码", notes = "批量生成二维码")
    @ApiImplicitParam(name = "createNumber", value = "生成数量", dataType = "int", paramType = "path", required = true)
    public Object qrCodeCreate(@PathVariable("createNumber") int createNumber, HttpServletRequest request) {
        if (createNumber > StaticDataEnum.QRCODE_CREATE_LIMIT.getCode()) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("超过创建上限", getLang(request)));
        }
        log.info("create qrcode");
        try {
            qrcodeInfoService.qrCodeCreate(createNumber, request);
        } catch (Exception e) {
            log.error("create qrcode failed");
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }

        return R.success();
    }

    @PostMapping("/downLoadQRCode")
    @ApiOperation(value = "下载二维码", notes = "下载二维码")
    public void downLoadQRCode(@RequestBody Long[] ids, HttpServletRequest request, HttpServletResponse response) {
        String zipFilePath = qrCodePath + ZIP_FILE;
        //获取二维码图片并压缩下载
        BufferedOutputStream bos;
        ZipOutputStream zos;
        //创建临时压缩文件
        try {
            //获取二维码
            List<String> pathList = qrcodeInfoService.qrCodeDownLoad(ids, request);
            //创建输出流
            bos = new BufferedOutputStream(new FileOutputStream(zipFilePath));
            zos = new ZipOutputStream(bos);
            //将所有二维码写入zip文件
            ZipEntry zipEntry = null;
            for (String path : pathList) {
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
        } catch (Exception e) {
            log.error("qrcode download failed, error message:{}, error all:{}", e.getMessage(), e);
            e.printStackTrace();
        }

        //下面将临时压缩文件输出
        //获取浏览器代理信息，并设置浏览器响应编码格式
        String userAgent = request.getHeader("USER-AGENT");
        String finalFileName = null;
        try {
            if (StringUtils.contains(userAgent, "MSIE")||StringUtils.contains(userAgent,"Trident")) {
                //IE
                finalFileName = URLEncoder.encode(ZIP_FILE,"UTF8");
            } else if (StringUtils.contains(userAgent, "Mozilla")) {
                //Chrome、火狐
                finalFileName = new String(ZIP_FILE.getBytes(), "ISO8859-1");
            } else {
                //其他
                finalFileName = URLEncoder.encode(ZIP_FILE,"UTF8");
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

    @ApiOperation(value = "商户绑定二维码", notes = "商户绑定二维码")
    @PostMapping("/merchantBindingQRCode")
    public Object merchantBindingQRCode(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        log.info("merchant binding QR code, data:{}", requestInfo);
        try {
            qrcodeInfoService.merchantBindingQRCode(requestInfo, request);
        } catch (Exception e) {
            log.info("merchant binding QR code failed, data:{}, error message:{}, e:{}", requestInfo, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("binding.qrcode.failed", getLang(request)));
        }
        return R.success();
    }


    /**
     * 后台二维码 解绑
     * @param qrCode 二维码编号
     * @param request
     * @return
     */
    @ApiOperation(value = "二维码解绑", notes = "二维码解绑")
    @PostMapping("/removeQrcodeBind/{qrCode}")
    public Object removeBindQrcode(@PathVariable("qrCode") String qrCode, HttpServletRequest request) {
        log.info("remove Qrcode Bind qrCode:{}", qrCode);
        R successResult = R.success();
        try{
            //remove bind QRcode
            qrcodeInfoService.unbindQrCode(qrCode, request);
        }catch (BizException e){
            log.error("remove Qrcode Bind qrCode,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return successResult;
    }


    /**
     * 后台二维码绑定商户
     * @param jsonObject
     * @param request
     * @return
     */
    @ApiOperation(value = "二维码绑定商户", notes = "二维码绑定商户")
    @PostMapping("/qrCodeBindMerchant")
    public Object qrCodeBindMerchant(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        log.info("后台 二维码绑定商户 接口, merchantId:{}, qrCode:{}", jsonObject);
        try{
            qrcodeInfoService.bindQrCode(jsonObject.getString("qrCode"),jsonObject.getLong("merchantId"), request);
        }catch (Exception e){
            log.error("后台 二维码绑定商户 接口,error message:{},e:{}",e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }


    /**
     * 获取商户二维码列表  参数 merchantId 商户ID
     * @param request
     * @return
     */
    @GetMapping("/getMerchantQrList/{merchantId}")
    public Object getMerchantQrList(@PathVariable("merchantId") String merchantId, HttpServletRequest request) {
//        Map<String, Object> params = getConditionsMap(request);
//        String merchantId = (String) params.get("merchantId");
        if(Objects.isNull(merchantId)){
            //参数校验
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("parameters.error", getLang(request)));
        }
        //根据merchantId 查询总数 merchantId
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("merchantId", merchantId);
        int total = qrcodeInfoService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        pc = getPagingContext(request, total);
        List<QrcodeListDTO> qrcodeListDTOS = Collections.emptyList();
        if (total > 0) {
            scs = getSortingContext(request);
            //查询数据列表
            qrcodeListDTOS = qrcodeInfoService.listMerchantQrList(params, scs, pc);
        }
        return R.success(qrcodeListDTOS, pc);
    }


}
