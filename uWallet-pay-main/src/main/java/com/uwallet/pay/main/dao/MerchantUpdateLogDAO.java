package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.MerchantUpdateLogDTO;
import com.uwallet.pay.main.model.entity.MerchantUpdateLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 商户修改记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 商户修改记录表
 * @author: xucl
 * @date: Created in 2021-03-15 08:39:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface MerchantUpdateLogDAO extends BaseDAO<MerchantUpdateLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MerchantUpdateLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MerchantUpdateLogDTO
     * @param params
     * @return
     */
    MerchantUpdateLogDTO selectOneDTO(Map<String, Object> params);

}
