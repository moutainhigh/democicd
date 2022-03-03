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
 * 用户详情- 还款记录详情DTO
 * </p>
 *
 * @description: 用户详情 还款记录详情DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月22日14:12:33
 */
@ApiModel("用户详情- 还款记录详情DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailRepaymentHistoryDetailDTO implements Serializable {


    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 订单编号
     */
    @ApiModelProperty(value = "订单编号")
    private String transNo;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 类型 1 订单 2 逾期费
     */
    @ApiModelProperty(value = "类型")
    private Integer orderType;

    /**
     * 总期数
     */
    @ApiModelProperty(value = "总期数")
    private String periodQuantity;


    /**
     * 交易日期
     */
    @ApiModelProperty(value = "交易日期")
    private String transactionTime;

    /**
     * 本期应还期数编号
     */
    @ApiModelProperty(value = "本期应还期数编号")
    private String periodSort;


    /**
     * 本期应还金额
     */
    @ApiModelProperty(value = "本期应还金额")
    private BigDecimal repayAmount;
    /**
     * 本期还款状态 1：还款代扣，2：主动还款，3：财务平账
     */
    @ApiModelProperty(value = "本期还款状态")
    private Integer repayType;

    /**
     * 逾期天数
     */
    @ApiModelProperty(value = "逾期天数")
    private String overdueDays;

    @JsonSerialize(using = LongJsonSerializer.class)
    private Long createdDate;
}
