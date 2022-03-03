package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.ActionDTO;
import com.uwallet.pay.main.model.dto.ActionOnlyDTO;
import com.uwallet.pay.main.model.entity.Action;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.mapper
 * @description: 权限表
 * @author: Strong
 * @date: Created in 2019-09-16 17:55:12
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Strong
 */
@Mapper
public interface ActionDAO extends BaseDAO<Action> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<ActionDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据管理员账号 只查询用户按钮权限
     * @param username
     * @return
     */
    List<String> findAction(String username);

    /**
     * 根据管理员账号 只查询用户菜单权限
     * @param username
     * @return
     */
    List<String> findMenuAction(String username);

    /**
     * 根据id查询一条 ActionDTO
     * @param params
     * @return
     */
    ActionDTO selectOneDTO(Map<String, Object> params);

    /**
     * 根据 ActionId查询 flag,url,urlType
     *
     * @param params 筛选条件的键值对
     * @return 符合条件的实体
     */
    List<ActionDTO> actionByActionId(String params);

    /**
     * 权限树
     * @return
     */
    List<ActionOnlyDTO> actionTree();

}
