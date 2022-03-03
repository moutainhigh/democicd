package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.UserEnterAppPageLogDTO;
import com.uwallet.pay.main.model.entity.UserEnterAppPageLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户APP页面流程记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 用户APP页面流程记录表
 * @author: zhangzeyuan
 * @date: Created in 2021-09-01 16:35:17
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface UserEnterAppPageLogDAO extends BaseDAO<UserEnterAppPageLog> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<UserEnterAppPageLogDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 UserEnterAppPageLogDTO
     * @param params
     * @return
     */
    UserEnterAppPageLogDTO selectOneDTO(Map<String, Object> params);

}
