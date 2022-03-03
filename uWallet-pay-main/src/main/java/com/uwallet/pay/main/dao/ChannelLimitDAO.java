package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ChannelLimitDTO;
import com.uwallet.pay.main.model.entity.ChannelLimit;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 渠道日交易累计金额记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 渠道日交易累计金额记录表
 * @author: baixinyue
 * @date: Created in 2019-12-21 10:06:05
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface ChannelLimitDAO extends BaseDAO<ChannelLimit> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ChannelLimitDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ChannelLimitDTO
     * @param params
     * @return
     */
    ChannelLimitDTO selectOneDTO(Map<String, Object> params);

    /**
     *  更新日累计
     * @param channelLimit
     * @return
     */
    int updateAmount(ChannelLimit channelLimit);

    /**
     * 日累计限额回滚
     * @param map
     */
    void channelLimitRollback(Map<String, Object> map);
}
