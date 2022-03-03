package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.MerchantContractFileRecordDTO;
import com.uwallet.pay.main.model.entity.MerchantContractFileRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 合同记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 合同记录表
 * @author: fenmi
 * @date: Created in 2021-04-29 10:11:38
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: fenmi
 */
@Mapper
public interface MerchantContractFileRecordDAO extends BaseDAO<MerchantContractFileRecord> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MerchantContractFileRecordDTO> selectDTO(Map<String, Object> params);


    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MerchantContractFileRecordDTO> listContractFile(Map<String, Object> params);

    /**
     * 根据id查询一条 MerchantContractFileRecordDTO
     * @param params
     * @return
     */
    MerchantContractFileRecordDTO selectOneDTO(Map<String, Object> params);

}
