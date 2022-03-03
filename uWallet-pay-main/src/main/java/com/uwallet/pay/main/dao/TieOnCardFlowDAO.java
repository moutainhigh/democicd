package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.CountryIsoDTO;
import com.uwallet.pay.main.model.dto.TieOnCardFlowDTO;
import com.uwallet.pay.main.model.entity.TieOnCardFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 绑卡交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 绑卡交易流水表
 * @author: baixinyue
 * @date: Created in 2020-01-06 11:37:40
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface TieOnCardFlowDAO extends BaseDAO<TieOnCardFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<TieOnCardFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 TieOnCardFlowDTO
     * @param params
     * @return
     */
    TieOnCardFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询国家
     * @param country
     * @return
     */
    CountryIsoDTO selectCountryIso(String country);
}
