package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.TipClearFileRecordDTO;
import com.uwallet.pay.main.model.entity.TipClearFileRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 小费清算文件记录
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 小费清算文件记录
 * @author: zhangzeyuan
 * @date: Created in 2021-08-11 17:21:14
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface TipClearFileRecordDAO extends BaseDAO<TipClearFileRecord> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<TipClearFileRecordDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 TipClearFileRecordDTO
     * @param params
     * @return
     */
    TipClearFileRecordDTO selectOneDTO(Map<String, Object> params);

}
