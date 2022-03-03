package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.filter.SignVerify;
import com.uwallet.pay.main.model.dto.TagDTO;
import com.uwallet.pay.main.model.entity.Tag;
import com.uwallet.pay.main.service.TagService;
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
 * Tag数据
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: Tag数据
 * @author: aaronS
 * @date: Created in 2021-01-07 11:19:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@RestController
@RequestMapping("/tag")
@Slf4j
@Api("Tag数据")
public class TagController extends BaseController<Tag> {

    @Autowired
    private TagService tagService;

    @ActionFlag(detail = "Tag_list")
    @ApiOperation(value = "分页查询Tag数据", notes = "分页查询Tag数据以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-Tag数据列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = tagService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<TagDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = tagService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询Tag数据", notes = "通过id查询Tag数据")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get tag Id:{}", id);
        return R.success(tagService.findTagById(id));
    }

    @ApiOperation(value = "通过查询条件查询Tag数据一条数据", notes = "通过查询条件查询Tag数据一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询Tag数据一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get tag findOne params:{}", params);
        int total = tagService.count(params);
        if (total > 1) {
            log.error("get tag findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        TagDTO tagDTO = null;
        if (total == 1) {
            tagDTO = tagService.findOneTag(params);
        }
        return R.success(tagDTO);
    }

    @ActionFlag(detail = "Tag_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增Tag数据", notes = "新增Tag数据")
    public Object create(@RequestBody TagDTO tagDTO, HttpServletRequest request) {
        log.info("add tag DTO:{}", tagDTO);
        try {
            tagService.saveTag(tagDTO, request);
        } catch (BizException e) {
            log.error("add tag failed, tagDTO: {}, error message:{}, error all:{}", tagDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Tag_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改Tag数据", notes = "修改Tag数据")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody TagDTO tagDTO, HttpServletRequest request) {
        log.info("put modify id:{}, tag DTO:{}", id, tagDTO);
        try {
            tagService.updateTag(id, tagDTO, request);
        } catch (BizException e) {
            log.error("update tag failed, tagDTO: {}, error message:{}, error all:{}", tagDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "Tag_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除Tag数据", notes = "删除Tag数据")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete tag, id:{}", id);
        try {
            tagService.logicDeleteTag(id, request);
        } catch (BizException e) {
            log.error("delete failed, tag id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }
    //@ActionFlag(detail = "Tag_delete")
    @PostMapping("/matchTags")
    @ApiOperation(value = "模糊匹配tag表数据", notes = "模糊匹配tag表数据")
    public Object matchTags(@RequestBody JSONObject data, HttpServletRequest request) {
        log.info("模糊匹配tag表数据, data:{}", data);
        try {
            return R.success( tagService.matchTags(data.getJSONObject("data"), request));
        } catch (Exception e) {
            log.error("模糊匹配tag表数据失败, data: {}, error message:{}, error all:{}", data, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
    }

    @ApiOperation(value = "获取tag,全量表信息", notes = "获取tag信息")
    @PostMapping("/getTagInfo")
    public Object getTagInfo() {
        try {
            return R.success(tagService.getTagAllInfo());
        } catch (Exception e) {
            log.error("WEB-get Tag Info, error message:{}", e.getMessage(),e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
    }
}
