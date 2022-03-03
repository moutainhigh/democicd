package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ShareholderDTO;
import com.uwallet.pay.main.model.entity.Shareholder;
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
 * @author: baixinyue
 * @date: Created in 2020-04-20 17:16:45
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface ShareholderDAO extends BaseDAO<Shareholder> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ShareholderDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ShareholderDTO
     * @param params
     * @return
     */
    ShareholderDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户id删除董事信息
     * @param merchantId
     * @return
     */
    int deleteShareholderByMerchantId(@Param("merchantId") Long merchantId);

}
