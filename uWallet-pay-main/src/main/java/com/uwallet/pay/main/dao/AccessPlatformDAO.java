package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.AccessPlatformDTO;
import com.uwallet.pay.main.model.entity.AccessPlatform;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 接入方平台表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 接入方平台表
 * @author: zhoutt
 * @date: Created in 2020-09-25 08:55:53
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: zhoutt
 */
@Mapper
public interface AccessPlatformDAO extends BaseDAO<AccessPlatform> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AccessPlatformDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AccessPlatformDTO
     * @param params
     * @return
     */
    AccessPlatformDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询所有可用平台
     * @return
     */
    List<AccessPlatform> getAllPlatform();
}
