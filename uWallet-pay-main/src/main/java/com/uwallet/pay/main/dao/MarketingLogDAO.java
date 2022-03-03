package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.MarketingLogDTO;
import com.uwallet.pay.main.model.entity.MarketingLog;
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
 * @date: Created in 2021-04-26 16:00:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface MarketingLogDAO extends BaseDAO<MarketingLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MarketingLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MarketingLogDTO
     * @param params
     * @return
     */
    MarketingLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询一条最大时间撮已读记录
     * @param params
     * @return
     */
    MarketingLogDTO findMaxTime(Map<String, Object> params);
}
