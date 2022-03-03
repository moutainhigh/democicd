package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * 小费流水表
 * </p>
 *
 * @description: 小费流水表
 * @author: zhangzeyuan
 * @date: Created in 2021-08-10 16:01:03
 */
@ApiModel("小费流水表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TipFlowDTO extends BaseDTO implements Serializable {

    /**
     * qrpayflow流水ID
     */
    @ApiModelProperty(value = "qrpayflow流水ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long flowId;
    /**
     * 付款用户id
     */
    @ApiModelProperty(value = "付款用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 商户id
     */
    @ApiModelProperty(value = "商户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * 小费金额
     */
    @ApiModelProperty(value = "小费金额")
    private BigDecimal tipAmount;
    /**
     * 待结算金额
     */
    @ApiModelProperty(value = "待结算金额")
    private BigDecimal settlementAmount;
    /**
     * 交易类型
     */
    @ApiModelProperty(value = "交易类型")
    private Integer transType;
    /**
     * 交易状态
     */
    @ApiModelProperty(value = "交易状态")
    private Integer state;
    /**
     * 结算时间
     */
    @ApiModelProperty(value = "结算时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long settlementTime;
    /**
     * 结算状态 0：未结算 1：已结算 2：结算中 3：延迟结算
     */
    @ApiModelProperty(value = "结算状态 0：未结算 1：已结算 2：结算中 3：延迟结算")
    private Integer settlementState;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 对账状态 0：未对账 1：已对账 2：无需对账
     */
    @ApiModelProperty(value = "对账状态 0：未对账 1：已对账 2：无需对账")
    private Integer checkState;
    /**
     * 对账时间
     */
    @ApiModelProperty(value = "对账时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long checkTime;
    /**
     * 批次号
     */
    @ApiModelProperty(value = "批次号")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long batchId;

}
