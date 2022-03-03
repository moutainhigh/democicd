package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO;
import com.uwallet.pay.main.model.entity.AppExclusiveBanner;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * APP首页banner、市场推广图片配置表
 * </p>
 *
 * @package: com.uwallet.pay.main.generator.mapper
 * @description: APP首页banner、市场推广图片配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-04-08 13:35:29
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface AppExclusiveBannerDAO extends BaseDAO<AppExclusiveBanner> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     *
     * @param params
     * @return
     */
    List<AppExclusiveBannerDTO> selectDTO(Map<String, Object> params);

    /**
     * 分页查询 关联查询操作人
     * @author zhangzeyuan
     * @date 2021/5/8 18:00
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO>
     */
    List<AppExclusiveBannerDTO> selectDTOByPage(Map<String, Object> params);

    /**
     * 根据id查询一条 AppExclusiveBannerDTO
     *
     * @param params
     * @return
     */
    AppExclusiveBannerDTO selectOneDTO(Map<String, Object> params);


    /**
     *  获取APP首页banner数据
     * @author zhangzeyuan
     * @date 2021/4/9 9:00
     * @param type
     * @return java.util.List<com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO>
     */
    List<AppExclusiveBannerDTO> listAppHomePageTopBanner(Integer type);



    /**
     * 根据状态获取最大的顺序
     * @author zhangzeyuan
     * @date 2021/4/12 14:57
     * @param state
     * @param displayType
     * @return java.lang.Integer
     */
    Integer getMaxOrderByState(Integer state, Integer displayType);


    /**
     * 获取当前记录下一条记录
     * @author zhangzeyuan
     * @date 2021/4/12 17:26
     * @param order
     * @param displayType
     * @return com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO
     */
    AppExclusiveBannerDTO getNextOrderBannerInfo(Integer order, Integer displayType);


    /**
     * 获取当前记录上一条记录
     * @author zhangzeyuan
     * @date 2021/4/12 17:26
     * @param order
     * @param displayType
     * @return com.uwallet.pay.main.model.dto.AppExclusiveBannerDTO
     */
    AppExclusiveBannerDTO getLastOrderBannerInfo(Integer order, Integer displayType);


    /**
     * 当状态变更时 更新其他数据的排序
     * @author zhangzeyuan
     * @date 2021/4/12 15:34
     * @param order
     * @param state
     * @param displayType
     * @return java.lang.Integer
     */
    Integer updateOthersOrder(Integer order, Integer state ,Integer displayType, Long modifiedBy, Long modifiedDate);


    /**
     * 获取除了这个ID的999999限制次数的数量
     * @author zhangzeyuan
     * @date 2021/5/15 17:53
     * @param displayType
     * @param id
     * @param limit
     * @return int
     */
    int  getLimitsCountByUpdate(Integer displayType, Long id, Integer limit);


}
