package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.TagDTO;
import com.uwallet.pay.main.model.entity.Tag;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * Tag数据
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: Tag数据
 * @author: aaronS
 * @date: Created in 2021-01-07 11:19:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: aaronS
 */
@Mapper
public interface TagDAO extends BaseDAO<Tag> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<TagDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 TagDTO
     * @param params
     * @return
     */
    TagDTO selectOneDTO(Map<String, Object> params);

    List<String> getTop10Tags(@Param("mark") String mark);

    /**
     * 完全匹配tag表中的数据,如果匹配上,则popular 字段+1
     * @param param
     */
    void updateTagPopular(JSONObject param);

    /**
     * 模糊匹配tag表数据
     * @param value
     * @return
     */
    List<String> matchTags(@Param("value")String value);

}
