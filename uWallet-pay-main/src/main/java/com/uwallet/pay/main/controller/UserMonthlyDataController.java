package com.uwallet.pay.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.util.SnowflakeUtil;
import com.uwallet.pay.main.dao.QrPayFlowDAO;
import com.uwallet.pay.main.filter.PassToken;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.dto.UserMonthlyDataDTO;
import com.uwallet.pay.main.model.entity.UserMonthlyData;
import com.uwallet.pay.main.service.UserMonthlyDataService;
import com.uwallet.pay.main.filter.ActionFlag;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.controller.BaseController;
import com.uwallet.pay.main.util.I18nUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;


/**
 * <p>
 * 用户每月统计表
 * </p>
 *
 * @package:  com.uwallet.pay.main.controller
 * @description: 用户每月统计表
 * @author: zhoutt
 * @date: Created in 2021-04-08 16:40:22
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhoutt
 */
@RestController
@RequestMapping("/userMonthlyData")
@Slf4j
@Api("用户每月统计表")
public class UserMonthlyDataController extends BaseController<UserMonthlyData> {

    @Autowired
    private UserMonthlyDataService userMonthlyDataService;

    @Autowired
    private QrPayFlowDAO qrPayFlowDAO;

    @ActionFlag(detail = "UserMonthlyData_list")
    @ApiOperation(value = "分页查询用户每月统计表", notes = "分页查询用户每月统计表以及排序功能")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "s", value = "每页的条数", paramType = "query"),
            @ApiImplicitParam(name = "p", value = "请求的页码", paramType = "query"),
            @ApiImplicitParam(name = "scs", value = "排序字段，格式：scs=name(asc)&scs=age(desc)", paramType = "query")
    })
    @GetMapping(name = "查询-用户每月统计表列表")
    public Object list(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        int total = userMonthlyDataService.count(params);
        PagingContext pc;
        Vector<SortingContext> scs;
        List<UserMonthlyDataDTO> list = new ArrayList<>();
        pc = getPagingContext(request, total);
        if (total > 0) {
            scs = getSortingContext(request);
            list = userMonthlyDataService.find(params, scs, pc);
        }
        return R.success(list, pc);
    }

    @ApiOperation(value = "通过id查询用户每月统计表", notes = "通过id查询用户每月统计表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    @GetMapping(value = "/{id}", name="详情")
    public Object view(@PathVariable("id") Long id) {
        log.info("get userMonthlyData Id:{}", id);
        return R.success(userMonthlyDataService.findUserMonthlyDataById(id));
    }

    @ApiOperation(value = "通过查询条件查询用户每月统计表一条数据", notes = "通过查询条件查询用户每月统计表一条数据")
    @GetMapping(value = "/findOne", name="通过查询条件查询用户每月统计表一条数据")
    public Object findOne(HttpServletRequest request) {
        Map<String, Object> params = getConditionsMap(request);
        log.info("get userMonthlyData findOne params:{}", params);
        int total = userMonthlyDataService.count(params);
        if (total > 1) {
            log.error("get userMonthlyData findOne params: {}, error message:{}", params, "查询失败，返回多条数据");
            return R.fail(ErrorCodeEnum.FAIL_CODE.getCode(), I18nUtils.get("query.failed.return.multiple.data", getLang(request)));
        }
        UserMonthlyDataDTO userMonthlyDataDTO = null;
        if (total == 1) {
            userMonthlyDataDTO = userMonthlyDataService.findOneUserMonthlyData(params);
        }
        return R.success(userMonthlyDataDTO);
    }

    @ActionFlag(detail = "UserMonthlyData_add")
    @PostMapping(name = "创建")
    @ApiOperation(value = "新增用户每月统计表", notes = "新增用户每月统计表")
    public Object create(@RequestBody UserMonthlyDataDTO userMonthlyDataDTO, HttpServletRequest request) {
        log.info("add userMonthlyData DTO:{}", userMonthlyDataDTO);
        try {
            userMonthlyDataService.saveUserMonthlyData(userMonthlyDataDTO, request);
        } catch (BizException e) {
            log.error("add userMonthlyData failed, userMonthlyDataDTO: {}, error message:{}, error all:{}", userMonthlyDataDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.INSERT_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserMonthlyData_update")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改用户每月统计表", notes = "修改用户每月统计表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object update(@PathVariable("id") Long id, @RequestBody UserMonthlyDataDTO userMonthlyDataDTO, HttpServletRequest request) {
        log.info("put modify id:{}, userMonthlyData DTO:{}", id, userMonthlyDataDTO);
        try {
            userMonthlyDataService.updateUserMonthlyData(id, userMonthlyDataDTO, request);
        } catch (BizException e) {
            log.error("update userMonthlyData failed, userMonthlyDataDTO: {}, error message:{}, error all:{}", userMonthlyDataDTO, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.UPDATE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }

    @ActionFlag(detail = "UserMonthlyData_delete")
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除用户每月统计表", notes = "删除用户每月统计表")
    @ApiImplicitParam(name = "id", value = "主键id", dataType = "Long", paramType = "path", required = true)
    public Object remove(@PathVariable("id") Long id, HttpServletRequest request) {
        log.info("delete userMonthlyData, id:{}", id);
        try {
            userMonthlyDataService.logicDeleteUserMonthlyData(id, request);
        } catch (BizException e) {
            log.error("delete failed, userMonthlyData id: {}, error message:{}, error all:{}", id, e.getMessage(), e);
            return R.fail(ErrorCodeEnum.DELETE_FAILED_ERROR.getCode(), e.getMessage());
        }
        return R.success();
    }


    @PassToken
    @PostMapping(value = "/initialData", name="")
    @ApiOperation(value = "初始化数据", notes = "初始化数据")
    public Object initialData(@RequestBody JSONObject requestInfo, HttpServletRequest request) {
        try {
            Long now = System.currentTimeMillis();
            Long start = requestInfo.getLong("start");
            Long end = requestInfo.getLong("end");
            Map<String,Object> params = new HashMap<>(8);
            params.put("start",start);
            params.put("end", end);
            List<QrPayFlowDTO> resultList = qrPayFlowDAO.getMonthlyUserData(params);

            Long saveTime = end-1;
            if(resultList != null && resultList.size() > 0){
                List<UserMonthlyData> data = new ArrayList<>();
                for(QrPayFlowDTO qrPayFlowDTO:resultList){
                    UserMonthlyData oneData = new UserMonthlyData();
                    oneData.setId(SnowflakeUtil.generateId());
                    oneData.setDate(saveTime);
                    oneData.setPayAmount(qrPayFlowDTO.getPayAmount());
                    oneData.setTransAmount(qrPayFlowDTO.getTransAmount());
                    oneData.setSavedAmount(qrPayFlowDTO.getTransAmount().subtract(qrPayFlowDTO.getPayAmount()));
                    oneData.setUserId(qrPayFlowDTO.getPayUserId());
                    oneData.setCreatedDate(now);
                    oneData.setModifiedDate(now);
                    oneData.setStatus(1);
                    data.add(oneData);
                }
                userMonthlyDataService.saveUserMonthlyDataList(data,null);
            }
        } catch (Exception e) {
            log.info("failed jsonObject:{}, error message:{}, error all:{}", requestInfo, e.getMessage(), e);
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("errorMessage",e.getMessage());
            return R.success(resMap);
        }
        return R.success();
    }
}
