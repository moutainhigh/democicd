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
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户每月统计表
 * </p>
 *
 * @description: 用户每月统计表
 * @author: zhoutt
 * @date: Created in 2021-04-08 16:40:22
 */
@ApiModel("用户每月统计表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMonthlyDataDTO extends BaseDTO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 
     */
    @ApiModelProperty(value = "日期")
    private Long date;
    /**
     * 
     */
    @ApiModelProperty(value = "节省金额")
    private BigDecimal savedAmount;
    /**
     * 
     */
    @ApiModelProperty(value = "实付金额")
    private BigDecimal payAmount;
    /**
     * 
     */
    @ApiModelProperty(value = "交易金额")
    private BigDecimal transAmount;

}
