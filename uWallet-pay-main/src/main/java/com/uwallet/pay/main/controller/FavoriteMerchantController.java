package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.FavoriteMerchantDTO;
import com.uwallet.pay.main.model.entity.FavoriteMerchant;
import com.uwallet.pay.main.service.FavoriteMerchantService;
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
 * 用户的收藏商户数据
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户的收藏商户数据
 * @author: aaron S
 * @date: Created in 2021-04-07 18:04:58
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaron S
 */
@RestController
@RequestMapping("/favoriteMerchant")
@Slf4j
@Api("用户的收藏商户数据")
public class FavoriteMerchantController extends BaseController<FavoriteMerchant> {

    @Autowired
    private FavoriteMerchantService favoriteMerchantService;

    @ActionFlag(detail = "FavoriteMerchant_list")
    @ApiOperation(value = "分页查询用户的收藏商户数据", notes = "分页查询用户的收藏商户数据以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户的收藏商户数据列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = favoriteMerchantService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<FavoriteMerchantDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = favoriteMerchantService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户的收藏商户数据", notes = "通过id查询用户的收藏商户数据")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get favoriteMerchant Id:{}", id);
        return R.success(favoriteMerchantService.findFavoriteMerchantById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户的收藏商户数据一条数据", notes = "通过查询条件查询用户的收藏商户数据一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户的收藏商户数据一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get favoriteMerchant findOne params:{}", params);
        int total = favoriteMerchantService.count(params);
        if (total > 1) {
            log.error("get favoriteMerchant findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        FavoriteMerchantDTO favoriteMerchantDTO = null;
        if (total == 1) {
            favoriteMerchantDTO = favoriteMerchantService.findOneFavoriteMerchant(params);
        }
        return R.success(favoriteMerchantDTO);
    }

    @ActionFlag(detail = "FavoriteMerchant_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户的收藏商户数据", notes = "新增用户的收藏商户数据")
    public Object create(@RequestBody FavoriteMerchantDTO favoriteMerchantDTO, HttpServletRequest request) {
        log.info("add favoriteMerchant DTO:{}", favoriteMerchantDTO);
        try {
            favoriteMerchantService.saveFavoriteMerchant(favoriteMerchantDTO, request);
        } catch (BizException e) {
            log.error("add favoriteMerchant failed, favoriteMerchantDTO: {}, error message:{}, error all:{}", favoriteMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "FavoriteMerchant_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户的收藏商户数据", notes = "修改用户的收藏商户数据")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody FavoriteMerchantDTO favoriteMerchantDTO, HttpServletRequest request) {
        log.info("put modify id:{}, favoriteMerchant DTO:{}", id, favoriteMerchantDTO);
        try {
            favoriteMerchantService.updateFavoriteMerchant(id, favoriteMerchantDTO, request);
        } catch (BizException e) {
            log.error("update favoriteMerchant failed, favoriteMerchantDTO: {}, error message:{}, error all:{}", favoriteMerchantDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "FavoriteMerchant_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户的收藏商户数据", notes = "删除用户的收藏商户数据")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete favoriteMerchant, id:{}", id);
        try {
            favoriteMerchantService.logicDeleteFavoriteMerchant(id, request);
        } catch (BizException e) {
            log.error("delete failed, favoriteMerchant id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
