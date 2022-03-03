package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ContactPersonDTO;
import com.uwallet.pay.main.model.entity.ContactPerson;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 联系人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 联系人信息表
 * @author: baixinyue
 * @date: Created in 2020-08-06 11:45:37
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface ContactPersonDAO extends BaseDAO<ContactPerson> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ContactPersonDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ContactPersonDTO
     * @param params
     * @return
     */
    ContactPersonDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户id删除受益人信息
     * @param merchantId
     * @return
     */
    int deleteContactPersonByMerchantId(@Param("merchantId") Long merchantId);

}
