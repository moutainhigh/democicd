package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.RechargeRouteDTO;
import com.uwallet.pay.main.model.entity.RechargeRoute;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 充值转账路由表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 充值转账路由表
 * @author: zhoutt
 * @date: Created in 2019-12-17 10:36:56
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface RechargeRouteDAO extends BaseDAO<RechargeRoute> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RechargeRouteDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RechargeRouteDTO
     * @param params
     * @return
     */
    RechargeRouteDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据Id更新实体
     *
     * @param params 数据结构 Map<key, Map<key, value>>
     *               key:datas里放需要更新的键值对；
     *               conditions里放where条件筛选条件键值对
     * @return 影响的条数
     */
    int updateRecharge(Map<String, Object> params);

}
