package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.AdminAndRoleDTO;
import com.uwallet.pay.main.model.dto.AdminDTO;
import com.uwallet.pay.main.model.dto.AdminOnlyDTO;
import com.uwallet.pay.main.model.dto.AdminWithBorrowVerifyCountDTO;
import com.uwallet.pay.main.model.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 管理员账户表
 * </p>
 *
 * @package: com.loancloud.rloan.main.admin.mapper
 * @description: 管理员账户表
 * @author: liming
 * @date: Created in 2019-09-09 15:24:15
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: liming
 */
@Mapper
public interface AdminDAO extends BaseDAO<Admin> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<AdminDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AdminDTO
     *
     * @param params
     * @return
     */
    AdminDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据条件查询多条 AdminWithBorrowVerifyCountDTO，包含分页排序信息
     *
     * @param params
     * @return
     */
    List<AdminWithBorrowVerifyCountDTO> selectAdminWithBorrowVerifyCountDTO(Map<String, Object> params);

    /**
     * 审批小组-根据组长id统计同一小组成员
     *
     * @param params
     * @return
     */
    int selectAdminAndRoleCount(Map<String, Object> params);

    /**
     * 根据条件查询总数
     *
     * @param params
     * @return
     */
    int selectAdminWithBorrowVerifyCount(Map<String, Object> params);

    /**
     * 根据条件查询多条 AdminDTO，不包含分页排序
     *
     * @param params
     * @return
     */
    List<AdminDTO> selectByParentId(Map<String, Object> params);

    /**
     * 联合三表查询
     *
     * @return
     */
    List<AdminAndRoleDTO> selectAdminAndRoleAndBorrow();

    /**
     * 根据id联合三表查询
     *
     * @param params
     * @return
     */
    List<AdminAndRoleDTO> selectAdminAndRoleAndBorrowById(Map<String, Object> params);

    /**
     * 组长获取小组成员【审批工作台-->待审核进件-->分配进件】
     *
     * @param params
     * @return java.util.List<com.loancloud.rloan.main.model.dto.AdminDTO>
     * @Author Laity
     */
    List<AdminDTO> getTeamMemberInfo(Map<String, Object> params);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    AdminOnlyDTO findOneAndRoleName(Map<String, Object> params);

    /**
     * 根据Id更新last_login_time上次登陆时间字段
     * last_login_ip 上次登陆IP字段
     *
     * @param params 数据结构 Map<key, Map<key, value>>
     *               key:datas里放需要更新的键值对；
     *               conditions里放where条件筛选条件键值对
     * @return 影响的条数
     */
    int updateLastLoginDate(Long params);

    /**
     * 根据 username查询一条 RiskAdminDTO
     *
     * @param params
     * @return
     */
    AdminDTO selectOneByUsernameDTO(Map<String, Object> params);

    /**
     * 查询符合条件的第一条数据
     *
     * @param params 筛选条件的键值对
     * @return 符合条件的实体
     */
    AdminDTO selectAdminPartOfData(Map<String, Object> params);

    /**
     * 根据角色ID 得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param param
     * @return
     */
    List<AdminOnlyDTO> findListAndRoleNameByRoleId(Map<String, Object> param);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    AdminOnlyDTO findAndRoleName(Map<String, Object> params);

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<AdminOnlyDTO> findListAndRoleName(Map<String, Object> params);

    /**
     * 统计查询条数
     *
     * @param params 筛选条件的键值对
     * @return 统计的条数
     */
    int countList(Map<String, Object> params);

    /**
     * 模糊统计查询
     *
     * @param params
     * @return
     */
    int likeCount(Map<String, Object> params);

    /**
     * 查询符合条件的数据
     *
     * @param params 筛选条件的键值对
     * @return 符合条件的实体
     */
    List<AdminDTO> selectRoleName(Map<String, Object> params);

    /**
     * 根据角色id查询管理员
     *
     * @param params 筛选条件的键值对
     * @return 符合条件的实体
     */
    List<AdminDTO> selectAdminByRoleId(Map<String, Object> params);

    /**
     * 统计同一小组的在岗人数
     *
     * @param params
     * @return
     */
    int selectisJobIngCount(Map<String, Object> params);

    /**
     * 查询result状态为1,2,3的值
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> returnResultOneCount(Map<String, Object> params);

    /**
     * 查询result状态为0的值
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> returnResultZeroCount(Map<String, Object> params);

    /**
     * 查询单条result状态为1,2,3的值
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> returnOnceResultOneCount(Map<String, Object> params);

    /**
     * 查询单条result状态为0的值
     *
     * @param params
     * @return
     */
    List<Map<String, Object>> returnOnceResultZeroCount(Map<String, Object> params);

    /**
     * 通过组长角色id查询在职组长
     *
     * @param roleIds
     * @return
     */
    List<AdminDTO> findTheAdministratorByRole(List roleIds);
}
