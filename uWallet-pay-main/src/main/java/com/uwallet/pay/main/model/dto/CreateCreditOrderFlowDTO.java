package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * 
 * </p>
 *
 * @description: 
 * @author: zhangzeyuan
 * @date: Created in 2021-07-07 11:21:54
 */
@ApiModel("")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCreditOrderFlowDTO extends BaseDTO implements Serializable {

    /**
     * 订单流水id
     */
    @ApiModelProperty(value = "订单流水id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long qrPayFlowId;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    /**
     * 0 处理中 1 成功 2 失败 3 可疑
     */
    @ApiModelProperty(value = "0 处理中 1 成功 2 失败 3 可疑")
    private Integer state;
    /**
     * 卡支付的费率
     */
    @ApiModelProperty(value = "卡支付的费率")
    private BigDecimal cardPayRate;
    /**
     * 卡支付的手续费
     */
    @ApiModelProperty(value = "卡支付的手续费")
    private BigDecimal cardFeeAmount;
    /**
     * 卡支付金额
     */
    @ApiModelProperty(value = "卡支付金额")
    private BigDecimal cardPayAmount;
    /**
     * 卡账户名
     */
    @ApiModelProperty(value = "卡账户名")
    private String cardAccountName;
    /**
     * 卡号
     */
    @ApiModelProperty(value = "卡号")
    private String cardNo;

}
