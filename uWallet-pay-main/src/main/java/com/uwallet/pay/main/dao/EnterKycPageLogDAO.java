package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.EnterKycPageLogDTO;
import com.uwallet.pay.main.model.entity.EnterKycPageLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 
 * @author: zhangzeyuan
 * @date: Created in 2021-08-13 15:47:46
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface EnterKycPageLogDAO extends BaseDAO<EnterKycPageLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<EnterKycPageLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 EnterKycPageLogDTO
     * @param params
     * @return
     */
    EnterKycPageLogDTO selectOneDTO(Map<String, Object> params);

    /**
     *  新增或更新
     * @author zhangzeyuan
     * @date 2021/8/13 16:05
     * @param params
     * @return int
     */
    int upsertLog(Map<String, Object> params);

}
