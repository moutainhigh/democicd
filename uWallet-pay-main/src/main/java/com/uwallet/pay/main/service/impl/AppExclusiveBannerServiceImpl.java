package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.core.model.entity.BaseEntity;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.util.QRCodeUtil;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.DataEnum;
import com.uwallet.pay.main.constant.StaticDataEnum;
import com.uwallet.pay.main.dao.AppExclusiveBannerDAO;
import com.uwallet.pay.main.dao.MerchantDAO;
import com.uwallet.pay.main.model.dto.*;
import com.uwallet.pay.main.model.entity.AppExclusiveBanner;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MerchantListUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * APP首页banner、市场推广图片配置表
 * </p>
 *
 * @package: com.uwallet.pay.main.generator.service.impl
 * @description: APP首页banner、市场推广图片配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-04-08 13:35:33
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Service
@Slf4j
public class AppExclusiveBannerServiceImpl extends BaseServiceImpl implements AppExclusiveBannerService {

    @Resource
    private AppExclusiveBannerDAO appExclusiveBannerDAO;

    @Resource
    private MerchantDAO merchantDAO;

    @Resource
    private AppCustomCategoryDisplayService appCustomCategoryDisplayService;

    @Resource
    private AppCustomCategoryDisplayStateService appCustomCategoryDisplayStateService;

    @Resource
    private StaticDataService staticDataService;


    @Resource
    private UserService userService;

    @Value("${spring.imgRequestHost}")
    private String imgRequestHost;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void saveAppExclusiveBanner(@NonNull AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) throws BizException {
        //banner 校验可用数量 如果超过10条 不允许新增
        if(appExclusiveBannerDTO.getDisplayType().equals(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode())){
            HashMap<String, Object> enableMaps = Maps.newHashMapWithExpectedSize(1);
            enableMaps.put("state", 1);
            enableMaps.put("displayType", appExclusiveBannerDTO.getDisplayType());
            int enableCount = this.count(enableMaps);
            if(enableCount > 9){
                throw new BizException(I18nUtils.get("banner.add.max", getLang(request)));
            }
            //查询限制数量为不限制的数量  没有的话 这个必须为99999
            HashMap<String, Object> limitCountMaps = Maps.newHashMapWithExpectedSize(1);
            limitCountMaps.put("state", 1);
            limitCountMaps.put("limitTimes", DataEnum.APP_BANNER_LIMITS_MAX.getCode());
            int limmitCount = this.count(limitCountMaps);
            if(limmitCount == 0 && !appExclusiveBannerDTO.getLimitTimes().equals(DataEnum.APP_BANNER_LIMITS_MAX.getCode())){
                throw new BizException(I18nUtils.get("banner.limits.max", getLang(request)));
            }
        }else if(appExclusiveBannerDTO.getDisplayType().equals(DataEnum.APP_DISPLAY_TYPE_EXCLUSIVE.getCode())){
            //市场推广限制数量为10条
            HashMap<String, Object> enableMaps = Maps.newHashMapWithExpectedSize(2);
            enableMaps.put("state", 1);
            enableMaps.put("displayType", appExclusiveBannerDTO.getDisplayType());
            int enableCount = this.count(enableMaps);
            if(enableCount > 9){
                throw new BizException(I18nUtils.get("banner.add.max", getLang(request)));
            }
        }

        AppExclusiveBanner appExclusiveBanner = BeanUtil.copyProperties(appExclusiveBannerDTO, new AppExclusiveBanner());
        log.info("save AppExclusiveBanner:{}", appExclusiveBanner);
        AppExclusiveBanner saveRecord = (AppExclusiveBanner) this.packAddBaseProps(appExclusiveBanner, request);
        if (appExclusiveBannerDAO.insert(saveRecord) != 1) {
            log.error("insert error, data:{}", appExclusiveBanner);
            throw new BizException("Insert appExclusiveBanner Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAppExclusiveBannerList(@NonNull List<AppExclusiveBanner> appExclusiveBannerList, HttpServletRequest request) throws BizException {
        if (appExclusiveBannerList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = appExclusiveBannerDAO.insertList(appExclusiveBannerList);
        if (rows != appExclusiveBannerList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, appExclusiveBannerList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateAppExclusiveBanner(@NonNull Long id, @NonNull AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) throws BizException {
        log.info("full update appExclusiveBannerDTO:{}", appExclusiveBannerDTO);
        AppExclusiveBanner appExclusiveBanner = BeanUtil.copyProperties(appExclusiveBannerDTO, new AppExclusiveBanner());
        appExclusiveBanner.setId(id);

        AppExclusiveBannerDTO appExclusiveBannerById = this.findAppExclusiveBannerById(id);
        if (Objects.isNull(appExclusiveBannerById)) {
            throw new BizException(I18nUtils.get("banner.not.exist", getLang(request)));
        }

        //banner的限制次数修改
        if(appExclusiveBanner.getDisplayType().equals(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode())){
            int limmitCount = appExclusiveBannerDAO.getLimitsCountByUpdate(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode(), id, DataEnum.APP_BANNER_LIMITS_MAX.getCode());
            if(limmitCount == 0 && !appExclusiveBannerDTO.getLimitTimes().equals(DataEnum.APP_BANNER_LIMITS_MAX.getCode())){
                throw new BizException(I18nUtils.get("banner.limits.max", getLang(request)));
            }
        }

        //将原来的数据删除
        this.deleteAppExclusiveBanner(id, request);

        //新增
        AppExclusiveBanner saveRecordNew = new AppExclusiveBanner();

        saveRecordNew.setState(appExclusiveBannerById.getState());
        saveRecordNew.setStatus(appExclusiveBannerById.getStatus());

        saveRecordNew.setDisplayType(appExclusiveBanner.getDisplayType());
        saveRecordNew.setDisplayOrder(appExclusiveBanner.getDisplayOrder());
        saveRecordNew.setTitle(appExclusiveBanner.getTitle());
        saveRecordNew.setSubTitle(appExclusiveBanner.getSubTitle());
        saveRecordNew.setImageUrl(appExclusiveBanner.getImageUrl());
        saveRecordNew.setLimitTimes(appExclusiveBanner.getLimitTimes());
        saveRecordNew.setRedirectType(appExclusiveBanner.getRedirectType());
        Integer redirectType = appExclusiveBanner.getRedirectType();
        //根据跳转类型封装不同参数
        if(Objects.nonNull(redirectType)){
            if(redirectType.equals(DataEnum.APP_BANNER_REDIRECT_TYPE_H5.getCode())){
                saveRecordNew.setRedirectH5LinkAddress(appExclusiveBanner.getRedirectH5LinkAddress());
            }else if(redirectType.equals(DataEnum.APP_BANNER_REDIRECT_TYPE_APP.getCode())){
                saveRecordNew.setRedirectAppLinkType(appExclusiveBanner.getRedirectAppLinkType());
            }else if(redirectType.equals(DataEnum.APP_BANNER_REDIRECT_TYPE_CUSTOM.getCode())){
                saveRecordNew.setRedirectCustomizedDisplayType(appExclusiveBanner.getRedirectCustomizedDisplayType());
                saveRecordNew.setRedirectCustomizedTitle(appExclusiveBanner.getRedirectCustomizedTitle());
                saveRecordNew.setRedirectCustomizedImageUrl(appExclusiveBanner.getRedirectCustomizedImageUrl());
                saveRecordNew.setRedirectCustomizedContent(appExclusiveBanner.getRedirectCustomizedContent());
            }
        }
        saveRecordNew.setTurnOffEffectStatus(appExclusiveBanner.getTurnOffEffectStatus());

        //首页banner 关闭效果参数封装
        if(appExclusiveBanner.getDisplayType().equals(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode())){
            //关闭效果
            if(appExclusiveBanner.getTurnOffEffectStatus().equals(DataEnum.APP_BANNER_TURNOFF_STATUS_SHOW.getCode())){
                //展示
                saveRecordNew.setTurnOffTextDisplay(appExclusiveBanner.getTurnOffTextDisplay());
                saveRecordNew.setTurnOffRedirectType(appExclusiveBanner.getTurnOffRedirectType());
                //关闭效果跳转类型
                Integer turnOffRedirectType = appExclusiveBanner.getTurnOffRedirectType();
                if(Objects.nonNull(turnOffRedirectType)){
                    if(turnOffRedirectType.equals(DataEnum.APP_BANNER_TURNOFF_STATUS_REDIRECT_TYPE_H5.getCode())){
                        saveRecordNew.setTurnOffRedirectH5Link(appExclusiveBanner.getTurnOffRedirectH5Link());
                    }else if(turnOffRedirectType.equals(DataEnum.APP_BANNER_TURNOFF_STATUS_REDIRECT_TYPE_APP.getCode())){
                        saveRecordNew.setTurnOffRedirectAppLinkType(appExclusiveBanner.getTurnOffRedirectAppLinkType());
                    }
                }
            }
        }

        log.info("save AppExclusiveBanner:{}", saveRecordNew);
        AppExclusiveBanner saveRecord = (AppExclusiveBanner) this.packAddBaseProps(saveRecordNew, request);
        if (appExclusiveBannerDAO.insert(saveRecord) != 1) {
            log.error("insert error, data:{}", saveRecordNew);
            throw new BizException("Insert appExclusiveBanner Error!");
        }

        //更新redis 修改时间
        this.updateBannerRedisTimestamp(id, appExclusiveBannerDTO.getDisplayType());
    }

    /**
     * 更改banner 可用 不可用状态
     *
     * @param id
     * @param appExclusiveBannerDTO
     * @param request
     * @author zhangzeyuan
     * @date 2021/4/12 14:49
     */
    @Override
    public void updateBannerEnableStatus(Long id, AppExclusiveBannerDTO appExclusiveBannerDTO, HttpServletRequest request) throws BizException {
        Integer state = appExclusiveBannerDTO.getState();
        Integer displayType = appExclusiveBannerDTO.getDisplayType();
        if (Objects.isNull(id) || Objects.isNull(state) || Objects.isNull(displayType)) {
            throw new BizException(I18nUtils.get("parameters.null", getLang(request)));
        }

        AppExclusiveBannerDTO banner = this.findAppExclusiveBannerById(id);
        if (Objects.isNull(banner)) {
            throw new BizException(I18nUtils.get("banner.not.exist", getLang(request)));
        }

        Integer newOrder = 999;
        AppExclusiveBanner updateRecord = new AppExclusiveBanner();
        updateRecord.setId(id);
        updateRecord.setState(state);
        if (state.equals(0)) {
            //置为不可用
            //banner必须有一个可用的数据
            if(banner.getDisplayType().equals(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode())){
                HashMap<String, Object> enableCount = Maps.newHashMapWithExpectedSize(2);
                enableCount.put("state", 1);
                enableCount.put("displayType", DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode());
                int count = appExclusiveBannerDAO.count(enableCount);
                if(count < 2){
                    throw new BizException(I18nUtils.get("banner.enable.size.min", getLang(request)));
                }
            }
            //求一个 不可用列表中最大的值 作为该行的 order
            Integer maxOrder = appExclusiveBannerDAO.getMaxOrderByState(state, displayType);
            if (Objects.nonNull(maxOrder)) {
                newOrder = maxOrder + 1;
            }
            //将大于该行排序的order  - 1
            appExclusiveBannerDAO.updateOthersOrder(banner.getDisplayOrder(), banner.getState(), displayType, getUserId(request), System.currentTimeMillis());
        } else {

            HashMap<String, Object> enableMaps = Maps.newHashMapWithExpectedSize(1);
            enableMaps.put("state", 1);
            enableMaps.put("displayType", appExclusiveBannerDTO.getDisplayType());
            int enableCount = this.count(enableMaps);
            if(enableCount > 9){
                throw new BizException(I18nUtils.get("banner.add.max", getLang(request)));
            }

            //不可用 转为 可用
            Integer maxOrder = appExclusiveBannerDAO.getMaxOrderByState(state, displayType);
            if (Objects.nonNull(maxOrder)) {
                newOrder = maxOrder + 1;
            } else {
                newOrder = 1;
            }
        }
        updateRecord.setDisplayOrder(newOrder);
        //update
        int update = appExclusiveBannerDAO.update((AppExclusiveBanner) this.packModifyBaseProps(updateRecord, request));
        if (update != 1) {
            log.error("banner置为可用不可用出错, data:{}", appExclusiveBannerDTO);
            throw new BizException("update appExclusiveBanner Error!");
        }
        this.updateBannerRedisTimestamp(id, displayType);
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
        AppExclusiveBannerDTO banner = this.findAppExclusiveBannerById(id);
        if (Objects.isNull(banner)) {
            throw new BizException(I18nUtils.get("banner.not.exist", getLang(request)));
        }
        //当前行排序
        Integer displayOrder = banner.getDisplayOrder();
        Integer displayType = banner.getDisplayType();

        //当前行更新记录
        AppExclusiveBanner currentUpdateRecord = new AppExclusiveBanner();
        currentUpdateRecord.setId(banner.getId());
        currentUpdateRecord = (AppExclusiveBanner) this.packModifyBaseProps(currentUpdateRecord, request);
        //要移动的行更新记录
        AppExclusiveBanner movedUpdateRecord = new AppExclusiveBanner();
        movedUpdateRecord = (AppExclusiveBanner) this.packModifyBaseProps(movedUpdateRecord, request);

        //0：上移 1：下移
        if (status.equals(1)) {
            //取下一条记录
            AppExclusiveBannerDTO nextOrderBannerInfo = appExclusiveBannerDAO.getNextOrderBannerInfo(displayOrder, displayType);
            if (Objects.isNull(nextOrderBannerInfo)) {
                throw new BizException(I18nUtils.get("banner.not.exist", getLang(request)));

            }
            movedUpdateRecord.setId(nextOrderBannerInfo.getId());
            movedUpdateRecord.setDisplayOrder(displayOrder);

            currentUpdateRecord.setDisplayOrder(nextOrderBannerInfo.getDisplayOrder());
        } else if (status.equals(0)) {
            ///取上一条记录
            AppExclusiveBannerDTO lastOrderBannerInfo = appExclusiveBannerDAO.getLastOrderBannerInfo(displayOrder, displayType);
            if (Objects.isNull(lastOrderBannerInfo)) {
                throw new BizException(I18nUtils.get("banner.not.exist", getLang(request)));
            }
            movedUpdateRecord.setId(lastOrderBannerInfo.getId());
            movedUpdateRecord.setDisplayOrder(displayOrder);
            currentUpdateRecord.setDisplayOrder(lastOrderBannerInfo.getDisplayOrder());
        }
        //更新
        appExclusiveBannerDAO.update(currentUpdateRecord);
        appExclusiveBannerDAO.update(movedUpdateRecord);
        //更新redis
        this.updateBannerRedisTimestamp(currentUpdateRecord.getId(), displayType);
        this.updateBannerRedisTimestamp(movedUpdateRecord.getId(), displayType);
    }

    /**
     * 添加记录时获取下一个排序
     *
     * @param displayType
     * @return java.lang.Integer
     * @author zhangzeyuan
     * @date 2021/4/21 18:24
     */
    @Override
    public Integer getAddNextOrder(Integer displayType) {
        Integer resultOrder = 1;
        Integer maxOrder = appExclusiveBannerDAO.getMaxOrderByState(1, displayType);
        if (Objects.nonNull(maxOrder)) {
            resultOrder = maxOrder + 1;
        }
        return resultOrder;
    }

    @Override
    public void updateAppExclusiveBannerSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        appExclusiveBannerDAO.updatex(params);
    }

    @Override
    public void logicDeleteAppExclusiveBanner(@NonNull Long id, Integer type, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);

        AppExclusiveBannerDTO appExclusiveBanner = this.findAppExclusiveBannerById(id);
        if(Objects.isNull(appExclusiveBanner)){
            throw new BizException(I18nUtils.get("banner.not.exist", getLang(request)));
        }

        //最少保留一条数据
        Map<String, Object> countParams = new HashMap<>(3);
        countParams.put("displayType", type);
        countParams.put("status", 1);
        int count = appExclusiveBannerDAO.count(countParams);
        if(count < 2){
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }

        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = appExclusiveBannerDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }

        //更新这个order下面的排序
        appExclusiveBannerDAO.updateOthersOrder(appExclusiveBanner.getDisplayOrder(), 1, appExclusiveBanner.getDisplayType(), getUserId(request), System.currentTimeMillis());


        //初始化redis 更新时间
        //根据id获取displayType
        Map<String, Object> paramsById = new HashMap<>(1);
        paramsById.put("id", id);
        paramsById.put("status", 0);
//        AppExclusiveBanner appExclusiveBanner = appExclusiveBannerDAO.selectOne(paramsById);
        this.updateBannerRedisTimestamp(appExclusiveBanner.getId(), appExclusiveBanner.getDisplayType());

    }

    /**
     * 更新reids banner更新时间
     *
     * @param displayType
     * @author zhangzeyuan
     * @date 2021/4/27 15:32
     */
    private void updateBannerRedisTimestamp(Long id, Integer displayType) {
        String redisKey = "";
        if (displayType.equals(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode())) {
            redisKey = Constant.getBannerUpdateTimestampRedisKey(id);
        } else if (displayType.equals(DataEnum.APP_DISPLAY_TYPE_EXCLUSIVE.getCode())) {
            redisKey = Constant.APP_EXCLUSIVE_BANNER_UPDATE_TIMESTAMP_REDIS_KEY;
        } else {
            return;
        }
        redisUtils.set(redisKey, System.currentTimeMillis());
    }


    @Override
    public void deleteAppExclusiveBanner(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = appExclusiveBannerDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public AppExclusiveBannerDTO findAppExclusiveBannerById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        AppExclusiveBannerDTO appExclusiveBannerDTO = appExclusiveBannerDAO.selectOneDTO(params);
        return appExclusiveBannerDTO;
    }

    @Override
    public AppExclusiveBannerDTO findOneAppExclusiveBanner(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        AppExclusiveBanner appExclusiveBanner = appExclusiveBannerDAO.selectOne(params);
        AppExclusiveBannerDTO appExclusiveBannerDTO = new AppExclusiveBannerDTO();
        if (null != appExclusiveBanner) {
            BeanUtils.copyProperties(appExclusiveBanner, appExclusiveBannerDTO);
        }
        return appExclusiveBannerDTO;
    }

    @Override
    public List<AppExclusiveBannerDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params = getUnionParams(params, scs, pc);
        List<AppExclusiveBannerDTO> resultList = appExclusiveBannerDAO.selectDTOByPage(params);
        return resultList;
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return appExclusiveBannerDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return appExclusiveBannerDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = appExclusiveBannerDAO.groupCount(conditions);
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
        return appExclusiveBannerDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = appExclusiveBannerDAO.groupSum(conditions);
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
     * 获取APPbanner数据
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/4/8 15:18
     */
    @Override
    public JSONObject getAppHomePageTopBanner(JSONObject requestInfo, HttpServletRequest request) {
        JSONObject result = new JSONObject(3);
        //时间戳 todo
        String requestTimestamp = requestInfo.getJSONObject("data").getString("timestamp");

        //当前显示的bannerId
        String bannerId = requestInfo.getJSONObject("data").getString("bannerId");
        //用户id
        Long userId = getUserId(request);

        Long currentTimeMillis = System.currentTimeMillis();

        //返回banner数据
        AppExclusiveBannerDTO resultBanner = null;
        //查询列表
        List<AppExclusiveBannerDTO> bannerList = appExclusiveBannerDAO.listAppHomePageTopBanner(DataEnum.APP_BANNER_DISPLAY_TYPE_BANNER.getCode());
        for (AppExclusiveBannerDTO bannerDTO : bannerList) {
            //配置次数
            Integer limitTimes = bannerDTO.getLimitTimes();
            //不限制次数  todo 新增时新增必须一个999999的校验
            if (limitTimes.compareTo(Constant.APP_BANNER_LIMITS_UNLIMITED) == 0) {
                resultBanner = bannerDTO;
                break;
            }
            //redis 计数
            Integer bannerRedisLimits = this.getBannerRedisLimitsById(bannerDTO.getId(), userId);

            //判断限制次数 和 redis 计数次数
            //判断
            if (limitTimes.compareTo(bannerRedisLimits) > 0) {
                resultBanner = bannerDTO;
                break;
            } else {
                //取下一个数据 必有一个999999的数据
                continue;
            }
        }

        //没有数据直接返回
        if(Objects.isNull(resultBanner)){
            result.put("banner", new AppExclusiveBannerDTO());
            result.put("updateStatus", false);
            result.put("timestamp", currentTimeMillis.toString());
            return result;
        }

        //处理图片为base64数据
        this.handleBannerBase64Image(resultBanner);

        //redis + 1
        this.setBannerRedisLimits(resultBanner.getId(), userId);

        //时间为空 是第一次访问  需要更新
        if (StringUtils.isBlank(requestTimestamp)) {
            result.put("banner", resultBanner);
            result.put("updateStatus", true);
            result.put("timestamp", currentTimeMillis.toString());
            return result;
        }

        String requestBannerId = resultBanner.getId().toString();
        //传时间戳 没传bannnerId  直接返回不用更新
        if(StringUtils.isBlank(requestBannerId)){
            result.put("banner", new AppExclusiveBannerDTO());
            result.put("updateStatus", false);
            result.put("timestamp", currentTimeMillis.toString());
            return result;
        }

        //对比app 传过来的 bannerId   和 要显示的bannerId
        if (requestBannerId.equals(bannerId)) {
            //一样的话 根据时间戳和reids时间戳对比是否要更新
            boolean needUpdateStatus = isNeedUpdateBannerByTimestamp(Long.valueOf(requestTimestamp), resultBanner.getId());
            if (needUpdateStatus) {
                result.put("banner", resultBanner);
                result.put("updateStatus", true);
                result.put("timestamp", currentTimeMillis.toString());
                return result;
            } else {
                //不需要更新
                result.put("banner", new AppExclusiveBannerDTO());
                result.put("updateStatus", false);
                result.put("timestamp", requestTimestamp);
                return result;
            }
        } else {
            //不一样的话需要强制更新
            result.put("banner", resultBanner);
            result.put("updateStatus", true);
            result.put("timestamp", currentTimeMillis.toString());
            return result;
        }
    }


    /**
     * 将图片信息转换为base64格式
     *
     * @param bannerData
     * @author zhangzeyuan
     * @date 2021/4/25 9:57
     */
    private void handleBannerBase64Image(AppExclusiveBannerDTO bannerData) {
        //处理图片
        String imageUrl = bannerData.getImageUrl();
        String base64Image = "";
        if (StringUtils.isNotBlank(imageUrl)) {
            base64Image = QRCodeUtil.imageUrlToBase64(imgRequestHost + bannerData.getImageUrl());
        }
        bannerData.setBase64Img(base64Image);
    }

    /**
     * 获取redis中banner对应的限制次数
     *
     * @param bannerId
     * @param userId
     * @return java.lang.Integer
     * @author zhangzeyuan
     * @date 2021/4/23 16:17
     */
    private Integer getBannerRedisLimits(Long bannerId, Long userId) {
        //判断当前用户是否超过限制次数
        Integer userBannerLimits = 0;
        Object userBannerLimitsObj = redisUtils.get(Constant.getBannerLimitsRedisKey(bannerId, userId));
        if (Objects.nonNull(userBannerLimitsObj)) {
            userBannerLimits = (Integer) userBannerLimitsObj;
        }
        return userBannerLimits;
    }


    private Integer getBannerRedisLimitsById(Long bannerId, Long userId) {
        //判断当前用户是否超过限制次数
        Integer userBannerLimits = 0;
        Object userBannerLimitsObj = redisUtils.get(Constant.getBannerLimitsRedisKey(bannerId, userId));
        if (Objects.nonNull(userBannerLimitsObj)) {
            userBannerLimits = (Integer) userBannerLimitsObj;
        }
        return userBannerLimits;
    }


    private boolean isNeedUpdateBannerByTimestamp(Long requestTimestamp, Long id) {
        //去redis判断该banner是否要更新
        Object redisObject = redisUtils.get(Constant.getBannerUpdateTimestampRedisKey(id));
        if (Objects.nonNull(redisObject)) {
            //理论情况不为空 新增的时候初始化 todo 需要注意
            //redis时间戳
            Long redisLongTime = Long.valueOf(redisObject.toString());

            //请求时间戳比redis时间戳小 则需要更新
            if (requestTimestamp.compareTo(redisLongTime) < 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * banner次数限制+1
     *
     * @param bannerId
     * @param userId
     * @return java.lang.Long
     * @author zhangzeyuan
     * @date 2021/4/23 16:55
     */
    private Long setBannerRedisLimits(Long bannerId, Long userId) {
        return redisUtils.incr(Constant.getBannerLimitsRedisKey(bannerId, userId), 1);
    }


    /**
     * banner是否需要更新 并将返回给app的时间戳放入result
     *
     * @param requestTimestamp
     * @return boolean
     * @author zhangzeyuan
     * @date 2021/4/23 14:37
     */
    private boolean needUpdateBanner(String requestTimestamp, JSONObject result) {
        //判断redis中 banner是否更新过
        Object redisObject = redisUtils.get(Constant.APP_BANNER_UPDATE_TIMESTAMP_REDIS_KEY);
        if (Objects.isNull(redisObject)) {
            //理论情况不为空 新增的时候初始化
            //如果为空则认为需要更新   将redis重新赋值  设置redis值比返回给app的时间戳小一点 下次则不用更新
            Long tempTamps = System.currentTimeMillis();

            redisUtils.set(Constant.APP_BANNER_UPDATE_TIMESTAMP_REDIS_KEY, tempTamps - 1000);
            result.put("timestamp", tempTamps.toString());
            return true;
        }
        //redis时间戳
        String redisTimeStamp = redisObject.toString();
        Long redisLongTime = Long.valueOf(redisTimeStamp);
        result.put("timestamp", redisTimeStamp);

        if (StringUtils.isBlank(requestTimestamp)) {
            //时间为空 是第一次访问  需要更新
            return true;
        }

        //请求时间戳
        Long requestLongTime = Long.valueOf(requestTimestamp);

        //请求时间戳比redis时间戳小 则需要更新
        if (requestLongTime.compareTo(redisLongTime) < 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取APP主页市场推广、自定义分类展示数据
     *
     * @param requestInfo
     * @param request
     * @return com.alibaba.fastjson.JSONObject
     * @author zhangzeyuan
     * @date 2021/4/8 16:27
     */
    @Override
    public JSONObject listAllAppHomePageBottomData(JSONObject requestInfo, HttpServletRequest request) {
        //todo
        JSONObject result = new JSONObject();
        //市场营销模块数据
        //更新时间戳
        String exclusiveTimestamp = requestInfo.getJSONObject("data").getString("exclusiveTimestamp");
        this.packageExclusiveData(exclusiveTimestamp, result);

        //自定义分类模块

        //用户经度
        String longitude = requestInfo.getJSONObject("data").getString("longitude");
        //用户纬度
        String latitude = requestInfo.getJSONObject("data").getString("latitude");

        //根据经纬度获取州名 默认为 todo 抽取
        String stateName = "QLD";
        JSONObject locationJson = MerchantListUtils.getCityStreet(latitude, longitude);
        //更新用户所在州
        userService.getUserStateCityByLongitude(locationJson, request);
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

        //遍历 封装每一行的数据
        Long userId = getUserId(request);
        //Long userId = Long.valueOf("517550805056442368");


        //todo 测试挡板
        /*if(userId.toString().equals("517550805056442368")){
            HashMap<String, Object> objectObjectHashMap = Maps.newHashMapWithExpectedSize(1);
            objectObjectHashMap.put("code", "testCategoryState");
            StaticDataDTO staticData = staticDataService.findOneStaticData(objectObjectHashMap);
            if(staticData != null && StringUtils.isNotBlank(staticData.getEnName())){
                stateName = staticData.getEnName();
            }
        }*/

        //获取redis中存放用户所在州信息
        String userRedisStateStr = "";
        Object userRedisState = redisUtils.get(Constant.getAppHomepageCategoryUserState(userId));
        if(Objects.isNull(userRedisState)){
            //redis没有数据
        }else {
            userRedisStateStr =  (String) userRedisState;
        }

        int length = 6;

        //查询分类信息
        JSONObject param = new JSONObject(3);
        param.put("code", "merchantCategories");
        Map<String, String> categoryMap = staticDataService.find(param, null, null).stream().collect(Collectors.toMap(StaticDataDTO::getValue, StaticDataDTO::getEnName));

        //遍历封装每一层数据
        for (int i = 1; i < length; i++) {
            this.packageAppHomePageCustomCategoryData(i, stateName, longitude, latitude, requestInfo, result, userId, categoryMap, userRedisStateStr);
        }
        //更新redis 用户所在州数据
        redisUtils.set(Constant.getAppHomepageCategoryUserState(userId), stateName);
//        log.info("==============================");
//        log.info("result:"+result);
        return result;
    }

    /**
     * 分页查询
     *
     * @param params
     * @param scs
     * @param pc
     * @return java.util.List<com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO>
     * @author zhangzeyuan
     * @date 2021/5/8 16:55
     */
    @Override
    public List<AppExclusiveBannerDTO> listBanner(Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        return null;
    }


    /**
     * 封装市场推广数据
     *
     * @param exclusiveTimestamp
     * @param result
     * @author zhangzeyuan
     * @date 2021/4/8 17:06
     */
    private void packageExclusiveData(String exclusiveTimestamp, JSONObject result) {
        JSONObject exclusiveResultJson = new JSONObject(3);

        Long currentTimeMillis = System.currentTimeMillis();

        if (StringUtils.isBlank(exclusiveTimestamp)) {
            //第一次为空 需要更新
            //查询数据
            List<AppExclusiveBannerDTO> data = appExclusiveBannerDAO.listAppHomePageTopBanner(DataEnum.APP_DISPLAY_TYPE_EXCLUSIVE.getCode());
            if(CollectionUtils.isNotEmpty(data)){
                exclusiveResultJson.put("list", data);
            }else {
                exclusiveResultJson.put("list", Collections.emptyList());
            }
            exclusiveResultJson.put("updateStatus", true);
            //每次将redis的最后更新时间返回给app
            exclusiveResultJson.put("exclusiveTimestamp", currentTimeMillis.toString());
            result.put("exclusive", exclusiveResultJson);
            return;
        }

        //是否要更新
        boolean exclusiveUpdateStatus = false;

        //todo redis key 长度
        Object redisObject = redisUtils.get(Constant.APP_EXCLUSIVE_BANNER_UPDATE_TIMESTAMP_REDIS_KEY);
        if (Objects.nonNull(redisObject)) {
            Long requestTime = Long.valueOf(exclusiveTimestamp);
            Long redisTime = Long.valueOf(redisObject.toString());
            if (requestTime.compareTo(redisTime) < 0) {
                exclusiveUpdateStatus = true;
            }
        }

        exclusiveResultJson.put("updateStatus", exclusiveUpdateStatus);
        if (exclusiveUpdateStatus) {
            //查询数据
            List<AppExclusiveBannerDTO> data = appExclusiveBannerDAO.listAppHomePageTopBanner(DataEnum.APP_DISPLAY_TYPE_EXCLUSIVE.getCode());
            if(CollectionUtils.isNotEmpty(data)){
                exclusiveResultJson.put("list", data);
            }else {
                exclusiveResultJson.put("list", Collections.emptyList());
            }
            exclusiveResultJson.put("exclusiveTimestamp", currentTimeMillis.toString());
        } else {
            exclusiveResultJson.put("list", Collections.emptyList());
            exclusiveResultJson.put("exclusiveTimestamp", exclusiveTimestamp);
        }

        result.put("exclusive", exclusiveResultJson);
    }


    /**
     * 封装APP主页自定义分类数据
     *
     * @param order
     * @param stateName
     * @param userLongitude
     * @param userLatitude
     * @param requestInfo
     * @param result
     * @param userId
     * @author zhangzeyuan
     * @date 2021/4/13 17:32
     */
    private void packageAppHomePageCustomCategoryData(Integer order, String stateName, String userLongitude, String userLatitude,
                                                      JSONObject requestInfo, JSONObject result,
                                                      Long userId, Map<String, String> categoryMap,String userRedisStateStr) {
        JSONObject categoryResultJson = new JSONObject(3);
        //请求时间戳信息
        String tempSplitStr = "category" + order + "Timestamp";
        String requestTimestamp = requestInfo.getJSONObject("data").getString(tempSplitStr);

        //是否要更新
        boolean updateStatus = false;

        out:if(StringUtils.isBlank(requestTimestamp)){
            updateStatus = true;
        }else {
            //时间戳不为空  需要先判断 用户所在州是否切换 切换则需要更新 没有切换则判断分类信息是否更新
            if(userRedisStateStr.equals("")){
                //第一次需要更新
                updateStatus = true;
                break out;
            }else {
                if(!userRedisStateStr.equals(stateName)){
                    updateStatus = true;
                    break out;
                }
            }

            String redisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(order, stateName);
            Object redisObj = redisUtils.get(redisKey);
            if (Objects.nonNull(redisObj)) {
                Long requestTime = Long.valueOf(requestTimestamp);
                Long redisTime = Long.valueOf(redisObj.toString());
                if (requestTime.compareTo(redisTime) < 0) {
                    updateStatus = true;
                }
            }
        }

        categoryResultJson.put("updateStatus", updateStatus);
        AppCustomCategoryDisplayDTO resultObj = new AppCustomCategoryDisplayDTO();

        AppCustomCategoryDisplayDTO appHomePageData2 = appCustomCategoryDisplayService.getAppHomePageDataByOrderAndState(order, stateName);
        Map merchantDisplayTypeMap2 = new HashMap();
        merchantDisplayTypeMap2.put("displayOrder", appHomePageData2.getDisplayOrder());
        merchantDisplayTypeMap2.put("stateName", stateName);
        AppCustomCategoryDisplayStateDTO stateDTO2 = appCustomCategoryDisplayStateService.findOneAppCustomCategoryDisplayState(merchantDisplayTypeMap2);
        Integer distance2 = 2;
        if (distance2.equals(stateDTO2.getMerchantShowType())) {
            updateStatus = true;

        }

        if (updateStatus) {
            categoryResultJson.put(tempSplitStr, Long.valueOf(System.currentTimeMillis()).toString());
            //查询对应order数据
            AppCustomCategoryDisplayDTO appHomePageData = appCustomCategoryDisplayService.getAppHomePageDataByOrderAndState(order, stateName);
            if (Objects.nonNull(appHomePageData)) {
                //处理分类名称
                if (Objects.nonNull(appHomePageData.getCategoryType())) {
                    int categoryType = appHomePageData.getCategoryType().intValue();
                    String categoryName = "";
                    String tempCategoryName = categoryMap.get(appHomePageData.getCategoryType().toString());
                    if (StringUtils.isNotBlank(tempCategoryName)) {
                        categoryName = tempCategoryName.toLowerCase();
                        if (categoryType == DataEnum.MERCHANT_CATEGORY_CAFE.getCode() ||
                                categoryType == DataEnum.MERCHANT_CATEGORY_BAR.getCode()) {
                            categoryName = tempCategoryName.toLowerCase() + "s";
                        } else if (categoryType == DataEnum.MERCHANT_CATEGORY_EXCLUSIVE_OFFER.getCode()) {
                            categoryName = "offers";
                        }
                    }
                    appHomePageData.setCategoryName(categoryName);
                }
//                System.out.println(appHomePageData);
//                appHomePageData.setCategoryType(null);
                //判断是自定义还是距离
                Map merchantDisplayTypeMap = new HashMap();
                merchantDisplayTypeMap.put("displayOrder", appHomePageData.getDisplayOrder());
                merchantDisplayTypeMap.put("stateName", stateName);
                AppCustomCategoryDisplayStateDTO stateDTO = appCustomCategoryDisplayStateService.findOneAppCustomCategoryDisplayState(merchantDisplayTypeMap);

                //距离为2
                Integer distance = 2;
                if (distance.equals(stateDTO.getMerchantShowType())) {
                    categoryResultJson.put("updateStatus", true);
                    if (StringUtils.isNotBlank(appHomePageData.getMerchantIds())) {
                        Map staticDateMap = new HashMap();
                        staticDateMap.put("enName", stateName);
                        staticDateMap.put("code", "merchantState");
                        StaticDataDTO dataDTO = staticDataService.findOneStaticData(staticDateMap);
                        List<MerchantAppHomePageDTO> merchantList = merchantDAO.getMerchantListDistance(dataDTO.getValue(),stateDTO.getCategoryType(), userId);
                        if (CollectionUtils.isEmpty(merchantList)) {
                            appHomePageData.setMerchants(Collections.emptyList());
                        } else {
                            for (MerchantAppHomePageDTO merchant : merchantList) {
                                //根据经纬度计算距离
                                String distanceStr = "";
                                BigDecimal distanceOrder = BigDecimal.ZERO;
                                BigDecimal distanceDecimal = MerchantListUtils.calDistanceAppHome(merchant, userLatitude, userLongitude);
                                if (Objects.nonNull(distanceDecimal)) {
                                    distanceStr = distanceDecimal.toString();
                                    distanceOrder = distanceDecimal;
                                }
                                merchant.setDistanceOrder(distanceOrder);
                                merchant.setDistance(distanceStr);

                                merchant.setLat(null);
                                merchant.setLng(null);
                                //分类名称
                                Integer categories = merchant.getCategories();
                                if (Objects.nonNull(categories)) {
                                    merchant.setCategoryName(categoryMap.get(categories.toString()));
                                }
                                merchant.setCategories(null);
                                //tag信息
                                String keyword = merchant.getKeyword();
                                //将keyword字段中','分割的tag封装成 List<String> ,返回最多3个tag(不管有几个)
                                if (StringUtils.isNotBlank(keyword)) {
                                    merchant.setTags(Arrays.stream(keyword.split(",")).filter(StringUtils::isNotBlank).limit(3).collect(Collectors.toList()));
                                } else {
                                    merchant.setTags(new ArrayList<>());
                                }
                                merchant.setKeyword(null);
                                //折扣信息
                                merchant.setUserDiscount(MerchantListUtils.setCardInstallmentDiscountByAppHomePageDTO(merchant));

                                //清空无关信息
                                merchant.setHaveWholeSell(null);
                                merchant.setWholeSaleUserDiscount(null);
                                merchant.setWholeSaleUserDiscount(null);
                                merchant.setMarketingDiscount(null);
                                merchant.setExtraDiscount(null);
                                merchant.setExtraDiscountPeriod(null);
                                merchant.setBasePayRate(null);
                                merchant.setBaseRate(null);
                            }
                            List<MerchantAppHomePageDTO> merchantListSorted = merchantList.stream().sorted(Comparator.comparing(MerchantAppHomePageDTO::getDistanceOrder)).limit(10).collect(Collectors.toList());
                            if(merchantListSorted.size() == 0){
                                appHomePageData.setMerchantIds(null);
                            }
                            appHomePageData.setMerchants(merchantListSorted);
                        }
                        appHomePageData.setMerchantIds(null);
                    }else {
                        appHomePageData.setMerchants(Collections.emptyList());
                    }
                } else {

                    //处理商户信息
                    if (StringUtils.isNotBlank(appHomePageData.getMerchantIds())) {
                        //根据ID查询商户信息
                        List<MerchantAppHomePageDTO> merchants = merchantDAO.getMerchantListByIds(appHomePageData.getMerchantIds(), userId);
                        if (CollectionUtils.isEmpty(merchants)) {
                            appHomePageData.setMerchants(Collections.emptyList());
                        } else {
                            for (MerchantAppHomePageDTO merchant : merchants) {
                                //根据经纬度计算距离
                                String distanceStr = "";
                                BigDecimal distanceDecimal = MerchantListUtils.calDistanceAppHome(merchant, userLatitude, userLongitude);
                                if (Objects.nonNull(distanceDecimal)) {
                                    distanceStr = distanceDecimal.toString();
                                }
                                merchant.setDistance(distanceStr);
                                merchant.setLat(null);
                                merchant.setLng(null);
                                //分类名称
                                Integer categories = merchant.getCategories();
                                if (Objects.nonNull(categories)) {
                                    merchant.setCategoryName(categoryMap.get(categories.toString()));
                                }
                                merchant.setCategories(null);
                                //tag信息
                                String keyword = merchant.getKeyword();
                                //将keyword字段中','分割的tag封装成 List<String> ,返回最多3个tag(不管有几个)
                                if (StringUtils.isNotBlank(keyword)) {
                                    merchant.setTags(Arrays.stream(keyword.split(",")).filter(StringUtils::isNotBlank).limit(3).collect(Collectors.toList()));
                                } else {
                                    merchant.setTags(new ArrayList<>());
                                }
                                merchant.setKeyword(null);
                                //折扣信息
                                merchant.setUserDiscount(MerchantListUtils.setCardInstallmentDiscountByAppHomePageDTO(merchant));

                                //清空无关信息
                                merchant.setHaveWholeSell(null);
                                merchant.setWholeSaleUserDiscount(null);
                                merchant.setWholeSaleUserDiscount(null);
                                merchant.setMarketingDiscount(null);
                                merchant.setExtraDiscount(null);
                                merchant.setExtraDiscountPeriod(null);
                                merchant.setBasePayRate(null);
                                merchant.setBaseRate(null);
                            }
                            appHomePageData.setMerchants(merchants);
                        }
                        appHomePageData.setMerchantIds(null);
                    } else {
                        appHomePageData.setMerchants(Collections.emptyList());
                    }

                }
                resultObj = appHomePageData;
            }
        } else {
            categoryResultJson.put(tempSplitStr, requestTimestamp);
        }

        categoryResultJson.put("data", resultObj);
        result.put("category" + order, categoryResultJson);
    }

    /**
     * 将州名转换为字段名称
     *
     * @param stateName
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/4/8 17:43
     */
    private String convertStateToColumn(String stateName) {
        String result = "";
        if (StringUtils.isBlank(stateName)) {
            return result;
        }

        switch (stateName) {
            case "1":
                result = "NSW_merchant_ids";
                break;
            case "QLD":
                result = "QLD_merchant_ids";
                break;

            case "111":
                result = "SA_merchant_ids";
                break;
            case "2111":
                result = "TAS_merchant_ids";
                break;
            case "2111111":
                result = "VIC_merchant_ids";
                break;
            case "22221":
                result = "WA_merchant_ids";
                break;
            case "212q2":
                result = "ACT_merchant_ids";
                break;
            case "qq1":
                result = "NT_merchant_ids";
                break;
            default:
                break;
        }
        return result;
    }
}
