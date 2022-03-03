package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.RoleActionDTO;
import com.uwallet.pay.main.model.entity.RoleAction;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 角色-权限关系表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.mapper
 * @description: 角色-权限关系表
 * @author: Strong
 * @date: Created in 2019-09-16 17:51:57
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface RoleActionDAO extends BaseDAO<RoleAction> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<RoleActionDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 RoleActionDTO
     * @param params
     * @return
     */
    RoleActionDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据RoleId查询 RiskRoleActionDTO
     * @param params
     * @return
     */
    List<RoleActionDTO> selectOneDTOByRoleId(Map<String, Object> params);

    /**
     * 物理删除
     *
     * @param params 筛选条件的键值对
     * @return 影响的条数
     */
    int deleteByRoleId(Map<String, Object> params);

}
