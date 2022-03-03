package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.SecondMerchantGatewayInfoDTO;
import com.uwallet.pay.main.model.entity.SecondMerchantGatewayInfo;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 二级商户渠道信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 二级商户渠道信息表
 * @author: baixinyue
 * @date: Created in 2019-12-26 17:02:13
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface SecondMerchantGatewayInfoDAO extends BaseDAO<SecondMerchantGatewayInfo> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<SecondMerchantGatewayInfoDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 SecondMerchantGatewayInfoDTO
     * @param params
     * @return
     */
    SecondMerchantGatewayInfoDTO selectOneDTO(Map<String, Object> params);

}
