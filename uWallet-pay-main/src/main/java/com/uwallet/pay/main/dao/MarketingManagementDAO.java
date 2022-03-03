package com.uwallet.pay.main.dao;

import com.uwallet.pay.core.exception.BizException;
import com.uwallet.pay.main.model.dto.MarketingManagementDTO;
import com.uwallet.pay.main.model.dto.UserDetailUsedPayoMoneyDTO;
import com.uwallet.pay.main.model.dto.UserPromotionDTO;
import com.uwallet.pay.main.model.entity.MarketingManagement;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 商户营销码
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 商户营销码
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:29:48
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface MarketingManagementDAO extends BaseDAO<MarketingManagement> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MarketingManagementDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MarketingManagementDTO
     * @param params
     * @return
     */
    MarketingManagementDTO selectOneDTO(Map<String, Object> params);

    /**
     * 累加已用次数
     * @return
     */
    int addUsedNumber(@Param("id")Long id);

    /**
     * 回滚已用次数
     * @param id
     * @return
     */
    int addUsedNumberRollback(@Param("id")Long id);
    /**
     * 列表页分页计数
     * @param params
     * @return
     */
    int marketingCodeCount(Map<String, Object> params);

    /**
     * 列表页分页
     * @param params
     * @return
     */
    List<MarketingManagementDTO> marketingCodeList(Map<String, Object> params);


    /**
     *  根据CODE搜索营销码
     * @author zhangzeyuan
     * @date 2021/10/27 17:04
     * @param params
     * @return com.uwallet.pay.main.model.dto.MarketingManagementDTO
     */
    MarketingManagementDTO getPromotionData(Map<String, Object> params);


    /**
     * 获取所有营销信息
     * @author zhangzeyuan
     * @date 2021/10/28 10:49
     * @param params
     * @return java.util.List<com.uwallet.pay.main.model.dto.MarketingManagementDTO>
     */
    List<MarketingManagementDTO> findAllMarketing(Map<String, Object> params);


    /**
     * 增加已领取数量
     * @author zhangzeyuan
     * @date 2021/10/29 15:40
     * @param id
     * @return int
     */
    int addReceivedNumber(Map<String, Object> params);

    /**
     * 领取注册券之后 更新 金额 数量
     * @author zhangzeyuan
     * @date 2021/10/29 17:14
     * @param id
     * @return int
     */
    int updateInviteMarketingByReceived(@Param("id")Long id, @Param("money") BigDecimal money);


    /**
     * 开始营销活动
     * @param now
     * @param time
     */
    int startMarketing(@Param("time") Long time, @Param("now") Long now);

    /**
     * 结束营销活动
     * @param time
     * @param now
     * @return
     */
    int endMarketing(@Param("time") Long time, @Param("now") Long now);


    /**
     * 累加已用次数
     * @author zhangzeyuan
     * @date 2021/11/15 16:18
     * @param params
     * @return int
     */
    int addUsedNumberNew(Map<String, Object> params);


    /**
     * 更新邀请码为不可用
     * @author zhangzeyuan
     * @date 2021/11/19 14:17
     * @param newId
     * @param id
     * @return int
     */
    int updateInvitationCodeNotAvailable(@Param("newId")Long newId , @Param("id")Long id);


    /**
     * 根据code查询数量
     * @author zhangzeyuan
     * @date 2021/11/19 16:47
     * @param code
     * @return int
     */
    int countByCode(String code);


    /**
     * 回滚领取数量
     * @author zhangzeyuan
     * @date 2021/11/23 17:33
     * @param params
     * @return int
     */
    int rollBackReceivedNumber(Map<String, Object> params);


    /**
     * 查询用户卡券数量
     * @param params
     * @return
     */
    int userPromotionCount(Map<String, Object> params);

    /**
     * 查询用户卡券列表
     * @param params
     * @return
     */
    List<UserPromotionDTO> userPromotionList(Map<String, Object> params);
}
