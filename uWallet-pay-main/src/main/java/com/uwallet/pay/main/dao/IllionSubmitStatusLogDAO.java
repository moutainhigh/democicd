package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.IllionSubmitStatusLogDTO;
import com.uwallet.pay.main.model.entity.IllionSubmitStatusLog;
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
 * @date: Created in 2021-06-22 13:04:06
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface IllionSubmitStatusLogDAO extends BaseDAO<IllionSubmitStatusLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<IllionSubmitStatusLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 IllionSubmitStatusLogDTO
     * @param params
     * @return
     */
    IllionSubmitStatusLogDTO selectOneDTO(Map<String, Object> params);

}
