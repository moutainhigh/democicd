package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: liming
 * @Date: 2020/10/22 15:17
 * @Description: 整体出售明细
 */
@Data
public class MerchantWholeSalesFlowInfoDTO {

    /**
     * 整体出售流水id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 结算金额
     */
    private BigDecimal settlementAmount;

    /**
     * 结算时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long settlementTime;

    /**
     * 结算状态
     */
    private Integer settlementState;

    /**
     * 延时结算状态
     */
    private Integer settlementDelay;

    /**
     * 商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;

    /**
     * 商户名
     */
    private String merchantName;

    /**
     * 银行编码
     */
    private String bsb;

    /**
     * 账户号
     */
    private String accountNo;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 银行名称
     */
    private String bankName;

}
