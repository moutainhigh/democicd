package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.GatewayDTO;
import com.uwallet.pay.main.model.entity.Gateway;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 支付渠道信息表

 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 支付渠道信息表

 * @author: Strong
 * @date: Created in 2019-12-12 10:16:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface GatewayDAO extends BaseDAO<Gateway> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<GatewayDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 GatewayDTO
     * @param params
     * @return
     */
    GatewayDTO selectOneDTO(Map<String, Object> params);

    List<GatewayDTO> getPayType();
}
