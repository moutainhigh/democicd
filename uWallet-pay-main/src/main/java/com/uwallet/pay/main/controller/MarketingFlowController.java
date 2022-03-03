package com.uwallet.pay.main.controller;

import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.main.model.dto.MarketingFlowDTO;
import com.uwallet.pay.main.model.entity.MarketingFlow;
import com.uwallet.pay.main.service.MarketingFlowService;
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
 * 账户动账交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:30:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@RestController
@RequestMapping("/marketingFlow")
@Slf4j
@Api("账户动账交易流水表")
public class MarketingFlowController extends BaseController<MarketingFlow> {

    @Autowired
    private MarketingFlowService marketingFlowService;

    @ActionFlag(detail = "MarketingFlow_list")
    @ApiOperation(value = "分页查询账户动账交易流水表", notes = "分页查询账户动账交易流水表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-账户动账交易流水表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = marketingFlowService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<MarketingFlowDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = marketingFlowService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询账户动账交易流水表", notes = "通过id查询账户动账交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get marketingFlow Id:{}", id);
        return R.success(marketingFlowService.findMarketingFlowById(id));
    }

    @ApiOperation(value = "通过查询条件查询账户动账交易流水表一条数据", notes = "通过查询条件查询账户动账交易流水表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询账户动账交易流水表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get marketingFlow findOne params:{}", params);
        int total = marketingFlowService.count(params);
        if (total > 1) {
            log.error("get marketingFlow findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        MarketingFlowDTO marketingFlowDTO = null;
        if (total == 1) {
            marketingFlowDTO = marketingFlowService.findOneMarketingFlow(params);
        }
        return R.success(marketingFlowDTO);
    }

    @ActionFlag(detail = "MarketingFlow_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增账户动账交易流水表", notes = "新增账户动账交易流水表")
    public Object create(@RequestBody MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) {
        log.info("add marketingFlow DTO:{}", marketingFlowDTO);
        try {
            marketingFlowService.saveMarketingFlow(marketingFlowDTO, request);
        } catch (BizException e) {
            log.error("add marketingFlow failed, marketingFlowDTO: {}, error message:{}, error all:{}", marketingFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MarketingFlow_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改账户动账交易流水表", notes = "修改账户动账交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody MarketingFlowDTO marketingFlowDTO, HttpServletRequest request) {
        log.info("put modify id:{}, marketingFlow DTO:{}", id, marketingFlowDTO);
        try {
            marketingFlowService.updateMarketingFlow(id, marketingFlowDTO, request);
        } catch (BizException e) {
            log.error("update marketingFlow failed, marketingFlowDTO: {}, error message:{}, error all:{}", marketingFlowDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "MarketingFlow_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除账户动账交易流水表", notes = "删除账户动账交易流水表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete marketingFlow, id:{}", id);
        try {
            marketingFlowService.logicDeleteMarketingFlow(id, request);
        } catch (BizException e) {
            log.error("delete failed, marketingFlow id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
