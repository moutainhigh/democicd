package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.DirectorDTO;
import com.uwallet.pay.main.model.entity.Director;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 董事信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 董事信息表
 * @author: Rainc
 * @date: Created in 2020-01-03 10:23:38
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Rainc
 */
@Mapper
public interface DirectorDAO extends BaseDAO<Director> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<DirectorDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 DirectorDTO
     * @param params
     * @return
     */
    DirectorDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据商户ID删除Director
     * @param merchantId
     * @return
     */
    int deleteDirectorByMerchantId(@Param("merchantId") Long merchantId);

}
