package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.PushTemplateDTO;
import com.uwallet.pay.main.model.entity.PushTemplate;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 模板
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 模板
 * @author: zhoutt
 * @date: Created in 2020-01-04 13:52:28
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface PushTemplateDAO extends BaseDAO<PushTemplate> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<PushTemplateDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PushTemplateDTO
     * @param params
     * @return
     */
    PushTemplateDTO selectOneDTO(Map<String, Object> params);

}
