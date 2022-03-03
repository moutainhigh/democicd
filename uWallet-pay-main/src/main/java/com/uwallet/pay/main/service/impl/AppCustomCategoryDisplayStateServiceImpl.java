package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.DataEnum;
import com.uwallet.pay.main.dao.AppCustomCategoryDisplayStateDAO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryImageDTO;
import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplayState;
import com.uwallet.pay.main.service.AppCustomCategoryDisplayStateService;
import com.uwallet.pay.main.service.StaticDataService;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MerchantListUtils;
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
 * APP首页自定义分类 每个州展示商户、图片信息
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: APP首页自定义分类 每个州展示商户、图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:19
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class AppCustomCategoryDisplayStateServiceImpl extends BaseServiceImpl implements AppCustomCategoryDisplayStateService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private StaticDataService staticDataService;

    @Autowired
    private AppCustomCategoryDisplayStateDAO appCustomCategoryDisplayStateDAO;

    @Override
    public void saveAppCustomCategoryDisplayState(@NonNull AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, HttpServletRequest request) throws BizException {
        AppCustomCategoryDisplayState appCustomCategoryDisplayState = BeanUtil.copyProperties(appCustomCategoryDisplayStateDTO, new AppCustomCategoryDisplayState());
        log.info("save AppCustomCategoryDisplayState:{}", appCustomCategoryDisplayState);
        if (appCustomCategoryDisplayStateDAO.insert((AppCustomCategoryDisplayState) this.packAddBaseProps(appCustomCategoryDisplayState, request)) != 1) {
            log.error("insert error, data:{}", appCustomCategoryDisplayState);
            throw new BizException("Insert appCustomCategoryDisplayState Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppCustomCategoryDisplayStateList(@NonNull List<AppCustomCategoryDisplayState> appCustomCategoryDisplayStateList, HttpServletRequest request) throws BizException {
        if (appCustomCategoryDisplayStateList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appCustomCategoryDisplayStateDAO.insertList(appCustomCategoryDisplayStateList);
        if (rows != appCustomCategoryDisplayStateList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appCustomCategoryDisplayStateList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppCustomCategoryDisplayState(@NonNull Long id, @NonNull AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, HttpServletRequest request) throws BizException {
        log.info("full update appCustomCategoryDisplayStateDTO:{}", appCustomCategoryDisplayStateDTO);
        AppCustomCategoryDisplayState appCustomCategoryDisplayState = BeanUtil.copyProperties(appCustomCategoryDisplayStateDTO, new AppCustomCategoryDisplayState());
        appCustomCategoryDisplayState.setId(id);
        //修改view all 图片
        Integer operateType = appCustomCategoryDisplayStateDTO.getOperateType();
        if (Objects.nonNull(operateType) && operateType.equals(1)) {
            this.updateViewAllBannerData(appCustomCategoryDisplayState, request);
            return;
        }

        //修改 商户信息
        if (Objects.nonNull(operateType) && operateType.equals(2)) {
            this.updateCategoryMerchantInfo(appCustomCategoryDisplayStateDTO, appCustomCategoryDisplayState, request);
            return;
        }
    }


    /**
     * 修改view all banner信息
     *
     * @param appCustomCategoryDisplayState
     * @param request
     * @author zhangzeyuan
     * @date 2021/5/6 17:45
     */
    private void updateViewAllBannerData(AppCustomCategoryDisplayState appCustomCategoryDisplayState, HttpServletRequest request) throws BizException {
        //更新图片数量
        String imagesJson = appCustomCategoryDisplayState.getImagesJson();
        if (StringUtils.isNotBlank(imagesJson)) {
            List<AppCustomCategoryImageDTO> imageList = JSONArray.parseArray(imagesJson, AppCustomCategoryImageDTO.class);
            if (CollectionUtils.isNotEmpty(imageList)) {
                appCustomCategoryDisplayState.setImageTotal(imageList.size());
            }
        }

        int cnt = appCustomCategoryDisplayStateDAO.update((AppCustomCategoryDisplayState) this.packModifyBaseProps(appCustomCategoryDisplayState, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appCustomCategoryDisplayState);
            throw new BizException("update appCustomCategoryDisplayState Error!");
        }

        //更新redis 修改时间
        String redisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(appCustomCategoryDisplayState.getDisplayOrder(), appCustomCategoryDisplayState.getStateName());
        redisUtils.set(redisKey, System.currentTimeMillis());
    }

    /**
     * 修改商户信息
     *
     * @param appCustomCategoryDisplayStateDTO
     * @param appCustomCategoryDisplayState
     * @param request
     * @author zhangzeyuan
     * @date 2021/5/6 17:44
     */
    private void updateCategoryMerchantInfo(AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO, AppCustomCategoryDisplayState appCustomCategoryDisplayState, HttpServletRequest request) throws BizException {

        //如果是和某个州一样的话 获取那个州的商户数据
        if (appCustomCategoryDisplayStateDTO.getMerchantDisplayType().equals(DataEnum.CUSTOM_CATEGORY_MERCHANT_STATUS_SAMEAS.getCode())) {
            //判断选择的是否是本州
            String merchantSameStateValue = appCustomCategoryDisplayStateDTO.getMerchantSameStateName();
            HashMap<String, Object> stateMap = Maps.newHashMapWithExpectedSize(3);
            stateMap.put("code", "merchantState");
            stateMap.put("value", merchantSameStateValue);
            StaticDataDTO staticStateData = staticDataService.findOneStaticData(stateMap);
            if (Objects.nonNull(staticStateData) && staticStateData.getEnName().equals(appCustomCategoryDisplayStateDTO.getStateName())) {
                throw new BizException(I18nUtils.get("categories.merchant.not.same.self", getLang(request)));
            }
            //查询 那个州的 商户信息
            HashMap<String, Object> stateMerchantMap = Maps.newHashMapWithExpectedSize(1);
            stateMerchantMap.put("displayOrder", appCustomCategoryDisplayStateDTO.getDisplayOrder());
            stateMerchantMap.put("stateName", staticStateData.getEnName());
            AppCustomCategoryDisplayStateDTO sameAsStateData = appCustomCategoryDisplayStateDAO.selectOneDTO(stateMerchantMap);
            if (Objects.nonNull(sameAsStateData) && StringUtils.isNotBlank(sameAsStateData.getMerchantIds())) {
                appCustomCategoryDisplayState.setMerchantIds(sameAsStateData.getMerchantIds());
            }else{
                throw new BizException(I18nUtils.get("categories.merchant.same.state.nodata", getLang(request)));
            }
        }else if(appCustomCategoryDisplayStateDTO.getMerchantDisplayType().equals(DataEnum.CUSTOM_CATEGORY_MERCHANT_STATUS_CUSTOM.getCode())){
            //校验商户数量最少3个、校验重复商户
            String merchantIds = appCustomCategoryDisplayStateDTO.getMerchantIds();
            if (StringUtils.isNotBlank(merchantIds) && appCustomCategoryDisplayStateDTO.getMerchantDisplayType().equals(DataEnum.CUSTOM_CATEGORY_MERCHANT_STATUS_CUSTOM.getCode())) {
                String[] split = merchantIds.split(",");
                if (split.length < 3) {
                    throw new BizException(I18nUtils.get("categories.merchant.size", getLang(request)));
                }
                int arrayLenth = split.length;
                Set<String> merchantIdSet = new HashSet<String>();
                //重新拼接商户信息
                String newMerchantIds = "";
                for (int i = 0; i < split.length; i++) {
                    if(StringUtils.isNotBlank(split[i])){
                        newMerchantIds = newMerchantIds + split[i] + ",";
                        merchantIdSet.add(split[i]);
                    }
                }
                newMerchantIds = newMerchantIds.substring(0, newMerchantIds.length() - 1);
                int setSize = merchantIdSet.size();
                if (setSize < arrayLenth) {
                    throw new BizException(I18nUtils.get("categories.merchant.not.repeat", getLang(request)));
                }
                if(StringUtils.isBlank(newMerchantIds)){
                    throw new BizException(I18nUtils.get("categories.merchant.size", getLang(request)));
                }
                appCustomCategoryDisplayState.setMerchantIds(newMerchantIds);
            }else {
                throw new BizException(I18nUtils.get("categories.merchant.size", getLang(request)));
            }
        }

        int cnt = appCustomCategoryDisplayStateDAO.update((AppCustomCategoryDisplayState) this.packModifyBaseProps(appCustomCategoryDisplayState, request));
        if (cnt != 1) {
            log.error("update error, data:{}", appCustomCategoryDisplayStateDTO);
            throw new BizException("update appCustomCategoryDisplayState Error!");
        }
        //更新 和该州一样的 商户
        if (appCustomCategoryDisplayStateDTO.getMerchantDisplayType().equals(DataEnum.CUSTOM_CATEGORY_MERCHANT_STATUS_CUSTOM.getCode())) {
            //根据州名获取value值
            HashMap<String, Object> stateMap = Maps.newHashMapWithExpectedSize(2);
            stateMap.put("code", "merchantState");
            stateMap.put("enName", appCustomCategoryDisplayStateDTO.getStateName());
            StaticDataDTO staticStateData = staticDataService.findOneStaticData(stateMap);
            if (Objects.nonNull(staticStateData)) {
                HashMap<String, Object> sameMap = Maps.newHashMapWithExpectedSize(2);
                sameMap.put("merchantDisplayType", DataEnum.CUSTOM_CATEGORY_MERCHANT_STATUS_SAMEAS.getCode());
                sameMap.put("merchantSameStateName", staticStateData.getValue());
                sameMap.put("displayOrder", appCustomCategoryDisplayStateDTO.getDisplayOrder());
                List<AppCustomCategoryDisplayStateDTO> sameList = appCustomCategoryDisplayStateDAO.selectDTO(sameMap);
                if (CollectionUtils.isNotEmpty(sameList)) {
                    /*String collect = sameList.stream()
                            .map(AppCustomCategoryDisplayStateDTO::getId)
                            .collect(Collectors.joining(","));*/
                    String ids = "";
                    for (AppCustomCategoryDisplayStateDTO state : sameList) {
                        ids = ids + state.getId().toString() + ",";
                    }
                    //同步商户信息
                    AppCustomCategoryDisplayState updateRecord = new AppCustomCategoryDisplayState();
                    updateRecord.setMerchantIds(appCustomCategoryDisplayStateDTO.getMerchantIds());
                    updateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateRecord, request);
                    updateRecord.setIds(ids.substring(0, ids.length() - 1));
                    appCustomCategoryDisplayStateDAO.updateMerchantIdsBySameStateChange(updateRecord);
                }
            }
        }

        //更新redis 修改时间
        String redisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(appCustomCategoryDisplayStateDTO.getDisplayOrder(), appCustomCategoryDisplayStateDTO.getStateName());
        redisUtils.set(redisKey, System.currentTimeMillis());
    }

    @Override
    public void updateAppCustomCategoryDisplayStateSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appCustomCategoryDisplayStateDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppCustomCategoryDisplayState(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appCustomCategoryDisplayStateDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteAppCustomCategoryDisplayState(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appCustomCategoryDisplayStateDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppCustomCategoryDisplayStateDTO findAppCustomCategoryDisplayStateById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO = appCustomCategoryDisplayStateDAO.selectOneDTO(params);
        return appCustomCategoryDisplayStateDTO;
    }

    @Override
    public AppCustomCategoryDisplayStateDTO findOneAppCustomCategoryDisplayState(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppCustomCategoryDisplayState appCustomCategoryDisplayState = appCustomCategoryDisplayStateDAO.selectOne(params);
        AppCustomCategoryDisplayStateDTO appCustomCategoryDisplayStateDTO = new AppCustomCategoryDisplayStateDTO();
        if (null != appCustomCategoryDisplayState) {
            BeanUtils.copyProperties(appCustomCategoryDisplayState, appCustomCategoryDisplayStateDTO);
        }
        return appCustomCategoryDisplayStateDTO;
    }

    @Override
    public List<AppCustomCategoryDisplayStateDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AppCustomCategoryDisplayStateDTO> resultList = appCustomCategoryDisplayStateDAO.selectDTO(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appCustomCategoryDisplayStateDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appCustomCategoryDisplayStateDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appCustomCategoryDisplayStateDAO.groupCount(conditions);
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
        return appCustomCategoryDisplayStateDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appCustomCategoryDisplayStateDAO.groupSum(conditions);
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
     * 获取APP主页自定义分类view all图片信息
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/4/14 10:02
     */
    @Override
    public JSONObject listAllAppHomePageCategoryAllImgData(JSONObject requestInfo, HttpServletRequest request) throws BizException {
        JSONObject result = new JSONObject();
        //排序
        Integer order = requestInfo.getJSONObject("data").getInteger("order");
        //州名
        /*String state = requestInfo.getJSONObject("data").getString("stateName");
        if(Objects.isNull(order) || StringUtils.isBlank(state)){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }*/

        //根据经纬度获取州名 默认为
        String stateName = "QLD";
        //用户经度
        String longitude = requestInfo.getJSONObject("data").getString("longitude");
        //用户纬度
        String latitude = requestInfo.getJSONObject("data").getString("latitude");

        JSONObject locationJson = MerchantListUtils.getCityStreet(latitude, longitude);
        String tempState = locationJson.getString("stateName");
        if (StringUtils.isNotBlank(tempState)) {
            //查询分类信息
            JSONObject param = new JSONObject(3);
            param.put("code", "merchantState");
            Map<String, String> locationMap = staticDataService.find(param, null, null).stream().collect(Collectors.toMap(StaticDataDTO::getEnName, StaticDataDTO::getValue));
            if (Objects.nonNull(locationMap.get(tempState))) {
                stateName = tempState;
            }
            log.info("获取自定义分类根据经纬度获取的州名为：" + tempState);
        }

        //查询
        HashMap<String, Object> paramMap = Maps.newHashMapWithExpectedSize(2);
        paramMap.put("displayOrder", order);
        paramMap.put("stateName", stateName);
        AppCustomCategoryDisplayStateDTO stateDTO = appCustomCategoryDisplayStateDAO.selectOneDTO(paramMap);
        JSONArray images = new JSONArray();
        if (Objects.nonNull(stateDTO) && StringUtils.isNotBlank(stateDTO.getImagesJson())) {
            images = JSONArray.parseArray(stateDTO.getImagesJson());
        }
        result.put("images", images);
        return result;
    }

    /**
     * 收藏变化时更新APP首页商户分类
     *
     * @param merchantId
     * @author zhangzeyuan
     * @date 2021/4/27 19:45
     */
    @Override
    public void updateAppCategoriesWhereFavoriteChange(Long merchantId) throws BizException {
        List<AppCustomCategoryDisplayStateDTO> list = appCustomCategoryDisplayStateDAO.listByMerchantId(merchantId);
        for (AppCustomCategoryDisplayStateDTO displayStateDTO : list) {
            //更新redis 修改时间
            String customCategoriesUpdateTimestampRedisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(displayStateDTO.getDisplayOrder(), displayStateDTO.getStateName());
            redisUtils.set(customCategoriesUpdateTimestampRedisKey, System.currentTimeMillis());
        }
    }

    /**
     * 商户禁用时 更新APP首页商户分类
     *
     * @param merchantId
     * @author zhangzeyuan
     * @date 2021/5/20 15:43
     */
    @Override
    public void updateMerchantDataByMerchantNotAvailable(Long merchantId, HttpServletRequest request) {
        List<AppCustomCategoryDisplayStateDTO> list = appCustomCategoryDisplayStateDAO.listByMerchantId(merchantId);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        for (AppCustomCategoryDisplayStateDTO categoryDisplayState : list){
            //更新库里数据
            String merchantIds = categoryDisplayState.getMerchantIds();
            String tempStr = merchantId + ",";
            String updateMerchantIds = "";
            if(merchantIds.contains(tempStr)){
                updateMerchantIds = merchantIds.replace(tempStr, "");
            }else {
                updateMerchantIds = merchantIds.replace(merchantId.toString(), "");
            }

            //更新库里数据
            AppCustomCategoryDisplayState updateRecord = new AppCustomCategoryDisplayState();
            updateRecord.setId(categoryDisplayState.getId());
            updateRecord.setMerchantIds(updateMerchantIds);
            updateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateRecord, request);
            appCustomCategoryDisplayStateDAO.updateCategoryMerchantInfo(updateRecord);
            //更新redis 修改时间
            String customCategoriesUpdateTimestampRedisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(categoryDisplayState.getDisplayOrder(), categoryDisplayState.getStateName());
            redisUtils.set(customCategoriesUpdateTimestampRedisKey, System.currentTimeMillis());
        }

    }


    /**
     * 商户禁用时 更新APP首页商户分类
     *
     * @param merchantId
     * @author zhangzeyuan
     * @date 2021/5/20 15:43
     */
    @Override
    public void updateMerchantDataToChangeAppHomepage(Long merchantId, Boolean merchantAvailableStatus, HttpServletRequest request) {
        List<AppCustomCategoryDisplayStateDTO> list = appCustomCategoryDisplayStateDAO.listByMerchantId(merchantId);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        for (AppCustomCategoryDisplayStateDTO categoryDisplayState : list){
            //商户不可用时/分类变动时  app首页去除显示
            if(!merchantAvailableStatus){
                //更新库里数据
                String merchantIds = categoryDisplayState.getMerchantIds();
                String tempStr = merchantId + ",";
                String updateMerchantIds = "";
                if(merchantIds.contains(tempStr)){
                    updateMerchantIds = merchantIds.replace(tempStr, "");
                }else {
                    updateMerchantIds = merchantIds.replace(merchantId.toString(), "");
                }

                //更新库里数据
                AppCustomCategoryDisplayState updateRecord = new AppCustomCategoryDisplayState();
                updateRecord.setId(categoryDisplayState.getId());
                updateRecord.setMerchantIds(updateMerchantIds);
                updateRecord = (AppCustomCategoryDisplayState) this.packModifyBaseProps(updateRecord, request);
                appCustomCategoryDisplayStateDAO.updateCategoryMerchantInfo(updateRecord);
            }

            //更新redis 修改时间
            String customCategoriesUpdateTimestampRedisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(categoryDisplayState.getDisplayOrder(), categoryDisplayState.getStateName());
            redisUtils.set(customCategoriesUpdateTimestampRedisKey, System.currentTimeMillis());
        }

    }

    @Override
    public void updatDefinition(Long id, Integer merchantDisplayType) {
        appCustomCategoryDisplayStateDAO.updatDefinition(id, merchantDisplayType);

        Map merchantDisplayTypeMap2 = new HashMap();
        merchantDisplayTypeMap2.put("id", id);
        AppCustomCategoryDisplayState categoryDisplayState = appCustomCategoryDisplayStateDAO.selectOne(merchantDisplayTypeMap2);
        //更新redis 修改时间
        String customCategoriesUpdateTimestampRedisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(categoryDisplayState.getDisplayOrder(), categoryDisplayState.getStateName());
        redisUtils.set(customCategoriesUpdateTimestampRedisKey, System.currentTimeMillis());
    }

    @Override
    public Map getDistanceMerchant(Long id) {
        return appCustomCategoryDisplayStateDAO.getDistanceMerchant(id);
    }


}
