package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.model.dto.NfcCodeInfoDTO;
import com.uwallet.pay.main.model.entity.NfcCodeInfo;
import com.uwallet.pay.main.service.NfcCodeInfoService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;


/**
 * <p>
 * NFC信息、绑定表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: NFC信息、绑定表
 * @author: zhoutt
 * @date: Created in 2020-03-23 14:31:21
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/nfcCodeInfo")
@Slf4j
@Api("NFC信息、绑定表")
public class NfcCodeInfoController extends BaseController<NfcCodeInfo> {

    @Autowired
    private NfcCodeInfoService nfcCodeInfoService;

    @Value("${spring.nfcTemplateUrl}")
    private String nfcTemplateUrl;

    @ActionFlag(detail = "NfcCodeInfo_list")
    @ApiOperation(value = "分页查询NFC信息、绑定表", notes = "分页查询NFC信息、绑定表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-NFC信息、绑定表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = nfcCodeInfoService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<NfcCodeInfoDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            log.info("scs:"+scs);
            list = nfcCodeInfoService.findList(params, scs, pc);
        }
        params.clear();
        params.put("state", StaticDataEnum.QRCODE_STATE_1.getCode());
        //查询已绑定总条数
        int count = nfcCodeInfoService.count(params);
        Map<String, Object> res = new HashMap<>(1);
        res.put("list", list);
        res.put("count", count);
        return R.success(res, pc);
    }

    @ApiOperation(value = "通过id查询NFC信息、绑定表", notes = "通过id查询NFC信息、绑定表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name = "详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get nfcCodeInfo Id:{}", id);
        return R.success(nfcCodeInfoService.findNfcCodeInfoById(id));
    }

    @ApiOperation(value = "通过查询条件查询NFC信息、绑定表一条数据", notes = "通过查询条件查询NFC信息、绑定表一条数据")
    @GetMapping(value = "/findOne", name = "通过查询条件查询NFC信息、绑定表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get nfcCodeInfo findOne params:{}", params);
        int total = nfcCodeInfoService.count(params);
        if (total > 1) {
            log.error("get nfcCodeInfo findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        NfcCodeInfoDTO nfcCodeInfoDTO = null;
        if (total == 1) {
            nfcCodeInfoDTO = nfcCodeInfoService.findOneNfcCodeInfo(params);
        }
        return R.success(nfcCodeInfoDTO);
    }

    @ActionFlag(detail = "NfcCodeInfo_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增NFC信息、绑定表", notes = "新增NFC信息、绑定表")
    public Object create(@RequestBody NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) {
        log.info("add nfcCodeInfo DTO:{}", nfcCodeInfoDTO);
        try {
            nfcCodeInfoService.saveNfcCodeInfo(nfcCodeInfoDTO, request);
        } catch (BizException e) {
            log.error("add nfcCodeInfo failed, nfcCodeInfoDTO: {}, error message:{}, error all:{}", nfcCodeInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "NfcCodeInfo_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改NFC信息、绑定表", notes = "修改NFC信息、绑定表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) {
        log.info("put modify id:{}, nfcCodeInfo DTO:{}", id, nfcCodeInfoDTO);
        try {
            nfcCodeInfoService.updateNfcCodeInfo(id, nfcCodeInfoDTO, request);
        } catch (BizException e) {
            log.error("update nfcCodeInfo failed, nfcCodeInfoDTO: {}, error message:{}, error all:{}", nfcCodeInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "NfcCodeInfo_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除NFC信息、绑定表", notes = "删除NFC信息、绑定表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete nfcCodeInfo, id:{}", id);
        try {
            nfcCodeInfoService.logicDeleteNfcCodeInfo(id, request);
        } catch (BizException e) {
            log.error("delete failed, nfcCodeInfo id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping("/bind/{id}")
    @ApiOperation(value = "NFC绑定", notes = "NFC绑定")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object bind(@PathVariable("id") Long id, @RequestBody NfcCodeInfoDTO nfcCodeInfoDTO, HttpServletRequest request) {
        log.info("put bind id:{}, nfcCodeInfo DTO:{}", id, nfcCodeInfoDTO);
        try {
            //查询商户信息和店长信息
            nfcCodeInfoDTO = nfcCodeInfoService.checkMerchant(id, nfcCodeInfoDTO, request);
            nfcCodeInfoService.updateNfcCodeInfo(id, nfcCodeInfoDTO, request);
        } catch (BizException e) {
            log.error("bind nfcCodeInfo failed, nfcCodeInfoDTO: {}, error message:{}, error all:{}", nfcCodeInfoDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/removeBind/{id}")
    @ApiOperation(value = "解绑", notes = "解绑")
    @ApiImplicitParam(name = "removeBind", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object removeBind(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("put removeBind id:{}, nfcCodeInfo DTO:{}", id);
        try {
            NfcCodeInfoDTO nfcCodeInfoDTO = nfcCodeInfoService.getRemoveBind(null,null,id, request);
            nfcCodeInfoService.removeBindNfcCodeInfo(id, nfcCodeInfoDTO, request);
        } catch (BizException e) {
            log.error("removeBind nfcCodeInfo failed,  error message:{}, error all:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();

    }

    @ApiOperation(value = "导入nfc", notes = "导入nfc")
    @PostMapping("/importNfc")
    public Object importNfc(@RequestBody MultipartFile multipartFile, HttpServletRequest request) {
        try {
            nfcCodeInfoService.importNfc(multipartFile, request);
        } catch (Exception e) {
            log.error("import nfc failed, e:{}, error message:{}", e, e.getMessage());
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "nfc模板下载", notes = "nfc模板下载")
    @PostMapping("/nfcTemplate")
    public void nfcTemplate(HttpServletRequest request, HttpServletResponse response) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File f = new File(nfcTemplateUrl);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-control", "private");
            response.setHeader("Cache-Control", "maxage=3600");
            response.setHeader("Pragma", "public");
            response.setContentType("application/vnd.ms-excel");
            response.setContentType("application/octet-stream;charset=UTF-8;");
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-disposition", "attachment; filename=nfc_template.xlsx");
            ServletOutputStream out = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(f));
            bos = new BufferedOutputStream(out);
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bis.close();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}