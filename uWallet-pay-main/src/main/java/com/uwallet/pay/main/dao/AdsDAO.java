package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.AdsDTO;
import com.uwallet.pay.main.model.entity.Ads;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 广告表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 广告表
 * @author: Strong
 * @date: Created in 2020-01-11 09:45:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface AdsDAO extends BaseDAO<Ads> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AdsDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AdsDTO
     * @param params
     * @return
     */
    AdsDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询app一条上架广告,list集合方便app处理结果
     * @param params
     * @return
     */
    List<AdsDTO> appFindOneAds(Map<String, Object> params);

    /**
     * 根据id查询一条 AdsDTO
     * @param params
     * @return
     */
    AdsDTO selectOneByIdDTO(Map<String, Object> params);

}
