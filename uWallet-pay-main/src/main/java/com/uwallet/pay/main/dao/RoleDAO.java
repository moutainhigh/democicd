package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.RoleAndActionDTO;
import com.uwallet.pay.main.model.dto.RoleDTO;
import com.uwallet.pay.main.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色表

 * </p>
 *
 * @package:  com.loancloud.rloan.main.mapper
 * @description: 角色表

 * @author: Strong
 * @date: Created in 2019-09-16 17:34:46
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface RoleDAO extends BaseDAO<Role> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RoleDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RoleDTO
     * @param params
     * @return
     */
    RoleDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RoleDTO(关联查询)
     * @param params
     * @return
     */
    List<RoleAndActionDTO> findRoleDTOById(Map<String, Object> params);

    /**
     * 根据id查询 RiskRoleDTO
     * @param params
     * @return
     */
    List<RoleDTO> selectByIdDTO(Map<String, Object> params);

    /**
     * 根据id查询 RiskRoleDTO
     * @param params
     * @return
     */
    List<RoleDTO> selectOneByIdDTO(Map<String, Object> params);

}
