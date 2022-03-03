package com.uwallet.pay.main.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.util.RedisUtils;
import com.uwallet.pay.main.constant.Constant;
import com.uwallet.pay.main.constant.DataEnum;
import com.uwallet.pay.main.dao.FavoriteMerchantDAO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO;
import com.uwallet.pay.main.model.dto.FavoriteMerchantDTO;
import com.uwallet.pay.main.model.dto.MerchantDTO;
import com.uwallet.pay.main.model.entity.FavoriteMerchant;
import com.uwallet.pay.main.service.*;
import com.uwallet.pay.core.common.PagingContext;
import com.uwallet.pay.core.common.SortingContext;
import com.uwallet.pay.core.service.impl.BaseServiceImpl;
import com.uwallet.pay.core.util.BeanUtil;
import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.util.I18nUtils;
import com.uwallet.pay.main.util.MerchantListUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.util.logging.Log;
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
 * 用户的收藏商户数据
 * </p>
 *
 * @package: com.uwallet.pay.main.service.impl
 * @description: 用户的收藏商户数据
 * @author: aaron S
 * @date: Created in 2021-04-07 18:04:58
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaron S
 */
@Service
@Slf4j
public class FavoriteMerchantServiceImpl extends BaseServiceImpl implements FavoriteMerchantService {

    @Autowired
    private FavoriteMerchantDAO favoriteMerchantDAO;
    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;

    @Autowired
    private AppCustomCategoryDisplayStateService appCustomCategoryDisplayStateService;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void saveFavoriteMerchant(@NonNull FavoriteMerchantDTO favoriteMerchantDTO, HttpServletRequest request) throws BizException {
        FavoriteMerchant favoriteMerchant = BeanUtil.copyProperties(favoriteMerchantDTO, new FavoriteMerchant());
        log.info("save FavoriteMerchant:{}", favoriteMerchant);
        if (favoriteMerchantDAO.insert((FavoriteMerchant) this.packAddBaseProps(favoriteMerchant, request)) != 1) {
            log.error("insert error, data:{}", favoriteMerchant);
            throw new BizException("Insert favoriteMerchant Error!");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFavoriteMerchantList(@NonNull List<FavoriteMerchant> favoriteMerchantList, HttpServletRequest request) throws BizException {
        if (favoriteMerchantList.size() == 0) {
            throw new BizException(I18nUtils.get("parameter.rule.length", getLang(request)));
        }
        int rows = favoriteMerchantDAO.insertList(favoriteMerchantList);
        if (rows != favoriteMerchantList.size()) {
            log.error("数据库实际插入成功数({})与给定的({})不一致", rows, favoriteMerchantList.size());
            throw new BizException(I18nUtils.get("batch.saving.exception", getLang(request)));
        }
    }

    @Override
    public void updateFavoriteMerchant(@NonNull Long id, @NonNull FavoriteMerchantDTO favoriteMerchantDTO, HttpServletRequest request) throws BizException {
        log.info("full update favoriteMerchantDTO:{}", favoriteMerchantDTO);
        FavoriteMerchant favoriteMerchant = BeanUtil.copyProperties(favoriteMerchantDTO, new FavoriteMerchant());
        favoriteMerchant.setId(id);
        int cnt = favoriteMerchantDAO.update((FavoriteMerchant) this.packModifyBaseProps(favoriteMerchant, request));
        if (cnt != 1) {
            log.error("update error, data:{}", favoriteMerchantDTO);
            throw new BizException("update favoriteMerchant Error!");
        }
    }

    @Override
    public void updateFavoriteMerchantSelective(@NonNull Map<String, Object> dataMap, @NonNull Map<String, Object> conditionMap) {
        log.info("part update dataMap:{}, conditionMap:{}", dataMap, conditionMap);
        Map<String, Object> params = new HashMap<>(2);
        params.put("datas", dataMap);
        params.put("conditions", conditionMap);
        favoriteMerchantDAO.updatex(params);
    }

    @Override
    public void logicDeleteFavoriteMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("逻辑删除，数据id:{}", id);
        Map<String, Object> params = new HashMap<>(3);
        params.put("id", id);
        params.put("modifiedBy", getUserId(request));
        params.put("modifiedDate", System.currentTimeMillis());
        int rows = favoriteMerchantDAO.delete(params);
        if (rows != 1) {
            log.error("逻辑删除异常, rows:{}", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public void deleteFavoriteMerchant(@NonNull Long id, HttpServletRequest request) throws BizException {
        log.info("物理删除, id:{}", id);
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        int rows = favoriteMerchantDAO.pdelete(params);
        if (rows != 1) {
            log.error("删除异常, 实际删除了{}条数据", rows);
            throw new BizException(I18nUtils.get("delete.failed", getLang(request)));
        }
    }

    @Override
    public FavoriteMerchantDTO findFavoriteMerchantById(@NonNull Long id) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("id", id);
        FavoriteMerchantDTO favoriteMerchantDTO = favoriteMerchantDAO.selectOneDTO(params);
        return favoriteMerchantDTO;
    }

    @Override
    public FavoriteMerchantDTO findOneFavoriteMerchant(@NonNull Map<String, Object> params) {
        log.info("find one params:{}", params);
        FavoriteMerchant favoriteMerchant = favoriteMerchantDAO.selectOne(params);
        FavoriteMerchantDTO favoriteMerchantDTO = new FavoriteMerchantDTO();
        if (null != favoriteMerchant) {
            BeanUtils.copyProperties(favoriteMerchant, favoriteMerchantDTO);
        }
        return favoriteMerchantDTO;
    }

    @Override
    public List<FavoriteMerchantDTO> find(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {

        List<FavoriteMerchantDTO> resultList = favoriteMerchantDAO.selectDTO(params);
        return resultList;
    }
    @Override
    public List<JSONObject> findListWithMerchantInfo(JSONObject data, Vector<SortingContext> scs, PagingContext pc) {
        String userLat = data.getString("lat");
        String userLng = data.getString("lng");
        Map<String, Object> params = getUnionParams(data, scs, pc);
        List<JSONObject> list = favoriteMerchantDAO.findListWithMerchantInfo(params);
        list = MerchantListUtils.processData(list, userLat,userLng,null);
        list.forEach(dto->{
            dto.put("isFavorite",DataEnum.IS_FAVORITE.getCode());
        });
        return list;
    }

    @Override
    public Object isUserFavorite(@NonNull Long merchantId, HttpServletRequest request) {
        JSONObject queryParam = new JSONObject(3);
        queryParam.put("userId",getUserId(request));
        queryParam.put("merchantId",merchantId);
        return count(queryParam);
    }

    @Override
    public int countFavorite(JSONObject data) {
        return favoriteMerchantDAO.countFavorite(data);
    }

    @Override
    public List<Map> findMap(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc, String... columns) throws BizException {
        if (columns.length == 0) {
            throw new BizException("columns长度不能为0");
        }
        params = getUnionParams(params, scs, pc);
        params.put("columns", columns);
        return favoriteMerchantDAO.selectMap(params);
    }

    private Map<String, Object> getUnionParams(@NonNull Map<String, Object> params, Vector<SortingContext> scs, PagingContext pc) {
        params.put("pc", pc);
        params.put("scs", scs);
        return params;
    }

    @Override
    public int count(@NonNull Map<String, Object> params) {
        return favoriteMerchantDAO.count(params);
    }

    @Override
    public Map<String, Integer> groupCount(String group, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(1);
        }
        conditions.put("group", group);
        List<Map<String, Object>> maps = favoriteMerchantDAO.groupCount(conditions);
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
        return favoriteMerchantDAO.sum(conditions);
    }

    @Override
    public Map<String, Double> groupSum(String group, String sumField, Map<String, Object> conditions) {
        if (conditions == null) {
            conditions = new HashMap<>(2);
        }
        conditions.put("group", group);
        conditions.put("sumfield", sumField);
        List<Map<String, Object>> maps = favoriteMerchantDAO.groupSum(conditions);
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

    @Override
    public void addNewFavorite(JSONObject data, HttpServletRequest request) throws BizException {
        if (data.isEmpty() || null == data.getLong("merchantId") || null == data.getInteger("isAdd")){
            throw new BizException(I18nUtils.get("parameters.error",getLang(request)));
        }
        MerchantDTO merchantDTO = merchantService.findMerchantById(data.getLong("merchantId"));
        if (null == merchantDTO || null == merchantDTO.getId()){
            throw new BizException(I18nUtils.get("merchant.not.found",getLang(request)));
        }
        //FAVOURITE_IS_ADD
        Integer isAdd = data.getInteger("isAdd");
        Long merchantId = data.getLong("merchantId");
        Long userId = getUserId(request);
        //新增我的收藏数据
        if (isAdd.equals(DataEnum.FAVOURITE_IS_ADD.getCode())){
            data.put("userId",userId);
            //查询是否已经收藏
            if (this.count(data) == 0) {
                this.saveFavoriteMerchant(FavoriteMerchantDTO.builder().userId(userId).merchantId(merchantId).build(), request);
            }else {
                throw new BizException(I18nUtils.get("merchant.already.favorite",getLang(request)));
            }
        }else {
            //删除我的收藏数据
            favoriteMerchantDAO.hardDelete(merchantId, userId);
        }

        //add by zhangzeyuan
        //商户收藏变更 需要通知app更新首页自定义分类模块数据
        try {
            appCustomCategoryDisplayStateService.updateAppCategoriesWhereFavoriteChange(data.getLong("merchantId"));
        }catch (Exception e){
            log.error("收藏 更新app首页自定义分类redis时间戳出错", e.getMessage());
        }


        try {
            Map map = appCustomCategoryDisplayStateService.getDistanceMerchant(data.getLong("merchantId"));
            log.info("距离, map:{}", map);
            Map map3 = new HashMap();
            map3.put("category_type", map.get("categories"));
            map3.put("state_name", map.get("en_name"));
            AppCustomCategoryDisplayStateDTO dto = appCustomCategoryDisplayStateService.findOneAppCustomCategoryDisplayState(map3);
            log.info("距离, dto:{}", dto);
            Integer distance = 2;
            if (distance.equals(dto.getMerchantShowType())) {
                log.info("距离redis, dto:{}", distance.equals(dto.getMerchantShowType()));
                String customCategoriesUpdateTimestampRedisKey = Constant.getCustomCategoriesUpdateTimestampRedisKey(dto.getDisplayOrder(), dto.getStateName());
                redisUtils.set(customCategoriesUpdateTimestampRedisKey, System.currentTimeMillis());
            }

        }catch (Exception e){
            log.error("收藏距离出错", e.getMessage());
        }




    }

}
