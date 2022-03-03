package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.AccessMerchantDTO;
import com.uwallet.pay.main.model.dto.AccessPlatformInfoDTO;
import com.uwallet.pay.main.model.entity.AccessMerchant;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 接入方商户表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 接入方商户表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface AccessMerchantDAO extends BaseDAO<AccessMerchant> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AccessMerchantDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AccessMerchantDTO
     * @param params
     * @return
     */
    AccessMerchantDTO selectOneDTO(Map<String, Object> params);

    /**
     * 获取三方商户列表
     * @param params
     * @return
     */
    List<AccessPlatformInfoDTO> getAccessMerchantList(Map<String, Object> params);

    /**
     * 查询三方商户列表个数
     * @param params
     * @return
     */
    int getAccessMerchantListCount(Map<String, Object> params);
}
