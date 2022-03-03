package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO;
import com.uwallet.pay.main.model.entity.AppCustomCategoryDisplayState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.crypto.MacSpi;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * APP首页自定义分类 每个州展示商户、图片信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: APP首页自定义分类 每个州展示商户、图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:12
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface AppCustomCategoryDisplayStateDAO extends BaseDAO<AppCustomCategoryDisplayState> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<AppCustomCategoryDisplayStateDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 AppCustomCategoryDisplayStateDTO
     * @param params
     * @return
     */
    AppCustomCategoryDisplayStateDTO selectOneDTO(Map<String, Object> params);



    /**
     * 根据商户ID查询包含该商户的分类信息
     * @author zhangzeyuan
     * @date 2021/4/27 19:49
     * @param merchantId
     * @return java.util.List<com.uwallet.pay.main.model.dto.AppCustomCategoryDisplayStateDTO>
     */
    List<AppCustomCategoryDisplayStateDTO> listByMerchantId(Long merchantId);



    /**
     * 批量更新州的排序信息
     * @author zhangzeyuan
     * @date 2021/5/5 22:43
     * @param record
     */
    void updateOrderByMoveUpOrDown(AppCustomCategoryDisplayState record);

    /**
     * 批量更新商户信息
     * @author zhangzeyuan
     * @date 2021/5/6 16:08
     * @param record
     */
    void updateMerchantIdsBySameStateChange(AppCustomCategoryDisplayState record);


    /**
     * 根据排序更新分类信息
     * @author zhangzeyuan
     * @date 2021/5/5 23:40
     * @param record
     */
    void updateCategoryByOrder(AppCustomCategoryDisplayState record);


    /**
     *  更新分类商户信息
     * @author zhangzeyuan
     * @date 2021/5/20 15:53
     * @param record
     */
    void updateCategoryMerchantInfo(AppCustomCategoryDisplayState record);

    /**
     *  更新自定义或者距离
     * @author caisj
     * @date 2021/9/27
     */
    void updatDefinition(@Param("id")Long id, @Param("merchantDisplayType")Integer merchantDisplayType);

    Map getDistanceMerchant(@Param("id")Long id);
}
