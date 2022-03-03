package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.PaymentChannelDTO;
import com.uwallet.pay.main.model.entity.PaymentChannel;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 三方支付通道表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.mapper
 * @description: 三方支付通道表
 * @author: baixinyue
 * @date: Created in 2020-02-13 10:31:26
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface PaymentChannelDAO extends BaseDAO<PaymentChannel> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<PaymentChannelDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PaymentChannelDTO
     * @param params
     * @return
     */
    PaymentChannelDTO selectOneDTO(Map<String, Object> params);

}
