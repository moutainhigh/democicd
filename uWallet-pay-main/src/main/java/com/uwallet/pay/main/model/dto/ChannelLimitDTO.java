package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * 渠道日交易累计金额记录表
 * </p>
 *
 * @description: 渠道日交易累计金额记录表
 * @author: baixinyue
 * @date: Created in 2019-12-21 10:06:05
 */
@ApiModel("渠道日交易累计金额记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelLimitDTO extends BaseDTO implements Serializable {

    /**
     * 渠道id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "渠道id")
    private Long channelId;
    /**
     * 累计金额
     */
    @ApiModelProperty(value = "累计金额")
    private BigDecimal accruingAmount;

    /**
     * 日累计上限
     */
    private BigDecimal dailyTotalAmount;

}
