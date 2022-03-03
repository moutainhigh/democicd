package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.ContactUsDTO;
import com.uwallet.pay.main.model.entity.ContactUs;
import com.uwallet.pay.main.service.ContactUsService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 联系我们
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 联系我们
 * @author: baixinyue
 * @date: Created in 2020-06-17 08:52:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/contactUs")
@Slf4j
@Api("联系我们")
public class ContactUsController extends BaseController<ContactUs> {

    @Autowired
    private ContactUsService contactUsService;

    @ActionFlag(detail = "ContactUs_list")
    @ApiOperation(value = "分页查询联系我们", notes = "分页查询联系我们以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-联系我们列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = contactUsService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ContactUsDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = contactUsService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询联系我们", notes = "通过id查询联系我们")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get contactUs Id:{}", id);
        return R.success(contactUsService.findContactUsById(id));
    }

    @ApiOperation(value = "通过查询条件查询联系我们一条数据", notes = "通过查询条件查询联系我们一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询联系我们一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get contactUs findOne params:{}", params);
        int total = contactUsService.count(params);
        if (total > 1) {
            log.error("get contactUs findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ContactUsDTO contactUsDTO = null;
        if (total == 1) {
            contactUsDTO = contactUsService.findOneContactUs(params);
        }
        return R.success(contactUsDTO);
    }

    @ActionFlag(detail = "ContactUs_list")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增联系我们", notes = "新增联系我们")
    @PassToken
    public Object create(@RequestBody ContactUsDTO contactUsDTO, HttpServletRequest request) {
        log.info("add contactUs DTO:{}", contactUsDTO);
        try {
            contactUsService.saveContactUs(contactUsDTO, request);
        } catch (BizException e) {
            log.error("add contactUs failed, contactUsDTO: {}, error message:{}, error all:{}", contactUsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ContactUs_list")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改联系我们", notes = "修改联系我们")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ContactUsDTO contactUsDTO, HttpServletRequest request) {
        log.info("put modify id:{}, contactUs DTO:{}", id, contactUsDTO);
        try {
            contactUsService.updateContactUs(id, contactUsDTO, request);
        } catch (BizException e) {
            log.error("update contactUs failed, contactUsDTO: {}, error message:{}, error all:{}", contactUsDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ContactUs_list")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除联系我们", notes = "删除联系我们")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete contactUs, id:{}", id);
        try {
            contactUsService.logicDeleteContactUs(id, request);
        } catch (BizException e) {
            log.error("delete failed, contactUs id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
