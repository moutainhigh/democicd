package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.main.service.ServerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bankLogo")
@Slf4j
@Api("银行logo")
public class BankLogoController extends BaseController {

    @Autowired
    private ServerService serverService;

    @Autowired
    private RedisUtils redisUtils;

    @ActionFlag(detail = "BankLogo_list")
    @ApiOperation(value = "分页查询银行logo", notes = "分页查询银行logo以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(value = "/bankLogoList", name = "查询-银行logo列表")
    public Object bankLogoList(HttpServletRequest request) {
        JSONObject result = null;
        try {
            Map<String, Object> params = getConditionsMap(request);
            result = serverService.bankLogoList(JSONObject.parseObject(JSONObject.toJSONString(params)));
        } catch (Exception e) {
            log.info("find bank logo list failed, data:{}, error message:{}, e:{}", result, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result.getJSONArray("data"), JSONObject.parseObject(result.getJSONObject("pc").toJSONString(), PagingContext.class));
    }

    @ApiOperation(value = "通过id查询银行logo", notes = "通过id查询银行logo")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/bankLogo/{id}", name="详情")
    public Object bankLogoView(@PathVariable("id") Long id) {
        log.info("get bankLogo Id:{}", id);
        JSONObject result = null;
        try {
            result = serverService.bankLogoInfo(id).getJSONObject("data");
        } catch (Exception e) {
            log.info("get bankLogo failed, id:{}, error message:{}, e:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), e.getMessage());
        }
        return R.success(result);
    }

    @ActionFlag(detail = "BankLogo_list")
    @PostMapping(value = "/bankLogoCreate", name = "创建")
    @ApiOperation(value = "新增银行logo", notes = "新增银行logo")
    public Object bankLogoCreate(@RequestBody JSONObject params, HttpServletRequest request) {
        log.info("add bankLogo data:{}", params);
        try {
            serverService.bankLogoSave(params);
            redisUtils.del("bankLogo");
            redisUtils.lSet("bankLogo", serverService.getBankLogoList().getJSONArray("data"));
        } catch (Exception e) {
            log.error("add bankLogo failed, data: {}, error message:{}, error all:{}", params, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "BankLogo_list")
    @PutMapping("/bankLogoUpdate/{id}")
    @ApiOperation(value = "修改银行logo", notes = "修改银行logo")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object bankLogoUpdate(@PathVariable("id") Long id, @RequestBody JSONObject params, HttpServletRequest request) {
        log.info("put modify id:{}, data:{}", id, params);
        try {
            serverService.updateBankLogo(id, params);
            redisUtils.del("bankLogo");
            redisUtils.lSet("bankLogo", serverService.getBankLogoList().getJSONArray("data"));
        } catch (Exception e) {
            log.error("update bankLogo failed, data: {}, error message:{}, error all:{}", params, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "BankLogo_list")
    @DeleteMapping("/bankLogoDelete/{id}")
    @ApiOperation(value = "删除银行logo", notes = "删除银行logo")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object bankLogoDelete(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete bankLogo, id:{}", id);
        try {
            serverService.bankLogoDelete(id);
            redisUtils.del("bankLogo");
            redisUtils.lSet("bankLogo", serverService.getBankLogoList().getJSONArray("data"));
        } catch (Exception e) {
            log.error("delete failed, bankLogo id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

}
