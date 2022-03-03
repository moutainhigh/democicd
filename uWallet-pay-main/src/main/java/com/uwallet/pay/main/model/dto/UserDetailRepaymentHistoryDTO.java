package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户详情- 还款记录DTO
 * </p>
 *
 * @description: 用户详情 还款记录DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月22日14:12:33
 */
@ApiModel("用户详情- 还款记录DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailRepaymentHistoryDTO implements Serializable {

    /**
     * Repayment Serial Number
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 还款金额
     * total amount = 还款金额 + 手续费
     */
    @ApiModelProperty(value = "还款金额")
    private BigDecimal repayAmount;

    /**
     * 手续费
     */
    @ApiModelProperty(value = "手续费")
    private BigDecimal charge;

    /**
     * 手续费费率
     */
    @ApiModelProperty(value = "手续费费率")
    private BigDecimal chargeRate;


    /**
     * 还款账户类型 0： 账户还款  1：卡还款
     */
    @ApiModelProperty(value = "还款账户类型")
    private Integer payType;


    /**
     * 还款账户/卡号
     */
    @ApiModelProperty(value = "还款账户/卡号")
    private String bankCardNumber;


    /**
     * 卡类型
     * 10、VISA, 20、MAST, 30、 SWITCH,  40、SOLO,  50、DELTA, 60、 AMEX
     */
    @ApiModelProperty(value = "卡类型")
    private Integer cardCcType;

    /**
     * Repayment Type
     * 交易类型：1：商户结算，2：主动还款，3：服务费代扣，4：还款代扣，5：财务平账
     */
    @ApiModelProperty(value = "卡类型")
    private Integer repaymentType;


    /**
     * Repayment Status
     * 0：失败，1：成功，2：交易中
     */
    @ApiModelProperty(value = "Repayment Status")
    private Integer repaymentStatus;


    /**
     * 还款时间
     */
    @ApiModelProperty(value = "还款时间")
    private String repaymentTimeStr;


    @JsonSerialize(using = LongJsonSerializer.class)
    private Long repaymentTime;

    /**
     * 错误信息
     * */
    private String errorMessage;

}
