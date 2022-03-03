package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.AdminRoleDTO;
import com.uwallet.pay.main.model.entity.AdminRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员 -角色关系表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.mapper
 * @description: 管理员 -角色关系表
 * @author: Strong
 * @date: Created in 2019-09-16 16:25:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface AdminRoleDAO extends BaseDAO<AdminRole> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AdminRoleDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AdminRoleDTO
     * @param params
     * @return
     */
    AdminRoleDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据AdminId查询 RiskAdminRoleDTO
     * @param params
     * @return
     */
    List<AdminRoleDTO> selectByAdminIdDTO(Map<String, Object> params);

    /**
     * 根据AdminId查询 RiskAdminRoleDTO
     * @param params
     * @return
     */
    List<AdminRoleDTO> selectOneByAdminIdDTO(Map<String, Object> params);

    /**
     * 根据AdminId删除关联关系
     * @param params
     * @return
     */
    int deleteByAdminId(Map<String, Object> params);

    /**
     * 查看是否已经具有 资料审查组长 抉择引擎组组长 授信审查组长 放款审查组长 角色
     *
     * @param params
     * @return
     */
    List<AdminRoleDTO> selectRoleDTO(Map<String, Object> params);

}
