package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ContentDTO;
import com.uwallet.pay.main.model.entity.Content;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 广告表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 广告表
 * @author: Strong
 * @date: Created in 2020-01-14 11:10:06
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface ContentDAO extends BaseDAO<Content> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ContentDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ContentDTO
     * @param params
     * @return
     */
    ContentDTO selectOneDTO(Map<String, Object> params);

}
