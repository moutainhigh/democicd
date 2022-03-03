package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.dao.AppCustomCategoryDisplayDAO;
import com.uwallet.pay.main.dao.AppCustomCategoryDisplayStateDAO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayDTO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO;
import com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplay;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplayState;
import com.uwallet.pay.main.model.entity.AppExclusiveBanner;
import com.uwallet.pay.main.service.AppCustomCategoryDisplayService;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.util.I18nUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * APP首页自定义分类展示信息
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: APP首页自定义分类展示信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:20
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class AppCustomCategoryDisplayServiceImpl extends BaseServiceImpl implements AppCustomCategoryDisplayService {


    @Resource
    private RedisUtils redisUtils;

    @Resource
    private StaticDataService staticDataService;

    @Autowired
    private AppCustomCategoryDisplayDAO appCustomCategoryDisplayDAO;

    @Resource
    private AppCustomCategoryDisplayStateDAO appCustomCategoryDisplayStateDAO;

    @Override
    public void saveAppCustomCategoryDisplay(@NonNull AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO, HttpServletRequest request) throws BizException {
        AppCustomCategoryDisplay appCustomCategoryDisplay = BeanUtil.copyProperties(appCustomCategoryDisplayDTO, new AppCustomCategoryDisplay());
        log.info("save AppCustomCategoryDisplay:{}", appCustomCategoryDisplay);
        if (appCustomCategoryDisplayDAO.insert((AppCustomCategoryDisplay) this.packAddBaseProps(appCustomCategoryDisplay, request)) != 1) {
            log.error("insert error, data:{}", appCustomCategoryDisplay);
            throw new BizException("Insert appCustomCategoryDisplay Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppCustomCategoryDisplayList(@NonNull List<AppCustomCategoryDisplay> appCustomCategoryDisplayList, HttpServletRequest request) throws BizException {
        if (appCustomCategoryDisplayList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appCustomCategoryDisplayDAO.insertList(appCustomCategoryDisplayList);
        if (rows != appCustomCategoryDisplayList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appCustomCategoryDisplayList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppCustomCategoryDisplay(@NonNull Long id, @NonNull AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO, HttpServletRequest request) throws BizException {
        log.info("full update appCustomCategoryDisplayDTO:{}", appCustomCategoryDisplayDTO);
        AppCustomCategoryDisplayDTO appCustomCategoryDisplay = this.findAppCustomCategoryDisplayById(id);
        if (Objects.isNull(appCustomCategoryDisplay)) {
            throw new BizException(I18nUtils.get("categories.not.exist", getLang(request)));
        }
        boolean needClearMerchantStatus = false;
        //查询分类是否已经存在
        Integer categoryType = appCustomCategoryDisplayDTO.getCategoryType();
        if (null != categoryType) {
            HashMap<String, Object> categoryTypeMap = Maps.newHashMapWithExpectedSize(1);
            categoryTypeMap.put("categoryType", categoryType);
            AppCustomCategoryDisplayDTO existCategory = appCustomCategoryDisplayDAO.selectOneDTO(categoryTypeMap);
            if (Objects.nonNull(existCategory) && !id.equals(existCategory.getId())) {
                throw new BizException(I18nUtils.get("categories.exist", getLang(request)));
            }

            //验证分类 是否更换 更换分类 需要清空商户信息
            if(!appCustomCategoryDisplay.getCategoryType().equals(appCustomCategoryDisplayDTO.getCategoryType())){
                needClearMerchantStatus = true;
            }
        }

        String categoryName = "";
        HashMap<String, Object> categoryMap = Maps.newHashMapWithExpectedSize(3);
        categoryMap.put("value", categoryType);
        categoryMap.put("code", "merchantCategories");
        StaticDataDTO staticData = staticDataService.findOneStaticData(categoryMap);
        if (null != staticData && null != staticData.getId()) {
            categoryName = staticData.getEnName();
        }
        AppCustomCategoryDisplay updateCategoryRecord = BeanUtil.copyProperties(appCustomCategoryDisplayDTO, new AppCustomCategoryDisplay());
        updateCategoryRecord.setId(id);
        updateCategoryRecord.setCategoryName(categoryName);
        int cnt = appCustomCategoryDisplayDAO.update((AppCustomCategoryDisplay) this.packModifyBaseProps(updateCategoryRecord, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appCustomCategoryDisplayDTO);
            throw new BizException("update appCustomCategoryDisplay Error!");
        }

        //更新州的排序对应的分类
        if(needClearMerchantStatus){
            AppCustomCategoryDisplayState updateStateRecord = new AppCustomCategoryDisplayState();
            updateStateRecord.setCategoryName(categoryName);
            updateStateRecord.setCategoryType(categoryType);
            updateStateRecord.setClearMerchatStr("1");
            updateStateRecord.setDisplayOrder(appCustomCategoryDisplay.getDisplayOrder());
            appCustomCategoryDisplayStateDAO.updateCategoryByOrder(updateStateRecord);
        }

        //更新redis
        HashMap<String, Object> queryParam = Maps.newHashMapWithExpectedSize(1);
        queryParam.put("id", id);
        AppCustomCategoryDisplay customCategory = appCustomCategoryDisplayDAO.selectOne(queryParam);
        //查询州信息
        JSONObject param = new JSONObject(3);
        param.put("code", "merchantState");
        List<StaticDataDTO> stateList = staticDataService.find(param, null, null);
        for (StaticDataDTO state : stateList) {
            redisUtils.set(Constant.getCustomCategoriesUpdateTimestampRedisKey(customCategory.getDisplayOrder(), state.getEnName()), System.currentTimeMillis());
        }
    }

    @Override
    public void updateAppCustomCategoryDisplaySelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appCustomCategoryDisplayDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppCustomCategoryDisplay(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appCustomCategoryDisplayDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAppCustomCategoryDisplay(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appCustomCategoryDisplayDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppCustomCategoryDisplayDTO findAppCustomCategoryDisplayById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO = appCustomCategoryDisplayDAO.selectOneDTO(params);
        return appCustomCategoryDisplayDTO;
    }

    @Override
    public AppCustomCategoryDisplayDTO findOneAppCustomCategoryDisplay(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppCustomCategoryDisplay appCustomCategoryDisplay = appCustomCategoryDisplayDAO.selectOne(params);
        AppCustomCategoryDisplayDTO appCustomCategoryDisplayDTO = new AppCustomCategoryDisplayDTO();
        if (null != appCustomCategoryDisplay) {
            BeanUtils.copyProperties(appCustomCategoryDisplay, appCustomCategoryDisplayDTO);
        }
        return appCustomCategoryDisplayDTO;
    }

    @Override
    public List<AppCustomCategoryDisplayDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AppCustomCategoryDisplayDTO> resultList = appCustomCategoryDisplayDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appCustomCategoryDisplayDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appCustomCategoryDisplayDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appCustomCategoryDisplayDAO.groupCount(conditions);
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("count");
            int count = 0;
            if (StringUtils.isNotBlank(value.toString())) {
                count = Integer.parseInt(value.toString());
            }
            map.put(key, count);
        }
        return map;
    }

    @Override
    public Double sum(String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("sumfield", sumField);
        return appCustomCategoryDisplayDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appCustomCategoryDisplayDAO.groupSum(conditions);
        Map<String, Double> map = new LinkedHashMap<>();
        for (Map<String, Object> m : maps) {
            String key = m.get("group") != null ? m.get("group").toString() : "group";
            Object value = m.get("sum");
            double sum = 0d;
            if (StringUtils.isNotBlank(value.toString())) {
                sum = Double.parseDouble(value.toString());
            }
            map.put(key, sum);
        }
        return map;
    }


    /**
     * 根据州名获取自定义分类信息
     *
     * @param order
     * @param stateName
     * @return com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayDTO
     * @author zhangzeyuan
     * @date 2021/4/13 16:18
     */
    @Override
    public AppCustomCategoryDisplayDTO getAppHomePageDataByOrderAndState(Integer order, String stateName) {
        return appCustomCategoryDisplayDAO.getAppHomePageDataByOrderAndState(order, stateName);
    }

    /**
     * 上移、下移f
     *
     * @param id
     * @param status
     * @param request
     * @author zhangzeyuan
     * @date 2021/4/12 16:27
     */
    @Override
    public void moveUpOrDown(Long id, Integer status, HttpServletRequest request) throws BizException {
        AppCustomCategoryDisplayDTO nowCategoryInfo = this.findAppCustomCategoryDisplayById(id);
        if (Objects.isNull(nowCategoryInfo)) {
            throw new BizException(I18nUtils.get("categories.not.exist", getLang(request)));
        }
        //当前行排序
        Integer displayOrder = nowCategoryInfo.getDisplayOrder();

        AppCustomCategoryDisplayDTO modifyCategoryInfo = null;

        //当前行更新记录
        AppCustomCategoryDisplay currentUpdateRecord = new AppCustomCategoryDisplay();
        //要移动的行更新记录
        AppCustomCategoryDisplay movedUpdateRecord = new AppCustomCategoryDisplay();

        currentUpdateRecord.setId(id);
        currentUpdateRecord = (AppCustomCategoryDisplay) this.packModifyBaseProps(currentUpdateRecord, request);

        movedUpdateRecord = (AppCustomCategoryDisplay) this.packModifyBaseProps(movedUpdateRecord, request);
        movedUpdateRecord.setDisplayOrder(displayOrder);

        //0：上移 1：下移
        if (status.equals(1)) {
            //取下一条记录
            modifyCategoryInfo = appCustomCategoryDisplayDAO.getNextOrderCategoryInfo(displayOrder);
        } else if (status.equals(0)) {
            ///取上一条记录
            modifyCategoryInfo = appCustomCategoryDisplayDAO.getLastOrderCategoryInfo(displayOrder);
        }
        if (Objects.isNull(modifyCategoryInfo)) {
            throw new BizException(I18nUtils.get("categories.not.exist", getLang(request)));
        }

        movedUpdateRecord.setId(modifyCategoryInfo.getId());
        currentUpdateRecord.setDisplayOrder(modifyCategoryInfo.getDisplayOrder());

        //更新对应州的排序
        //获取分类对应州信息 并更新
        HashMap<String, Object> stateQueryMap = Maps.newHashMapWithExpectedSize(1);
        stateQueryMap.put("displayOrder", displayOrder);
        List<AppCustomCategoryDisplayStateDTO> nowOrderStateList = appCustomCategoryDisplayStateDAO.selectDTO(stateQueryMap);

        stateQueryMap.put("displayOrder", modifyCategoryInfo.getDisplayOrder());
        List<AppCustomCategoryDisplayStateDTO> modifyOrderStateList = appCustomCategoryDisplayStateDAO.selectDTO(stateQueryMap);

        //更新redis
        //查询州信息
        JSONObject param = new JSONObject(3);
        param.put("code", "merchantState");
        List<StaticDataDTO> stateList = staticDataService.find(param, null, null);
        for (StaticDataDTO state : stateList) {
            redisUtils.set(Constant.getCustomCategoriesUpdateTimestampRedisKey(currentUpdateRecord.getDisplayOrder(), state.getEnName()), System.currentTimeMillis());
            redisUtils.set(Constant.getCustomCategoriesUpdateTimestampRedisKey(movedUpdateRecord.getDisplayOrder(), state.getEnName()), System.currentTimeMillis());
        }

        //更新分类对应州的排序信息
//        AppCustomCategoryDisplayState updateNowStateRecord = new AppCustomCategoryDisplayState();
//        updateNowStateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateNowStateRecord, request);
        if (CollectionUtils.isNotEmpty(nowOrderStateList)) {
            /*String ids = "";
            for (AppCustomCategoryDisplayStateDTO state : nowOrderStateList) {
                ids = ids + state.getId().toString() + ",";
            }
            updateNowStateRecord.setIds(ids.substring(0, ids.length() - 1));
            updateNowStateRecord.setDisplayOrder(modifyCategoryInfo.getDisplayOrder());
            appCustomCategoryDisplayStateDAO.updateOrderByMoveUpOrDown(updateNowStateRecord);*/

            for(AppCustomCategoryDisplayStateDTO state : nowOrderStateList){
                AppCustomCategoryDisplayState updateStateRecord = new AppCustomCategoryDisplayState();
                updateStateRecord.setId(state.getId());
                updateStateRecord.setDisplayOrder(modifyCategoryInfo.getDisplayOrder());
                updateStateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateStateRecord, request);
                appCustomCategoryDisplayStateDAO.update(updateStateRecord);
            }
        }

//        AppCustomCategoryDisplayState updateModifyStateRecord = new AppCustomCategoryDisplayState();
//        updateModifyStateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateModifyStateRecord, request);
        if (CollectionUtils.isNotEmpty(modifyOrderStateList)) {
            /*String ids = "";
            for (AppCustomCategoryDisplayStateDTO state : modifyOrderStateList) {
                ids = ids + state.getId().toString() + ",";
            }
            updateModifyStateRecord.setIds(ids.substring(0, ids.length() - 1));
            updateModifyStateRecord.setDisplayOrder(displayOrder);
            appCustomCategoryDisplayStateDAO.updateOrderByMoveUpOrDown(updateModifyStateRecord);*/

            for(AppCustomCategoryDisplayStateDTO state : modifyOrderStateList){
                AppCustomCategoryDisplayState updateStateRecord = new AppCustomCategoryDisplayState();
                updateStateRecord.setId(state.getId());
                updateStateRecord.setDisplayOrder(displayOrder);
                updateStateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateStateRecord, request);
                appCustomCategoryDisplayStateDAO.update(updateStateRecord);
            }
        }

        //更新
        appCustomCategoryDisplayDAO.update(currentUpdateRecord);
        appCustomCategoryDisplayDAO.update(movedUpdateRecord);
    }


}
