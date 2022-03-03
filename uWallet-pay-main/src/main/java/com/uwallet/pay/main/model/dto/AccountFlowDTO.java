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
 * 账户动账交易流水表
 * </p>
 *
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2019-12-16 10:49:00
 */
@ApiModel("账户动账交易流水表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountFlowDTO extends BaseDTO implements Serializable {

    /**
     * 
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "")
    private Long userId;
    /**
     * 前置交易流水
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "前置交易流水")
    private Long flowId;
    /**
     * 子账户类型（0：钱包余额子户，1：理财余额子户）
     */
    @ApiModelProperty(value = "子账户类型（0：钱包余额子户，1：理财余额子户）")
    private Integer accountType;
    /**
     * 交易金额
     */
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transAmount;
    /**
     * 手续费收取方向 0：己方 ，1：对手方
     */
    @ApiModelProperty(value = "手续费收取方向 0：己方 ，1：对手方")
    private Integer feeDirection;
    /**
     * 收取商户手续费金额
     */
    @ApiModelProperty(value = "收取商户手续费金额")
    private BigDecimal fee;
    /**
     * 
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "")
    private Long oppositeUserId;
    /**
     * 子账户类型（0：钱包余额子户，1：理财余额子户）
     */
    @ApiModelProperty(value = "子账户类型（0：钱包余额子户，1：理财余额子户）")
    private Integer oppositeAccountType;
    /**
     * 冻结金额
     */
    @ApiModelProperty(value = "冻结金额")
    private BigDecimal freezingAmount;
    /**
     * 发送账户交易流水
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "发送账户交易流水")
    private Long orderNo;
    /**
     * 回滚流水号
     */
    @ApiModelProperty(value = "回滚流水号")
    private Long rollBackNo;
    /**
     * 交易类型
     */
    @ApiModelProperty(value = "交易类型")
    private Integer transType;
    /**
     * 对账状态 0：未对账 1：已对账
     */
    @ApiModelProperty(value = "对账状态 0：未对账 1：已对账")
    private Integer checkState;
    /**
     * 对账时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "对账时间")
    private Long checkTime;
    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    private String returnMessage;
    /**
     * 错误码
     */
    @ApiModelProperty(value = "错误码")
    private String returnCode;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 
     */
    @ApiModelProperty(value = "交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 ")
    private Integer state;

    private int[] transTypeList;

}
