package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.CodeUpdateLogDTO;
import com.uwallet.pay.main.model.entity.CodeUpdateLog;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 码操作记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 码操作记录表
 * @author: xucl
 * @date: Created in 2021-03-09 09:55:32
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface CodeUpdateLogDAO extends BaseDAO<CodeUpdateLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<CodeUpdateLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 CodeUpdateLogDTO
     * @param params
     * @return
     */
    CodeUpdateLogDTO selectOneDTO(Map<String, Object> params);

}
