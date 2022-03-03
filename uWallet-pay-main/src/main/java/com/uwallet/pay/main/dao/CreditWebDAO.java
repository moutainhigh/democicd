package com.uwallet.pay.main.dao;

import com.uwallet.pay.main.model.dto.DiscountPackageInfoDTO;
import com.uwallet.pay.main.model.dto.MerchantWholeSalesFlowInfoDTO;
import com.uwallet.pay.main.model.dto.SettlementInfoDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author: liming
 * @Date: 2020/10/19 16:06
 * @Description: CreditWeb服务
 */
@Mapper
public interface CreditWebDAO {

    /**
     * 查询整体出售结算信息
     * @param params 条件
     * @return 结果
     */
    List<DiscountPackageInfoDTO> searchMerchantList(Map<String, Object> params);

    /**
     * 统计查询
     * @param params 条件
     * @return 结果
     */
    int searchMerchantListCount(Map<String, Object> params);

    /**
     * 修改整体出售流水状态
     * @param params 条件
     * @return 受影响行数
     */
    int updateWholeSalesFlow(Map<String, Object> params);

    /**
     * 清算记录
     * @param params 查询条件
     * @return 结果集
     */
    List<SettlementInfoDTO> searchSettlementInfo(Map<String, Object> params);

    /**
     * 清算记录统计查询
     * @param params 查询条件
     * @return 结果集
     */
    int searchSettlementInfoCount(Map<String, Object> params);

    /**
     * 修改整体出售流水
     * @param params 条件
     * @return 受影响行数
     */
    int updateWholeSalesFlowInfo(Map<String, Object> params);

    /**
     * 修改清算明细
     * @param params 条件
     * @return 受影响行数
     */
    int updateClearFlowDetail(Map<String, Object> params);

    /**
     * 查询整体出售流水明细
     * @param params 查询条件
     * @return 结果集
     */
    List<MerchantWholeSalesFlowInfoDTO> searchMerchantWholeSalesFlowInfo(Map<String, Object> params);

    /**
     * 统计查询整体出售流水明细
     * @param params 查询条件
     * @return 结果
     */
    int searchMerchantWholeSalesFlowInfoCount(Map<String, Object> params);

    /**
     * 修改延时状态
     * @param params 条件
     * @return 结果集
     */
    int updateSettlementDelay(Map<String, Object> params);
}
