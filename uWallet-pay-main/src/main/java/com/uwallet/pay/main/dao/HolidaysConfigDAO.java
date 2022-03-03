package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.HolidaysConfigDTO;
import com.uwallet.pay.main.model.entity.HolidaysConfig;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 节假日表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 节假日表
 * @author: baixinyue
 * @date: Created in 2020-09-08 11:24:52
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface HolidaysConfigDAO extends BaseDAO<HolidaysConfig> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<HolidaysConfigDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 HolidaysConfigDTO
     * @param params
     * @return
     */
    HolidaysConfigDTO selectOneDTO(Map<String, Object> params);

}
