package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.StaticDataDTO;
import com.uwallet.pay.main.model.entity.StaticData;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 数据字典
 * @author: Strong
 * @date: Created in 2019-12-13 15:35:58
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface StaticDataDAO extends BaseDAO<StaticData> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<StaticDataDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 StaticDataDTO
     * @param params
     * @return
     */
    StaticDataDTO selectOneDTO(Map<String, Object> params);


    /**
     * 城市模糊搜索
     * @author zhangzeyuan
     * @date 2021/11/22 10:47
     * @param keyword
     * @return java.util.List<com.uwallet.pay.main.model.dto.StaticDataDTO>
     */
    List<StaticDataDTO> getCityListByKeywords(@Param("keyword") String keyword);


    /**
     * 获取支持的卡列表品牌
     * @author zhangzeyuan
     * @date 2022/1/28 10:44
     * @param code
     * @return java.util.List<java.lang.String>
     */
    List<String> getsupportedCardList(@Param("code") String code);

    /**
     * 根据条件查询得到第一条 staticData
     *
     * @param  code 查询条件
     * @return 符合条件的一个 staticData
     */
    String selectCountry(String code);
}
