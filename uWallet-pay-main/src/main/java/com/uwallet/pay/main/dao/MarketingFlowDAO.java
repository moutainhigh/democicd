package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.model.dto.MarketingFlowDTO;
import com.uwallet.pay.main.model.entity.MarketingFlow;
import com.uwallet.pay.core.dao.BaseDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

import java.util.List;

/**
 * <p>
 * 账户动账交易流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:30:03
 * @copyright: Copyright (c) 2020
 * @version: V1.0
 * @modified: baixinyue
 */
@Mapper
public interface MarketingFlowDAO extends BaseDAO<MarketingFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<MarketingFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 MarketingFlowDTO
     * @param params
     * @return
     */
    MarketingFlowDTO selectOneDTO(Map<String, Object> params);

    /**
     * 查询失败和可疑
     * @param params
     * @return
     */
    List<MarketingFlowDTO> findAbnormal(Map<String, Object> params);

    /**
     * 获取用户使用payo money金额
     * @author zhangzeyuan
     * @date 2021/9/9 15:37
     * @param userId
     * @return java.math.BigDecimal
     */
    BigDecimal getUseRedAmountByUserId(@Param("userId") Long userId);

    /**
     * 查询回退可疑流水
     * @return
     */
    List<MarketingFlowDTO> findRollBackDoubtHandle();

    /**
     * 查询回退失败流水
     * @return
     */
    List<MarketingFlowDTO> findRollBackFailHandle();

    /**
     * 查询券码使用记录
     * @return
     */
    List<MarketingFlowDTO>  getMarketingCodeUsedLog(Map<String, Object> params);

    /**
     * 查询券码使用数量
     * @return
     */
    int  countUsedLog(Map<String, Object> params);

    /**
     * 查询在哪消费的券的商户名称
     * @return
     */
    String getPaidMerchantName(Map<String, Object> params);


    List<MarketingFlowDTO>  getFlowList(Map<String, Object> params);


    /**
     * 查询用户可用卡券总额
     * @param userId
     *
     * @return
     */
    BigDecimal getRedAmountByUserId(Long userId);

    /**
     * 查询可用卡券
     * @param param
     * @return
     */
    List<JSONObject> getUseAvailablePromotionByUserId(JSONObject param);
}
