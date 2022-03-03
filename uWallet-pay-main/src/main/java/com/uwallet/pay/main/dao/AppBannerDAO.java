package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.AppBannerDTO;
import com.uwallet.pay.main.model.entity.AppBanner;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.*;

import java.util.List;

/**
 * <p>
 * app banner
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: app banner
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:29:08
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface AppBannerDAO extends BaseDAO<AppBanner> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppBannerDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AppBannerDTO
     * @param params
     * @return
     */
    AppBannerDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询最大排序数
     * @return
     */
    Integer selectMaxSort();

    /**
     * 查询上一级或下一级
     * @param id
     * @param lessOrMore
     * @return
     */
    AppBannerDTO shiftUpOrDown(@Param("id") Long id, @Param("upOrDown") Integer upOrDown);

    /**
     * 获取轮播图
     * @return
     */
    List<AppBannerDTO> getBannerImg();

}
