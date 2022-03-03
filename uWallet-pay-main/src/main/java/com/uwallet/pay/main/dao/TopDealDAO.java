package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.TopDealDTO;
import com.uwallet.pay.main.model.entity.TopDeal;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * top deal
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: top deal
 * @author: zhoutt
 * @date: Created in 2020-03-12 14:34:50
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface TopDealDAO extends BaseDAO<TopDeal> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<TopDealDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 TopDealDTO
     * @param params
     * @return
     */
    TopDealDTO selectOneDTO(Map<String, Object> params);

    /**
     * deal上下移
     * @param id
     * @param upOrDown
     * @return
     */
    TopDealDTO shiftUpOrDown(@Param("id") Long id, @Param("upOrDown") Integer upOrDown);

    /**
     * 查询最大排序数
     * @return
     */
    Integer selectMaxSort();

    /**
     * 获取轮播图
     * @return
     */
    List<TopDealDTO> getTopDealImg();
}
