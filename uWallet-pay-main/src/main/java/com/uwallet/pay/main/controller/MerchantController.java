package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.MathUtils;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.ApiMerchant;
import com.uwallet.pay.main.model.entity.Merchant;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.AmazonAwsUploadUtil;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.UploadFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;


/**
 * <p>
 * 商户信息表
 * </p>
 *
 * @package: com.uwallet.pay.main.controller
 * @description: 商户信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:22:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@RestController
@RequestMapping("/merchant")
@Slf4j
@Api("商户信息表")
public class MerchantController extends BaseController<Merchant> {

    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ApiMerchantService apiMerchantService;

    @Autowired
    private CSVService cSVService;

    @Value("${spring.csvTempFilePath}")
    private String filePath;

    @Value("${export.excel.merchantDetails}")
    private String merchantDetails;

    @Value("${spring.imgRequestHost}")
    private String imgRequestHost;


    @Value("${spring.basePath}")
    private String basePath;




    @Autowired
    private MerchantContractFileRecordService merchantContractFileRecordService;

    @ActionFlag(detail = "Merchant_list")
    @ApiOperation(value = "分页查询商户信息表", notes = "分页查询商户信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-商户信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询商户信息表", notes = "通过id查询商户信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name = "详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get merchant Id:{}", id);
        return R.success(merchantService.findMerchantById(id));
    }

    @ApiOperation(value = "通过查询条件查询商户信息表一条数据", notes = "通过查询条件查询商户信息表一条数据")
    @GetMapping(value = "/findOne", name = "通过查询条件查询商户信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get merchant findOne params:{}", params);
        int total = merchantService.count(params);
        if (total > 1) {
            log.error("get merchant findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MerchantDTO merchantDTO = null;
        if (total == 1) {
            merchantDTO = merchantService.findOneMerchant(params);
        }
        return R.success(merchantDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增商户信息表", notes = "新增商户信息表")
    public Object create(@RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("add merchant DTO:{}", merchantDTO);
        try {
            merchantService.saveMerchant(merchantDTO, request);
        } catch (BizException e) {
            log.error("add merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改商户信息表：银行信息", notes = "修改商户信息表：银行信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchant DTO:{}", id, merchantDTO);
        try {
            // 查询用户信息
            if (merchantDTO.getIsAvailable() != null) {
            if (merchantDTO.getIsAvailable() == StaticDataEnum.STATUS_1.getCode()) {
                MerchantDTO checkMerchant = merchantService.findMerchantById(id);
                if (checkMerchant.getCategories() == null) {
                    throw new BizException(I18nUtils.get("categories.is.null", getLang(request)));
                }
            }
        }
            merchantService.updateMerchant(id, merchantDTO, request);
        } catch (BizException e) {
            log.error("update merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/h5/{id}")
    @ApiOperation(value = "修改商户信息", notes = "修改商户信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateh5(@PathVariable("id") Long id, @RequestBody ApiMerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, h5merchant DTO:{}", id, merchantDTO);
        try {
            // 查询用户信息
            if (merchantDTO.getIsAvailable() != null) {
                if (merchantDTO.getIsAvailable() == StaticDataEnum.STATUS_1.getCode()) {
                    MerchantDTO checkMerchant = merchantService.findMerchantById(id);
                    if (checkMerchant.getCategories() == null) {
                        throw new BizException(I18nUtils.get("categories.is.null", getLang(request)));
                    }
                }
            }
            merchantService.updateMerchanth5(id, merchantDTO, request);
        } catch (BizException e) {
            log.error("update h5merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/update/{id}")
    @ApiOperation(value = "修改商户基础信息", notes = "修改商户基础信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateDetail(@PathVariable("id") Long id, @RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchant DTO:{}", id, merchantDTO);
        try {
            merchantService.updateDetail(id, merchantDTO, request);
        } catch (BizException e) {
            log.error("update merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/updateH5/{id}")
    @ApiOperation(value = "H5修改商户基础信息", notes = "修改商户基础信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateDetailH5(@PathVariable("id") Long id, @RequestBody ApiMerchantDTO apiMerchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchant DTO:{}", id, apiMerchantDTO);
        try {
            apiMerchantService.updateApiMerchant(id, apiMerchantDTO, request);
        } catch (BizException e) {
            log.error("update merchant failed, merchantDTO: {}, error message:{}, error all:{}", apiMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/updateDirectorAndOwner")
    @ApiOperation(value = "修改商户股东、董事", notes = "修改商户股东、董事")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateDirectorAndOwner(@RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("put updateDirectorAndOwner id:{}, merchant DTO:{}", merchantDTO.getId(), merchantDTO);
        try {
            merchantService.replenishDirectorAndOwner(merchantDTO, request);
        } catch (BizException e) {
            log.error("update updateDirectorAndOwner failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/updateH5DirectorAndOwner")
    @ApiOperation(value = "修改H5商户股东、董事", notes = "修改H5商户股东、董事")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateH5DirectorAndOwner(@RequestBody ApiMerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("put update h5 DirectorAndOwner id:{}, merchant DTO:{}", merchantDTO.getId(), merchantDTO);
        try {
            merchantService.replenishH5DirectorAndOwner(merchantDTO, request);
        } catch (BizException e) {
            log.error("update update h5 DirectorAndOwner failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除商户信息表", notes = "删除商户信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete merchant, id:{}", id);
        try {
            merchantService.logicDeleteMerchant(id, request);
        } catch (BizException e) {
            log.error("delete failed, merchant id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @ActionFlag(detail = "merchantReview_list")
    @ApiOperation(value = "分页查询商户审核列表", notes = "分页查询商户审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-商户审核列表", value = "/listMerchantApprove")
    public Object listMerchantApprove(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
//        params.put("isAvailable", StaticDataEnum.MERCHANT_AVAILABLE_0.getCode());
//        params.put("state", StaticDataEnum.MERCHANT_STATE_2.getCode());
        int total = merchantService.countMerchantApprove(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.listMerchantApprove(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "user_list")
    @ApiOperation(value = "分页查询商户列表", notes = "分页查询商户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-商户列表", value = "/listMerchant")
    public Object listMerchant(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantService.countMerchant(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.listMerchant(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "H5分页查询商户列表", notes = "H5分页查询商户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "H5查询-商户列表", value = "/listMerchantH5")
    public Object listMerchanth5(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantService.countMerchantH5(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ApiMerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.listMerchantH5(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ActionFlag(detail = "changeApproval_list")
    @ApiOperation(value = "分页查询变更审批列表", notes = "分页查询变更审批列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-变更审批列表", value = "/listChangeApprove")
    public Object listChangeApprove(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        params.put("isAvailable", StaticDataEnum.MERCHANT_AVAILABLE_1.getCode());
        params.put("accountApplyState", StaticDataEnum.ACCOUNT_APPLY_STATE_3.getCode());
        int total = merchantService.countMerchantChangeApprove(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.listMerchantChangeApprove(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "商户审核页面", notes = "商户审核页面")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "merchantApprove/{id}", name = "商户审核页面")
    public Object merchantApprove(@PathVariable("id") Long id) {
        log.info("get merchant Id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
//        params.put("state", StaticDataEnum.MERCHANT_STATE_2.getCode());
//        params.put("isAvailable", StaticDataEnum.MERCHANT_AVAILABLE_0.getCode());
        return R.success(merchantService.selectMerchantApproveById(params));
    }


    @ApiOperation(value = "商户审核页面", notes = "商户审核页面")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "merchantApproveH5/{id}", name = "商户审核页面")
    public Object merchantApproveH5(@PathVariable("id") Long id) {
        log.info("get h5 merchant Id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        return R.success(merchantService.selectMerchantApproveByIdH5(params));
    }

    @ApiOperation(value = "商户详情页面", notes = "商户详情页面")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "merchantDetail/{id}", name = "商户审核详情")
    public Object merchantDetail(@PathVariable("id") Long id) {
        log.info("get merchant Id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("merchantId", id);
        MerchantDTO merchantDTO = merchantService.findMerchantById(id);
        merchantDTO =  merchantService.getOtherMerchantMessage(merchantDTO);
        return R.success(merchantDTO);
    }

    @PutMapping("/refuse/{id}")
    @ApiOperation(value = "商户入网审核拒绝", notes = "商户入网审核拒绝")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true),
            @ApiImplicitParam(name = "remark", value = "审核结果备注", dataType = "String", required = true)
    })
    public Object refuseMerchant(@PathVariable("id") Long id, @RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("audit modify id:{}, merchant DTO:{}", id, merchantDTO);
        try {
            merchantService.refuseMerchant(id, merchantDTO, request);
        } catch (BizException e) {
            log.error("refuse merchant failed, merchantDTO: {}, error message:{}", merchantDTO, e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping(name = "商户入网审核通过", value = "/pass")
    @ApiOperation(value = "商户入网审核通过", notes = "设置费率")
    public Object passMerchant(@RequestBody MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) {
        try {
            merchantService.passMerchant(merchantDetailDTO, request);
        } catch (Exception e) {
            log.error("pass merchant failed, merchantDetailDTO: {}, error message:{}", merchantDetailDTO, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping(name = "审核页面费率修改", value = "/reviewUpdateRate")
    @ApiOperation(value = "审核页面费率修改", notes = "审核页面费率修改")
    public Object reviewUpdateRate(@RequestBody MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) {
        try {
            merchantService.reviewUpdateRate(merchantDetailDTO, request);
        } catch (BizException e) {
            log.error("reviewUpdateRate failed, merchantDetailDTO: {}, error message:{}", merchantDetailDTO, e.getMessage(),e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/replenishDirectorAndOwner")
    @ApiOperation(value = "股东、董事添加", notes = "股东、董事添加")
    public Object replenishDirectorAndOwner(@RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("app replenishDirectorAndOwner merchant DTO:{}", merchantDTO);
        try {
            merchantService.replenishDirectorAndOwner(merchantDTO, request);
        } catch (BizException e) {
            log.error("replenishDirectorAndOwner merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "通过id查询商户支付渠道信息", notes = "通过id查询商户支付渠道信息")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "route/{id}", name = "详情")
    public Object route(@PathVariable("id") Long id) {
        log.info("route merchant Id:{}", id);
        return R.success(merchantService.route(id));
    }

    @PutMapping(name = "修改费率", value = "/rate/{id}")
    @ApiOperation(value = "修改费率", notes = "修改费率")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object updateRate(@PathVariable("id") Long id, @RequestBody MerchantDetailDTO merchantDetailDTO, HttpServletRequest request) {
        log.info("updateRate modify id:{}, merchantDetail DTO:{}", id, merchantDetailDTO);
        try {
            merchantService.updateRate(id, merchantDetailDTO, request);
        } catch (BizException e) {
            log.error("updateRate merchant failed, merchantDetailDTO: {}, error message:{}", merchantDetailDTO, e.getMessage());
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ApiOperation(value = "商户变更审批页面", notes = "商户变更审批页面")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "merchantChange/{id}", name = "商户变更审批页面")
    public Object merchantChange(@PathVariable("id") Long id) {
        log.info("get merchant Id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("state", StaticDataEnum.MERCHANT_STATE_3.getCode());
        params.put("isAvailable", StaticDataEnum.MERCHANT_AVAILABLE_1.getCode());
        return R.success(merchantService.selectMerchantChange(params));
    }

    @PostMapping("/auditChange")
    @ApiOperation(value = "变更审批审核", notes = "变更审批审核")
    @ApiImplicitParam(name = "id", value = "商户ID", dataType = "Long", required = true)
    public Object auditChange(@RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("auditChange modify id:{}, merchant DTO:{}", merchantDTO.getId(), merchantDTO);
        try {
            merchantService.auditChange(merchantDTO, request);
        } catch (BizException e) {
            log.error("auditChange merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/isTop/{id}")
    @ApiOperation(value = "商家推荐", notes = "推荐")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true),
            @ApiImplicitParam(name = "isTop", value = "是否推荐", dataType = "Integer", required = true)
    })
    public Object isTop(@PathVariable("id") Long id, @RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, merchant DTO:{}", id, merchantDTO);
        try {
            merchantService.merchantTopChange(id, merchantDTO, request);
        } catch (Exception e) {
            log.error("update merchant failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PostMapping(value = "/exportCSV", name = "导出商户列表信息成CSV文件")
    @ApiOperation(value = "导出商户列表信息成CSV文件", notes = "导出商户列表信息成CSV文件")
    public void exportMerchantCSV(@RequestBody MerchantIdListDTO merchantIdList, HttpServletResponse response) throws IOException {
        // 空数据返回
        if (merchantIdList == null || merchantIdList.getIdList() == null || merchantIdList.getIdList().size() == 0) {
            return;
        }
        // 查询merchant信息
        List<ContactsFileDTO> merchantList = merchantService.selectMerchantListByIdList(merchantIdList.getIdList());

        String fileName = "Contacts.csv";
        //String path = "D://file";
        String[] headArr = {"*ContactName", "AccountNumber", "EmailAddress", "FirstName", "LastName", "POAttentionTo", "POAddressLine1", "POAddressLine2", "POAddressLine3",
                "POAddressLine4", "POCity", "PORegion", "POPostalCode", "POCountry", "SAAttentionTo", "SAAddressLine1", "SAAddressLine2", "SAAddressLine3", "SAAddressLine4",
                "SACity", "SARegion", "SAPostalCode", "SACountry", "PhoneNumber", "FaxNumber", "MobileNumber", "DDINumber", "SkypeName", "BankAccountName", "BankAccountNumber",
                "BankAccountParticulars", "TaxNumber", "AccountsReceivableTaxCodeName", "AccountsPayableTaxCodeName", "Website", "LegalName", "Discount", "CompanyNumber",
                "DueDateBillDay", "DueDateBillTerm", "DueDateSalesDay", "DueDateSalesTerm", "SalesAccount", "PurchasesAccount", "TrackingName1", "SalesTrackingOption1",
                "PurchasesTrackingOption1", "TrackingName2", "SalesTrackingOption2", "PurchasesTrackingOption2", "BrandingTheme", "DefaultTaxBills", "DefaultTaxSales",
                "Person1FirstName", "Person1LastName", "Person1Email", "Person1IncludeInEmail", "Person2FirstName", "Person2LastName", "Person2Email", "Person2IncludeInEmail",
                "Person3FirstName", "Person3LastName", "Person3Email", "Person3IncludeInEmail", "Person4FirstName", "Person4LastName", "Person4Email", "Person4IncludeInEmail",
                "Person5FirstName", "Person5LastName", "Person5Email", "Person5IncludeInEmail"};
        List<String> merchantStringList = new ArrayList<>();

        for (ContactsFileDTO contactsFileDTO : merchantList) {
            String s = contactsFileDTO.toRow();
            merchantStringList.add(s);
        }
        //生成文件
        File csvFile = cSVService.createCsvFile(fileName, filePath, headArr, merchantStringList);
        //导出文件
        cSVService.outCsvStream(response, csvFile);
        //删除文件
        cSVService.deleteFile(csvFile);
    }

    @GetMapping("/exportExcel/{id}")
    @PassToken
    @ApiOperation(value = "导出商户详细信息成Excel文件", notes = "导出商户详细信息成Excel文件")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public void exportMerchantExcel(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
        log.info("exportMerchantExcel id:{}", id);
        HSSFWorkbook workbook = null;
        try {
            workbook = merchantService.exportMerchantExcel(id, request);
        } catch (Exception e) {
            log.error("exportMerchantExcel failed, error message:{}, error all:{}", e.getMessage(), e);
        }

        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        MerchantDetailDTO merchantDetailDTO = merchantService.selectMerchantApproveById(params);

        // 重置响应对象
        response.reset();
        String dateStr = null;
        try {
            dateStr = URLEncoder.encode(merchantDetailDTO.getCorporateName() + "_detail", "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 指定下载的文件名--设置响应头
        response.setHeader("Content_Disposition", "attachment;filename=" + dateStr + ".xls");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.addHeader("FileName", dateStr);

        try {
            Sheet sheet = workbook.createSheet("sheet1");
            OutputStream output = response.getOutputStream();
            BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
            workbook.write(bufferedOutPut);
            bufferedOutPut.flush();
            bufferedOutPut.close();
            output.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @ApiOperation(value = "全部商户列表", notes = "全部商户列表")
    @GetMapping("/merchantList")
    public Object merchantList(HttpServletRequest request) {
        return R.success(merchantService.merchantList());
    }

    @ActionFlag(detail = "Merchant_list")
    @ApiOperation(value = "分页查询推荐商户信息表", notes = "分页查询推荐商户信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/topList")
    public Object topList(HttpServletRequest request) throws Exception {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantService.topCount();
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.topList(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "上移、下移操作", notes = "上移、下移操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "被操作id", paramType = "path"),
            @ApiImplicitParam(name = "upOrDown", value = "0：上移 1：下移", paramType = "path")
    })
    @PutMapping("/shift/{id}/{upOrDown}")
    public Object shiftUpOrDown(@PathVariable("id") Long id, @PathVariable("upOrDown") Integer upOrDown, HttpServletRequest request) {
        log.info("shift appBanner, id:{}", id);
        try {
            merchantService.shiftUpOrDown(id, upOrDown, request);
        } catch (BizException e) {
            log.error("shift failed, appBanner id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Merchant_list")
    @ApiOperation(value = "官网列表", notes = "官网列表")
    @PostMapping("/listOfWebsite")
    @PassToken
    public Object listOfWebsite(@RequestBody JSONObject requestInfo, HttpServletRequest request) throws Exception {
        Map<String, Object> params = requestInfo;
        int total = merchantService.listOfWebSiteCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(requestInfo, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.listOfWebSite(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @PassToken
    @ApiOperation(value = "通过id查询商户信息表", notes = "通过id查询商户信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/viewOfWebsite/{id}", name = "详情")
    public Object viewOfWebsite(@PathVariable Long id) throws Exception {
        log.info("view merchant id:{}", id);
        MerchantDTO merchantDTO = merchantService.selectMerchantById(id);
        return R.success(merchantDTO);
    }

    @ActionFlag(detail = "Merchant_list")
    @ApiOperation(value = "分页查询推荐商户信息表", notes = "分页查询推荐商户信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping("/merchantContractManage")
    public Object merchantContractManage(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = merchantService.merchantContractManageCount(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MerchantDetailDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = merchantService.merchantContractManage(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @GetMapping("/downLoadContract/{id}")
    @PassToken
    public void downLoadContract(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MerchantDTO merchantDTO = merchantService.findMerchantById(id);
        if (!StringUtils.isEmpty(merchantDTO.getDocusignFiles())) {
            JSONObject docusignFiles = JSONObject.parseObject(merchantDTO.getDocusignFiles());
            List<JSONObject> docusignFileList = new ArrayList<>(3);
            docusignFiles.keySet().stream().forEach(key -> {
                if (!key.startsWith("envelopId")) {
                    JSONObject file = new JSONObject();
                    file.put("name", key);
                    file.put("url", docusignFiles.getString(key).replaceAll(" ", "%20"));
                    docusignFileList.add(file);
                }
            });
            String outputFileName = "docusignContract.zip";
            // 设置response参数
            response.reset();
            response.setContentType("application/x-download");
//            response.setContentType("content-type:octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String((outputFileName).getBytes(), "iso-8859-1"));
            response.setHeader("filename", outputFileName);
            ServletOutputStream out = response.getOutputStream();

            ZipArchiveOutputStream zous = new ZipArchiveOutputStream(out);
            zous.setUseZip64(Zip64Mode.AsNeeded);
            for (JSONObject file : docusignFileList) {
                HttpURLConnection conn = null;
                InputStream inputStream = null;
                try {
                    URL url = new URL(imgRequestHost + file.getString("url"));
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(20 * 1000);
                    inputStream = conn.getInputStream();
                } catch (Exception e) {
                    throw e;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                if (baos != null) {
                    baos.flush();
                }
                byte[] bytes = baos.toByteArray();

                String fileName = file.getString("name") + ".pdf";
                //设置文件名
                ArchiveEntry entry = new ZipArchiveEntry(fileName);
                zous.putArchiveEntry(entry);
                zous.write(bytes);
                zous.closeArchiveEntry();
                if (baos != null) {
                    baos.close();
                }
            }
            if (zous != null) {
                zous.close();
            }
        }
    }


    /**
     * 下载合同压缩包文件
     *
     * @param id
     * @param response
     * @author zhangzeyuan
     * @date 2021/4/30 15:05
     */
    @GetMapping("/downMerchantContractFilesZip/{id}")
    @PassToken
    public void downMerchantContractFilesZip(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {
        //获取商户
        MerchantDTO merchantDTO = merchantService.findMerchantById(id);
        if (Objects.isNull(merchantDTO)) {
            return;
        }
        //获取merchant表 docusignFiles字段的文件
        String docusignFile = merchantDTO.getDocusignFiles();
        List<MerchantContractFileRecordDTO> fileList = new ArrayList<>();
        if (StringUtils.isNotBlank(docusignFile)) {
            JSONObject docusignFiles = JSONObject.parseObject(merchantDTO.getDocusignFiles());
            docusignFiles.keySet().stream().forEach(key -> {
                if (!key.startsWith("envelopId")) {
                    MerchantContractFileRecordDTO record = new MerchantContractFileRecordDTO();
                    record.setFileName(key + ".pdf");
                    record.setFilePath(docusignFiles.getString(key).replaceAll(" ", "%20"));
                    fileList.add(record);
                }
            });
        }

        //获取u_merchant_contract_file_record表该商户文件数据
        Map<String, Object> queryParam = Maps.newHashMapWithExpectedSize(2);
        queryParam.put("merchantId", id);
        queryParam.put("status", 1);
        List<MerchantContractFileRecordDTO> fileRecordList = merchantContractFileRecordService.find(queryParam, null, null);
        if (CollectionUtils.isNotEmpty(fileRecordList)) {
            fileList.addAll(fileRecordList);
        }

        if(fileList.size() < 1){
            return;
        }

        String outputFileName = "docusignContract.zip";
        // 设置response参数
        response.reset();
        response.setContentType("application/x-download");
//            response.setContentType("content-type:octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + new String((outputFileName).getBytes(), "iso-8859-1"));
        response.setHeader("filename", outputFileName);
        ServletOutputStream out = response.getOutputStream();

        ZipArchiveOutputStream zous = new ZipArchiveOutputStream(out);
        zous.setUseZip64(Zip64Mode.AsNeeded);

        HttpURLConnection conn = null;
        InputStream inputStream = null;

        for (MerchantContractFileRecordDTO fileParam : fileList) {
            try {
                URL url = new URL(imgRequestHost + fileParam.getFilePath());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(20 * 1000);
                inputStream = conn.getInputStream();
            } catch (Exception e) {
                log.error("下载合同压缩包文件出错 " + e.getMessage());
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            if (baos != null) {
                baos.flush();
            }
            byte[] bytes = baos.toByteArray();

            String fileName = fileParam.getFileName();
            //设置文件名
            ArchiveEntry entry = new ZipArchiveEntry(fileName);
            zous.putArchiveEntry(entry);
            zous.write(bytes);
            zous.closeArchiveEntry();
            if (baos != null) {
                baos.close();
            }
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
        if (zous != null) {
            zous.close();
        }
    }

    /**
     * 下载合同单个文件
     *
     * @param id
     * @param response
     * @author zhangzeyuan
     * @date 2021/4/30 14:59
     */
    /*@GetMapping("/downMerchantContractSingleFileOld/{id}")
    @PassToken
    public void downMerchantContractSingleFileOld(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //获取u_merchant_contract_file_record表该商户文件数据
        Map<String, Object> queryParam = Maps.newHashMapWithExpectedSize(1);
        queryParam.put("id", id);
        MerchantContractFileRecordDTO merchantContractFileRecord = merchantContractFileRecordService.findOneMerchantContractFileRecord(queryParam);
        if (Objects.isNull(merchantContractFileRecord) || StringUtils.isBlank(merchantContractFileRecord.getFilePath())) {
            return;
        }

        String fileName = merchantContractFileRecord.getFileName();
        //处理空格转为加号的问题
        fileName = fileName.replaceAll("\\+","%20");

        String filePath = basePath + "/paper/" + merchantContractFileRecord.getFilePath();

        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        response.setHeader("FileName", fileName);

        OutputStream os = null;
        FileInputStream fis = null;
        try {
            os = response.getOutputStream();
            fis = new FileInputStream(filePath);
            byte[] b = new byte[100];
            int c;
            while((c=fis.read(b))>0){
                os.write(b,0,c);
            }
            os.flush();
            os.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }finally{
            if(null!=fis){
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if(null!=os){
                try {
                    os.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
*/

    @GetMapping("/downMerchantContractSingleFile/{id}")
    @PassToken
    public void downMerchantContractSingleFileNew(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        //todo 中文下划线问题未解决
        //获取u_merchant_contract_file_record表该商户文件数据
        Map<String, Object> queryParam = Maps.newHashMapWithExpectedSize(1);
        queryParam.put("id", id);
        MerchantContractFileRecordDTO merchantContractFileRecord = merchantContractFileRecordService.findOneMerchantContractFileRecord(queryParam);
        if (Objects.isNull(merchantContractFileRecord) || StringUtils.isBlank(merchantContractFileRecord.getFilePath())) {
            return;
        }

        String fileName = merchantContractFileRecord.getFileName();
        //处理空格转为加号的问题
        fileName = fileName.replaceAll("\\+","%20");

        String filePath = imgRequestHost + merchantContractFileRecord.getFilePath();

        response.setCharacterEncoding("UTF-8");

        response.addHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));

//        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        response.setHeader("FileName", fileName);

        URL url = new URL(filePath);
        URLConnection conn = url.openConnection();
        InputStream inputStream = conn.getInputStream();

        byte[] buffer = new byte[1024];
        int len;
        OutputStream outputStream = response.getOutputStream();
        while ((len = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
        outputStream.close();
    }


//    @GetMapping("/downMerchantContractSingleFile111/{id}")
//    @PassToken
//    public void downMerchantContractSingleFileNew1(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
//        //获取u_merchant_contract_file_record表该商户文件数据
//        Map<String, Object> queryParam = Maps.newHashMapWithExpectedSize(1);
//        queryParam.put("id", id);
//        MerchantContractFileRecordDTO merchantContractFileRecord = merchantContractFileRecordService.findOneMerchantContractFileRecord(queryParam);
//        if (Objects.isNull(merchantContractFileRecord) || StringUtils.isBlank(merchantContractFileRecord.getFilePath())) {
//            return;
//        }
//        //文件名
//        String fileName = merchantContractFileRecord.getFileName();
//
//        String fileUrl = imgRequestHost + merchantContractFileRecord.getFilePath();
//
//        //处理文件名
//        String newFileName = "";
//        String agent = request.getHeader("USER-AGENT");
//        if (null != agent && -1 != agent.indexOf("MSIE") || null != agent
//                && -1 != agent.indexOf("Trident")) {
//            // ie
//            newFileName = java.net.URLEncoder.encode(fileName, "UTF8");
//        } else if (null != agent && -1 != agent.indexOf("Mozilla")) {
//            // 火狐,chrome等
//            newFileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
//        }
//
//        if(StringUtils.isBlank(newFileName)){
//            return;
//        }
//        response.setHeader("Content-disposition", "attachment; filename = " + newFileName);
//        response.setHeader("FileName", newFileName);
//
//        BufferedOutputStream bf = new BufferedOutputStream(response.getOutputStream());
//        BufferedInputStream in = null;
//        ByteArrayOutputStream out = null;
//        URLConnection conn = null;
//        int httpResult = 0;
//        try {
//            StringBuffer sb = new StringBuffer();
//            //处理url路径中的中文路径
//            for(int i = 0;i < fileUrl.length();i++){
//                char a = fileUrl.charAt(i);
//                if(a > 127){
//                    //将中文UTF-8编码
//                    sb.append(URLEncoder.encode(String.valueOf(a), "utf-8"));
//                }else{
//                    sb.append(String.valueOf(a));
//                }
//            }
//            //创建URL
//            URL url = new URL(sb.toString());
//
//            // 试图连接并取得返回状态码urlconn.connect();
//            URLConnection urlConn = url.openConnection();
//            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
//            httpResult = httpConn.getResponseCode();
//            in = new BufferedInputStream(httpConn.getInputStream());
//            //不等于HTTP_OK说明连接不成功
//            if (httpResult != HttpURLConnection.HTTP_OK){
//                log.info("连接url获取文件失败,url:" + fileUrl);
//            }else {
//                out = new ByteArrayOutputStream(1024);
//                byte[] temp = new byte[1024];
//                int size = 0;
//                while ((size = in.read(temp)) != -1) {
//                    out.write(temp, 0, size);
//                }
//                bf.write(out.toByteArray());
//            }
//        } catch (Exception e) {
//            log.error("下载单个文件出错,url:" + fileUrl + "||e:" + e.getMessage());
//        } finally {
//            try {
//                in.close();
//            } catch (IOException e) {
//                log.error("关闭输入流出错,url:" + fileUrl + "||e:" + e.getMessage());
//            }
//            try {
//                out.close();
//            } catch (IOException e) {
//                log.error("关闭输出流出错,url:" + fileUrl + "||e:" + e.getMessage());
//            }
//        }
//    }


    /**
     * 根据输入关键字模糊查询商户列表
     *
     * @param keywords 查询参数
     * @param request
     * @return
     */
    @ApiOperation(value = "根据名称模糊查询商户列表", notes = "根据名称模糊查询商户列表")
    @GetMapping("/getMerchantListByKeywords")
    public Object getMerchantListByKeywords(@RequestParam("keywords") String keywords, HttpServletRequest request) {
        log.info("根据名称模糊查询商户列表, data:{}", keywords);
        R result = R.success();
        List<MerchantAppHomePageDTO> list = Collections.emptyList();
        try {
            list = merchantService.getMerchantListByKeywords(keywords,request);
        } catch (BizException e) {
            log.error("根据名称模糊查询商户列表,error message:{},e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        result.setData(list);
        return result;
    }







    @ApiOperation(value = "根据名称模糊查询商户列表", notes = "根据名称模糊查询商户列表")
    @GetMapping("/getBannerMerchantListByKeywords")
    public Object getMerchantListByKeywords(@RequestParam(value = "categoryType", required = true) String categoryType,
                                            @RequestParam("keywords") String keywords,
                                            @RequestParam(value = "stateName", required = true) String stateName,HttpServletRequest request) {
        log.info("根据名称模糊查询商户列表, data:{}", keywords);
        R result = R.success();
        List<MerchantAppHomePageDTO> list = Collections.emptyList();
        try {
            list = merchantService.getBannerMerchantListByKeywords(categoryType, keywords, stateName,request);
        } catch (BizException e) {
            log.error("根据名称模糊查询商户列表,error message:{},e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        result.setData(list);
        return result;
    }


    /**
     * 根据商户ID查询商户绑定码列表
     *
     * @param merchantId 店铺ID
     * @param request
     * @return
     */
    @ApiOperation(value = "根据商户ID查询商户绑定码列表", notes = "根据商户ID查询商户绑定码列表")
    @GetMapping("/getMerchantCodeList/{merchantId}")
    public Object getMerchantCodeList(@PathVariable("merchantId") String merchantId, HttpServletRequest request) {
        log.info("根据商户ID查询商户绑定码列表, data:{}", merchantId);
        R result = R.success();
        Map<String, Object> list = null;
        try {
            list = merchantService.getMerchantDetailsById(merchantId, request);
        } catch (BizException e) {
            log.error("根据商户ID查询商户绑定码列表,error message:{},e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        result.setData(list);
        return result;
    }


    /**
     * 将电子合同转为纸质合同
     *
     * @param merchantId
     * @param request
     * @return java.lang.Object
     * @author zhangzeyuan
     * @date 2021/3/26 9:57
     */
    @ApiOperation(value = "将电子合同转为纸质合同", notes = "将电子合同转为纸质合同")
    @GetMapping("/convertToPaperContract/{merchantId}")
    public Object convertToPaperContract(@PathVariable("merchantId") Long merchantId, HttpServletRequest request) {
        log.info("将电子合同转为纸质合同, data:{}", merchantId);
        try {
            merchantService.convertToPaperContract(merchantId, request);
            return R.success();
        } catch (BizException e) {
            log.error("将电子合同转为纸质合同异常,error message:{},e:{}", e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }

    @Value("${spring.paperPath}")
    private String paperPath;

    @PassToken
    @PostMapping(name = "上传合同", value = "/paper")
    @ApiOperation(value = "上传合同", notes = "上传合同")
    @ApiImplicitParam(name = "file", value = "合同", dataType = "MultipartFile", required = true)
    public Object paper(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String originalFilename = file.getOriginalFilename();
        boolean fileFlag = UploadFileUtil.checkFile(originalFilename);
        boolean imgFlag = UploadFileUtil.checkImg(originalFilename);
        if (fileFlag || imgFlag) {
            String fileName;
            String sfx = originalFilename.substring(originalFilename.lastIndexOf(".")).trim().toLowerCase();
            String key = this.paperPath + "/" + SnowflakeUtil.generateId() + sfx;
            try {
                fileName = AmazonAwsUploadUtil.upload(file, key);
            } catch (Exception e) {
                log.error("upload paper failed, error message:{}", e.getMessage());
                return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
            }
            //将文件路径更新到表里

            return R.success(fileName);
        } else {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("illegal.file", getLang(request)));
        }
    }


    @PostMapping("/contractUpload")
    public Object contractUpload(@RequestBody MerchantDTO merchantDTO, HttpServletRequest request) {
        log.info("更新商户合同上传文件路径 merchant DTO:{}", merchantDTO);
        try {
            merchantService.updateContratFilePath(merchantDTO, request);
        } catch (BizException e) {
            log.error("更新商户合同上传文件路径 failed, merchantDTO: {}, error message:{}, error all:{}", merchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
