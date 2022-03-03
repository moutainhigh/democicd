package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.IllionSubmitLogDTO;
import com.uwallet.pay.main.model.entity.IllionSubmitLog;
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
 * @date: Created in 2021-04-13 11:11:34
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface IllionSubmitLogDAO extends BaseDAO<IllionSubmitLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<IllionSubmitLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 IllionSubmitLogDTO
     * @param params
     * @return
     */
    IllionSubmitLogDTO selectOneDTO(Map<String, Object> params);

    /**
     * @param params
     * 修改一条记录
     * @return
     */
    int updateNew(Map<String, Object> params);

    /**
     * 查询不符合条件
     * @param params
     * @return
     */
    List<IllionSubmitLogDTO> selectNotInDTO(Map<String, Object> params);
}
