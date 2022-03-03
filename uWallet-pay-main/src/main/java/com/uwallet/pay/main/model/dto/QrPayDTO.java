package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.math.BigDecimal;

@ApiModel("扫码支付req项")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrPayDTO extends BaseDTO implements Serializable {


    /**
     * 付款userId
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long payUserId;

    /**
     * 收款userId
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long recUserId;

    /**
     * 收款userId
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;

    /**
     * 交易金额
     */
    private BigDecimal transAmount;
    /**
     * 实付金额
     */
    private BigDecimal trulyPayAmount;
    /**
     * 支付方式  0:卡支付  4：分期支付
     */
    private Integer payType;

    /**
     * 手续费金额
     */
    private BigDecimal feeAmt ;

    /**
     * 手续费收取方向 0：收款方 ，1：付款方
     */
    private Integer feeDirection;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 卡id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long cardId;
    /**
     * 费率
     */
    private BigDecimal rate;
    /**
     * 支付折扣率
     */
    @ApiModelProperty(value = "支付折扣率")
    private BigDecimal payDiscountRate;

    /**
     * 整体出售金额
     */
    private BigDecimal wholeSalesAmount;


    /**
     * 正常出售金额
     */
    private BigDecimal normalSalesAmount;

    /**
     * 红包金额
     */
    private BigDecimal redEnvelopeAmount;

    private String productId;

    private Integer isShow;

    private String transNo;

    /**
     * pos订单号
     */
    private String posTransNo;


    /**
     * 红包金额
     */
    private BigDecimal creditNeedCardPayAmount;

    /**
     * 捐赠
     */
    private String donationInstiuteId;


    /**
     * 小费金额
     */
    private BigDecimal tipAmount;

    /**
     * 支付绑卡状态 1 有卡不需要绑卡 0 没有卡 先绑卡在支付
     */
    @Transient
    private Integer payBindCardState;

    /**
     * 卡号
     */
    @Transient
    private String cardNo;

    /**
     * 卡过期月份
     */
    @Transient
    private String customerCcExpmo;
    /**
     * 卡过期年份
     */
    @Transient
    private String customerCcExpyr;

    /**
     * 银行卡背面的安全码
     */
    @Transient
    private String customerCcCvc;

    /**
     * 营销券Id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long marketingId;

    private BigDecimal marketingBalance;

    /**
     * 营销规则ID
     */
    @ApiModelProperty(value = "营销规则ID")
    private Long marketingManageId;


    /**
     * 营销类型
     */
    @ApiModelProperty(value = "营销规则ID")
    private Integer marketingType;


}
