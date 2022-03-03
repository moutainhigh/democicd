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
 * 用户详情- 使用红包记录DTO
 * </p>
 *
 * @description: 使用红包记录DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月22日14:12:33
 */
@ApiModel("使用红包记录DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailUsedPayoMoneyDTO implements Serializable {

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
     * Payo money
     */
    @ApiModelProperty(value = "Payo money")
    private BigDecimal payoMoney;


    /**
     * 交易时间
     */
    @ApiModelProperty(value = "交易时间")
    private String paidTime;


    /**
     * 订单状态
     */
    @ApiModelProperty(value = "订单状态")
    private Integer orderState;


    @JsonSerialize(using = LongJsonSerializer.class)
    private Long flowId;
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;
}
