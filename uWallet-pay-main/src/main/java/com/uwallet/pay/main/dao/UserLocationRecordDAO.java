package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.UserLocationRecordDTO;
import com.uwallet.pay.main.model.entity.UserLocationRecord;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 用户地理位置信息记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户地理位置信息记录表
 * @author: xucl
 * @date: Created in 2021-05-15 10:22:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface UserLocationRecordDAO extends BaseDAO<UserLocationRecord> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserLocationRecordDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserLocationRecordDTO
     * @param params
     * @return
     */
    UserLocationRecordDTO selectOneDTO(Map<String, Object> params);

}
