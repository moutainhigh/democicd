package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.PartnerDTO;
import com.uwallet.pay.main.model.entity.Partner;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 合伙人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 合伙人信息表
 * @author: Rainc
 * @date: Created in 2019-12-16 10:55:17
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Mapper
public interface PartnerDAO extends BaseDAO<Partner> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<PartnerDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PartnerDTO
     * @param params
     * @return
     */
    PartnerDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户id商户合伙人
     * @param merchantId
     * @return
     */
    int deletePartnerByMerchantId(@Param("merchantId") Long merchantId);

}
