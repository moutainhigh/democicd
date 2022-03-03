package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.Validator;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.model.dto.AppAboutUsDTO;
import com.uwallet.pay.main.model.entity.AppAboutUs;
import com.uwallet.pay.main.service.AppAboutUsService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.UploadFileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * <p>
 * app 关于我们
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: app 关于我们
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/appAboutUs")
@Slf4j
@Api("app 关于我们")
public class AppAboutUsController extends BaseController<AppAboutUs> {

    @Autowired
    private AppAboutUsService appAboutUsService;

    @ActionFlag(detail = "about_list")
    @ApiOperation(value = "分页查询app 关于我们", notes = "分页查询app 关于我们以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-app 关于我们列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = appAboutUsService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<AppAboutUsDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = appAboutUsService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询app 关于我们", notes = "通过id查询app 关于我们")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get appAboutUs Id:{}", id);
        return R.success(appAboutUsService.findAppAboutUsById(id));
    }

    @ApiOperation(value = "通过查询条件查询app 关于我们一条数据", notes = "通过查询条件查询app 关于我们一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询app 关于我们一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get appAboutUs findOne params:{}", params);
        int total = appAboutUsService.count(params);
        if (total > 1) {
            log.error("get appAboutUs findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        AppAboutUsDTO appAboutUsDTO = null;
        if (total == 1) {
            appAboutUsDTO = appAboutUsService.findOneAppAboutUs(params);
        }
        return R.success(appAboutUsDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增app 关于我们", notes = "新增app 关于我们")
    public Object create(@RequestBody AppAboutUsDTO appAboutUsDTO, HttpServletRequest request) {
        log.info("add appAboutUs DTO:{}", appAboutUsDTO);
        if (appAboutUsDTO.getPhone() == null || appAboutUsDTO.getPhone().length() > Validator.PHONE_LENGTH) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("aboutUs.phone.over.limit", getLang(request)));
        }
        if (appAboutUsDTO.getAppIntro() == null || appAboutUsDTO.getAppIntro().length() > Validator.TEXT_LENGTH_100) {
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), I18nUtils.get("banner.url.over.limit", getLang(request)));
        }
        try {
            appAboutUsService.saveAppAboutUs(appAboutUsDTO, request);
        } catch (BizException e) {
            log.error("add appAboutUs failed, appAboutUsDTO: {}, error message:{}, error all:{}", appAboutUsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改app 关于我们", notes = "修改app 关于我们")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody AppAboutUsDTO appAboutUsDTO, HttpServletRequest request) throws BizException {
        log.info("put modify id:{}, appAboutUs DTO:{}", id, appAboutUsDTO);
        if (appAboutUsDTO.getAppIntro() == null || appAboutUsDTO.getAppIntro().length() > Validator.TEXT_LENGTH_1000) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("exceed.input.limit", getLang(request)));
        }
        if (appAboutUsDTO.getPhone() == null) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("phone.length.error", getLang(request)));
        }
        if (!Validator.isEmail(appAboutUsDTO.getEmail())){
            throw new BizException(I18nUtils.get("username.contains.invalid.characters", getLang(request)));
        }
        if (appAboutUsDTO.getEmail() == null || (!Validator.isEmail(appAboutUsDTO.getEmail()))) {
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("email.format.error", getLang(request)));
        }
        try {
            appAboutUsService.updateAppAboutUs(id, appAboutUsDTO, request);
        } catch (BizException e) {
            log.error("update appAboutUs failed, appAboutUsDTO: {}, error message:{}, error all:{}", appAboutUsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除app 关于我们", notes = "删除app 关于我们")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete appAboutUs, id:{}", id);
        try {
            appAboutUsService.logicDeleteAppAboutUs(id, request);
        } catch (BizException e) {
            log.error("delete failed, appAboutUs id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
}
