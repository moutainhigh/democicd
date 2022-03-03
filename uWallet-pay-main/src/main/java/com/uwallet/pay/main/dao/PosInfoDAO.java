package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.PosInfoDTO;
import com.uwallet.pay.main.model.entity.PosInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * pos基本信息
 * </p>
 *
 * @package: com.fenmi.generator.mapper
 * @description: pos基本信息
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 15:17:59
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface PosInfoDAO extends BaseDAO<PosInfo> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<PosInfoDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 PosInfoDTO
     *
     * @param params
     * @return
     */
    PosInfoDTO selectOneDTO(Map<String, Object> params);

}
