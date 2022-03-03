package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.FavoriteMerchantDTO;
import com.uwallet.pay.main.model.entity.FavoriteMerchant;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户的收藏商户数据
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户的收藏商户数据
 * @author: aaron S
 * @date: Created in 2021-04-07 18:04:58
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaron S
 */
@Mapper
public interface FavoriteMerchantDAO extends BaseDAO<FavoriteMerchant> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<FavoriteMerchantDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 FavoriteMerchantDTO
     * @param params
     * @return
     */
    FavoriteMerchantDTO selectOneDTO(Map<String, Object> params);

    /**
     * 硬删除我的收藏数据
     * @param merchantId
     * @param userId
     */
    Integer hardDelete(@Param("merchantId") Long merchantId,@Param("userId") Long userId);

    /**
     * 获取分页展示我的收藏列表
     * @param params
     * @return
     */
    List<JSONObject> findListWithMerchantInfo(Map<String, Object> params);

    /**
     * 计算数据
     * @param data
     * @return
     */
    int countFavorite(JSONObject data);
}
