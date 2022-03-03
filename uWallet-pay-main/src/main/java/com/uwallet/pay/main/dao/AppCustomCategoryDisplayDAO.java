package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayDTO;
import com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * APP首页自定义分类展示信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: APP首页自定义分类展示信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:13
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface AppCustomCategoryDisplayDAO extends BaseDAO<AppCustomCategoryDisplay> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppCustomCategoryDisplayDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AppCustomCategoryDisplayDTO
     * @param params
     * @return
     */
    AppCustomCategoryDisplayDTO selectOneDTO(Map<String, Object> params);


    /**
     * 根据州名查询自定义分类信息
     * @author zhangzeyuan
     * @date 2021/4/13 16:19
     * @param order
     * @param stateName
     * @return com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayDTO
     */
    AppCustomCategoryDisplayDTO getAppHomePageDataByOrderAndState(@Param("order") Integer order, @Param("stateName") String stateName);


    /**
     * 获取当前记录下一条记录
     * @author zhangzeyuan
     * @date 2021/4/12 17:26
     * @param order
     * @return com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO
     */
    AppCustomCategoryDisplayDTO getNextOrderCategoryInfo(Integer order);


    /**
     * 获取当前记录上一条记录
     * @author zhangzeyuan
     * @date 2021/4/12 17:26
     * @param order
     * @return com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO
     */
    AppCustomCategoryDisplayDTO getLastOrderCategoryInfo(Integer order);
}
