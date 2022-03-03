package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * 充值转账路由表
 * </p>
 *
 * @description: 充值转账路由表
 * @author: zhoutt
 * @date: Created in 2019-12-17 10:36:56
 */
@ApiModel("充值转账路由表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RechargeRouteDTO extends BaseDTO implements Serializable {

    /**
     * 渠道类型（0：代扣）
     */
    @ApiModelProperty(value = "渠道类型（0：代扣）")
    private Integer gatewayType;
    /**
     * 费率值
     */
    @ApiModelProperty(value = "费率值")
    private Double rate;
    /**
     * 工作时间  0:自然日 1：工作日
     */
    @ApiModelProperty(value = "工作时间  0:自然日 1：工作日")
    private Integer workType;
    /**
     * 最大金额
     */
    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxAmount;
    /**
     * 最小金额
     */
    @ApiModelProperty(value = "最小金额")
    private BigDecimal minAmount;
    /**
     * 每日起始时间
     */
    @ApiModelProperty(value = "每日起始时间")
    private String startTime;
    /**
     * 每日截止时间
     */
    @ApiModelProperty(value = "每日截止时间")
    private String endTime;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}
