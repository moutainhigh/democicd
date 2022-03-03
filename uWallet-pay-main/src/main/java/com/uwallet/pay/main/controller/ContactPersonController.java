package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.ContactPersonDTO;
import com.uwallet.pay.main.model.entity.ContactPerson;
import com.uwallet.pay.main.service.ContactPersonService;
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
 * 联系人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 联系人信息表
 * @author: baixinyue
 * @date: Created in 2020-08-06 11:45:37
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/contactPerson")
@Slf4j
@Api("联系人信息表")
public class ContactPersonController extends BaseController<ContactPerson> {

    @Autowired
    private ContactPersonService contactPersonService;

    @ActionFlag(detail = "ContactPerson_list")
    @ApiOperation(value = "分页查询联系人信息表", notes = "分页查询联系人信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-联系人信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = contactPersonService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<ContactPersonDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = contactPersonService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询联系人信息表", notes = "通过id查询联系人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get contactPerson Id:{}", id);
        return R.success(contactPersonService.findContactPersonById(id));
    }

    @ApiOperation(value = "通过查询条件查询联系人信息表一条数据", notes = "通过查询条件查询联系人信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询联系人信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get contactPerson findOne params:{}", params);
        int total = contactPersonService.count(params);
        if (total > 1) {
            log.error("get contactPerson findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        ContactPersonDTO contactPersonDTO = null;
        if (total == 1) {
            contactPersonDTO = contactPersonService.findOneContactPerson(params);
        }
        return R.success(contactPersonDTO);
    }

    @ActionFlag(detail = "ContactPerson_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增联系人信息表", notes = "新增联系人信息表")
    public Object create(@RequestBody ContactPersonDTO contactPersonDTO, HttpServletRequest request) {
        log.info("add contactPerson DTO:{}", contactPersonDTO);
        try {
            contactPersonService.saveContactPerson(contactPersonDTO, request);
        } catch (BizException e) {
            log.error("add contactPerson failed, contactPersonDTO: {}, error message:{}, error all:{}", contactPersonDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ContactPerson_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改联系人信息表", notes = "修改联系人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody ContactPersonDTO contactPersonDTO, HttpServletRequest request) {
        log.info("put modify id:{}, contactPerson DTO:{}", id, contactPersonDTO);
        try {
            contactPersonService.updateContactPerson(id, contactPersonDTO, request);
        } catch (BizException e) {
            log.error("update contactPerson failed, contactPersonDTO: {}, error message:{}, error all:{}", contactPersonDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "ContactPerson_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除联系人信息表", notes = "删除联系人信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete contactPerson, id:{}", id);
        try {
            contactPersonService.logicDeleteContactPerson(id, request);
        } catch (BizException e) {
            log.error("delete failed, contactPerson id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
