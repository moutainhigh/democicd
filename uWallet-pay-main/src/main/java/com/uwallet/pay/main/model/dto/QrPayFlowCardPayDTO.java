package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 扫码支付
 * </p>
 *
 * @description: 卡支付订单
 * @author: zhoutt
 * @date: Created in 2019-12-18 09:25:21
 */
@ApiModel("卡支付订单")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QrPayFlowCardPayDTO extends BaseDTO implements Serializable {

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String transNo;
    /**
     * 用户编号
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "用户编号")
    private Long payUserId;
    /**
     * 用户姓名
     */
    @ApiModelProperty(value = "用户姓名")
    private String userName;
    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String practicalName;
    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal transAmount;
    /**
     * 实付金额
     */
    @ApiModelProperty(value = "实付金额")
    private BigDecimal payAmount;
    /**
     * 折扣金额
     */
    @ApiModelProperty(value = "折扣金额")
    private BigDecimal discountAmount;
    /**
     * 红包金额
     */
    @ApiModelProperty(value = "红包金额")
    private BigDecimal redEnvelopeAmount;
    /**
     * 平台服务费
     */
    @ApiModelProperty(value = "平台服务费")
    private BigDecimal platformFee;
    /**
     * 商户结算金额
     */
    @ApiModelProperty(value = "商户结算金额")
    private BigDecimal recAmount;
    /**
     * 结算时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "结算时间")
    private Long clearTime;
    /**
     * 业务模式 0：正常销售 1：整体销售 2：混合销售
     */
    @ApiModelProperty(value = "业务模式 0：正常销售 1：整体销售 2：混合销售")
    private Integer saleType;
    /**
     * 付款方式 2：卡支付，22：分期付
     */
    @ApiModelProperty(value = "付款方式 2：卡支付，22：分期付")
    private Integer transType;
    /**
     * 是否需要清算给商户 0：不需要  1：需要
     */
    @ApiModelProperty(value = "是否需要清算给商户 0：不需要  1：需要")
    private Integer isNeedClear;
    /**
     * 支付通道
     */
    @ApiModelProperty(value = "支付通道")
    private String channelName;
    /**
     * 对账状态 0：未对账 1：已对账 2：无需对账
     */
    @ApiModelProperty(value = "对账状态 0：未对账 1：已对账 2：无需对账")
    private Integer checkState;
    /**
     * 对账时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "对账时间")
    private Long checkTime;
    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String phone;
    /**
     * 用户名字
     */
    private String userLastName;
    /**
     * 用户名字
     */
    private String  userFirstName;
    /**
     *  订单状态
     */
    private Integer state;

    /**
     * 错误信息
     * */
    private String errorMessage;
}
