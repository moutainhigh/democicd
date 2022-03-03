package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.RouteDTO;
import com.uwallet.pay.main.model.entity.Route;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 路由表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 路由表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:57:32
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Mapper
public interface RouteDAO extends BaseDAO<Route> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RouteDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RouteDTO> findList(Map<String, Object> params);

    /**
     * 根据id查询一条 RouteDTO
     * @param params
     * @return
     */
    RouteDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户ID删除Route
     * @param merchantId
     * @return
     */
    int deleteRouteByMerchantId(@Param("merchantId") Long merchantId);

    RouteDTO findMaxMerRate(Long merchantId);
}
