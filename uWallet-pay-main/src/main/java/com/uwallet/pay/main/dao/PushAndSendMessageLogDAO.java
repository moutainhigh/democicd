package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.PushAndSendMessageLogDTO;
import com.uwallet.pay.main.model.entity.PushAndSendMessageLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 
 * @author: xucl
 * @date: Created in 2021-04-16 15:56:48
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface PushAndSendMessageLogDAO extends BaseDAO<PushAndSendMessageLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<PushAndSendMessageLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PushAndSendMessageLogDTO
     * @param params
     * @return
     */
    PushAndSendMessageLogDTO selectOneDTO(Map<String, Object> params);

}
