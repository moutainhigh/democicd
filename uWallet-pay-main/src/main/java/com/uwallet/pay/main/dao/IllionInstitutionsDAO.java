package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.IllionInstitutionsDTO;
import com.uwallet.pay.main.model.entity.IllionInstitutions;
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
 * @date: Created in 2021-03-19 09:37:47
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: xucl
 */
@Mapper
public interface IllionInstitutionsDAO extends BaseDAO<IllionInstitutions> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<IllionInstitutionsDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 IllionInstitutionsDTO
     * @param params
     * @return
     */
    IllionInstitutionsDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询所有机构
     * @param params
     * @return
     */
    List<IllionInstitutions> selectDTOs(Map<String, Object> params);
}
