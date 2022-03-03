package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Lenovo
 */
@ApiModel("用户卡券")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPromotionDTO implements Serializable {
    @ApiModelProperty(value = "卡券ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    @ApiModelProperty(value = "卡券类型")
    private Integer type;

    @ApiModelProperty(value = "码")
    private String code;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "最小支付金额")
    private BigDecimal minTransAmount;

    @ApiModelProperty(value = "描述")
    private String  description;

    @ApiModelProperty(value = "过期日")
    private String expiredDate;

    @ApiModelProperty(value = "状态")
    private Integer promotionState;

    @ApiModelProperty(value = "交易类型")
    private Integer transType;

    @ApiModelProperty(value = "最后动账时间")
    private Long lastMoveDate;
}
