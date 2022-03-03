package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.MailLogDTO;
import com.uwallet.pay.main.model.entity.MailLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 邮件发送记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 邮件发送记录表
 * @author: zhoutt
 * @date: Created in 2020-01-07 15:46:48
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface MailLogDAO extends BaseDAO<MailLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MailLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MailLogDTO
     * @param params
     * @return
     */
    MailLogDTO selectOneDTO(Map<String, Object> params);

}
