package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.ClearBatchDTO;
import com.uwallet.pay.main.model.dto.ClearDetailDTO;
import com.uwallet.pay.main.model.entity.ClearBatch;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 清算表生成
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 清算表生成
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:49:55
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface ClearBatchDAO extends BaseDAO<ClearBatch> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ClearBatchDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 ClearBatchDTO
     * @param params
     * @return
     */
    ClearBatchDTO selectOneDTO(Map<String, Object> params);

}
