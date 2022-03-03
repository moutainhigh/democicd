package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.NoticeMassDTO;
import com.uwallet.pay.main.model.entity.NoticeMass;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 群发消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 群发消息表
 * @author: baixinyue
 * @date: Created in 2020-02-21 08:50:22
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface NoticeMassDAO extends BaseDAO<NoticeMass> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<NoticeMassDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 NoticeMassDTO
     * @param params
     * @return
     */
    NoticeMassDTO selectOneDTO(Map<String, Object> params);

}
