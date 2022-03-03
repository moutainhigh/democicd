package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.MailTemplateDTO;
import com.uwallet.pay.main.model.entity.MailTemplate;
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
 * @date: Created in 2020-01-04 13:56:55
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface MailTemplateDAO extends BaseDAO<MailTemplate> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MailTemplateDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MailTemplateDTO
     * @param params
     * @return
     */
    MailTemplateDTO selectOneDTO(Map<String, Object> params);

    /**
     *
     * @param node
     * @return
     */
    MailTemplateDTO findMailTemplateBySendNode(String node);
}
