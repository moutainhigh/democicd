package com.uwallet.pay.main.model.dto;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * <p>
 * 退款订单列表返回参数
 * </p>
 *
 * @description: 退款订单列表返回参数
 * @author: zhoutt
 * @date: Created in 2021-08-18 09:01:47
 */
@ApiModel("退款订单")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefundOrderListDTO {


    /**
     * 退款订单
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private  Long  id;
    /**
     * 原订单号
     */
    private String transNo;
    /**
     * 原订单id
     */
    private String qrPayFlowId;
    /**
     * 名
     */
    private String userFirstName;
    /**
     * 姓
     */
    private String userLastName;
    /**
     * 手机号
     */
    private String phone ;
    /**
     * 退款状态
     */
    private Integer state;
    /**
     * 交易商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 交易商户名称
     */
    private String practicalName;
    /**
     * 一级商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long superMerchantId;
    /**
     * 一级商户名称
     */
    private String superPracticalName;
    /**
     * 付款账号
     */
    private String cardNo;
    /**
     * 原订单金额
     */
    private BigDecimal transAmount;
    /**
     * 卡类型
     */
    private String cardCcType;
    /**
     * 红包金额
     */
    private BigDecimal redEnvelopeAmount;
    /**
     * 用户固定折扣金额
     */
    private BigDecimal baseDiscountAmount;
    /**
     * 商户周期内营销折扣金额
     */
    private BigDecimal extraDiscountAmount;
    /**
     * 商户可配置营销折扣金额
     */
    private BigDecimal markingDiscountAmount;
    /**
     * 用户整体销售折扣
     */
    private BigDecimal wholeSalesDiscountAmount;
    /**
     * 捐赠金额
     */
    private BigDecimal donationAmount;
    /**
     * 小费金额
     */
    private BigDecimal tipAmount;
    /**
     * 退款订单金额
     */
    private BigDecimal refundAmount;
    /**
     * 退款成功总金额
     */
    private BigDecimal refundTotalAmount;
    /**
     * 实收金额
     */
    private BigDecimal recAmount;
    /**
     * 退款时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long refundTime;
    /**
     * 订单创建时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;
    /**
     * 总期数
     */
    private Integer period;
    /**
     * 已还期数
     */
    private Integer paidPeriod;
    /**
     * 已还款金额
     */
    private BigDecimal paidAmount;
    /**
     * 交易类型
     */
    private Integer transType ;
    /**
     * 还款状态
     */
    private Integer borrowState;
    /**
     * 分期付单号
     */
    private Long borrowId;

    /**
     * 还款详情列表
     */
    private JSONArray repayPlan;
}
