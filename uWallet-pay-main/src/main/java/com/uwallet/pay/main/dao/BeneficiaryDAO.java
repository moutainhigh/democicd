package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.BeneficiaryDTO;
import com.uwallet.pay.main.model.entity.Beneficiary;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 受益人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 受益人信息表
 * @author: Rainc
 * @date: Created in 2019-12-11 16:51:43
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Mapper
public interface BeneficiaryDAO extends BaseDAO<Beneficiary> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<BeneficiaryDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 BeneficiaryDTO
     * @param params
     * @return
     */
    BeneficiaryDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户id删除受益人信息
     * @param merchantId
     * @return
     */
    int deleteBeneficiaryByMerchantId(@Param("merchantId") Long merchantId);

}
