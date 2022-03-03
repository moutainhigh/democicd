package com.uwallet.pay.main.dao;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.core.dao.BaseDAO;
import com.uwallet.pay.main.model.dto.QrPayFlowDTO;
import com.uwallet.pay.main.model.dto.TipFlowClearDTO;
import com.uwallet.pay.main.model.dto.TipFlowDTO;
import com.uwallet.pay.main.model.dto.TipMerchantsDTO;
import com.uwallet.pay.main.model.entity.ClearBatch;
import com.uwallet.pay.main.model.entity.ClearDetail;
import com.uwallet.pay.main.model.entity.ClearFlowDetail;
import com.uwallet.pay.main.model.entity.TipFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 小费流水表
 * </p>
 *
 * @package:  com.uwallet.pay.main.mapper
 * @description: 小费流水表
 * @author: zhangzeyuan
 * @date: Created in 2021-08-10 16:01:03
 * @copyright: Copyright (c) 2021
 * @version: V1.0
 * @modified: zhangzeyuan
 */
@Mapper
public interface TipFlowDAO extends BaseDAO<TipFlow> {

    /**
     * 根据查询条件得到指定字段集合的数据列表，包含分页和排序信息
     * @param params
     * @return
     */
    List<TipFlowDTO> selectDTO(Map<String, Object> params);

    /**
     * 根据id查询一条 TipFlowDTO
     * @param params
     * @return
     */
    TipFlowDTO selectOneDTO(Map<String, Object> params);


    /**
     *
     * @author zhangzeyuan
     * @date 2021/8/10 17:24
     * @param params
     * @return int
     */
    int updateStateByFlowId(Map<String, Object> params);

    /**
     * 查询包含小费商户列表
     * @param params
     * @return
     */
    List<TipMerchantsDTO> countTipMerchant(Map<String, Object> params);

    /**查询包含小费 商户数据
     * @param params
     * @return
     */
    List<JSONObject> findTipMerchantData(Map<String, Object> params);

    /**
     * 查询某个状态下的订单数量
     * @param param
     * @return
     */
    int countOrderByMerchantId(JSONObject param);

    /**
     * 查询某个状态订单数据
     * @param params
     * @return
     */
    List<JSONObject> selectDTOList(JSONObject params);

    /**
     *  转换结算状态
     * @param data
     * @return
     */
    int updateSettlementState(JSONObject data);

    /**
     * 查询导出excl数据
     * @param param
     * @return
     */
    List<JSONObject> exportUserOrder(TipFlowClearDTO param);

    /**
     * 查询导出excl数据
     * @param param
     * @return
     */
    List<JSONObject> findTotal(TipFlowClearDTO param);

    /**
     * 根据批次号更新结算信息
     * @author zhangzeyuan
     * @date 2021/8/12 11:15
     * @param params
     * @return int
     */
    int updateSettlementStateByBatchId(Map<String, Object> params);



    /**
     * 根据商户ID获取未结算小费流水数量
     * @author zhangzeyuan
     * @date 2021/8/11 17:54
     * @param merchantIds
     * @return int
     */
    int countUnsettledData(@Param("merchantIds")String merchantIds);

    /**
     * 根据商户ID获取未结算小费列表
     * @author zhangzeyuan
     * @date 2021/8/11 17:54
     * @param batchId
     * @return int
     */
    List<ClearFlowDetail> getUnsettledIdList(@Param("merchantIds")String merchantIds);


    /**
     * 更新小费流水 批次号ID
     * @author zhangzeyuan
     * @date 2021/8/11 17:55
     * @param params
     * @return int
     */
    int updateBatchIdByMerchantIds(Map<String, Object> params);


    /**
     *  组装clear detail数据
     * @author zhangzeyuan
     * @date 2021/8/11 17:56
     * @param batchId
     * @return java.util.List<com.uwallet.pay.main.model.entity.ClearDetail>
     */
    List<ClearDetail> getTipClearDetail(@Param("batchId")Long batchId);


    /**
     * 结算失败回滚
     * @author zhangzeyuan
     * @date 2021/8/12 16:37
     * @param params
     * @return int
     */
    int updateSettledStateRollback(Map<String, Object> params);


    List<QrPayFlowDTO> clearedDetailTransFlowList(Map<String, Object> params);

    int clearedDetailTransFlowCount(@Param("id") Long id);

}
