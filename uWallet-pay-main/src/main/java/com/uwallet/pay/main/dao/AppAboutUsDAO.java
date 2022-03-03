package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.AppAboutUsDTO;
import com.uwallet.pay.main.model.entity.AppAboutUs;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;

import java.util.List;

/**
 * <p>
 * app 关于我们
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: app 关于我们
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:53
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface AppAboutUsDAO extends BaseDAO<AppAboutUs> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppAboutUsDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AppAboutUsDTO
     * @param params
     * @return
     */
    AppAboutUsDTO selectOneDTO(Map<String, Object> params);

}
