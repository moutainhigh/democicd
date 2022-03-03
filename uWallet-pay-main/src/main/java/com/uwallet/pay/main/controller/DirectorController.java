package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.DirectorDTO;
import com.uwallet.pay.main.model.entity.Director;
import com.uwallet.pay.main.service.DirectorService;
import com.uwallet.pay.main.util.I18nUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * <p>
 * 董事信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 董事信息表
 * @author: Rainc
 * @date: Created in 2020-01-03 10:23:38
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Rainc
 */
@RestController
@RequestMapping("/director")
@Slf4j
@Api("董事信息表")
public class DirectorController extends BaseController<Director> {

    @Autowired
    private DirectorService directorService;

    @ApiOperation(value = "分页查询董事信息表", notes = "分页查询董事信息表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-董事信息表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = directorService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<DirectorDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = directorService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询董事信息表", notes = "通过id查询董事信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get director Id:{}", id);
        return R.success(directorService.findDirectorById(id));
    }

    @ApiOperation(value = "通过查询条件查询董事信息表一条数据", notes = "通过查询条件查询董事信息表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询董事信息表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get director findOne params:{}", params);
        int total = directorService.count(params);
        if (total > 1) {
            log.error("get director findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        DirectorDTO directorDTO = null;
        if (total == 1) {
            directorDTO = directorService.findOneDirector(params);
        }
        return R.success(directorDTO);
    }

    @PostMapping(name = "创建")
    @ApiOperation(value = "新增董事信息表", notes = "新增董事信息表")
    public Object create(@RequestBody DirectorDTO directorDTO, HttpServletRequest request) {
        log.info("add director DTO:{}", directorDTO);
        try {
            directorService.saveDirector(directorDTO, request);
        } catch (BizException e) {
            log.error("add director failed, directorDTO: {}, error message:{}, error all:{}", directorDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "修改董事信息表", notes = "修改董事信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody DirectorDTO directorDTO, HttpServletRequest request) {
        log.info("put modify id:{}, director DTO:{}", id, directorDTO);
        try {
            directorService.updateDirector(id, directorDTO, request);
        } catch (BizException e) {
            log.error("update director failed, directorDTO: {}, error message:{}, error all:{}", directorDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除董事信息表", notes = "删除董事信息表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete director, id:{}", id);
        try {
            directorService.logicDeleteDirector(id, request);
        } catch (BizException e) {
            log.error("delete failed, director id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
