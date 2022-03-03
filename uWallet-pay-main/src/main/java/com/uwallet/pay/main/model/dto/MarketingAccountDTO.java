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
 * 电子账户子户表
 * </p>
 *
 * @description: 电子账户子户表
 * @author: zhoutt
 * @date: Created in 2021-10-27 13:28:17
 */
@ApiModel("电子账户子户表")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketingAccountDTO  implements Serializable {

    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;
    /**
     * 面额
     */
    @ApiModelProperty(value = "面额")
    private BigDecimal balance;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String markingId;
    /**
     * 电子账户表ID
     */
    @ApiModelProperty(value = "电子账户表ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long accountId;
    /**
     * 可叠加：0：否
     */
    @ApiModelProperty(value = "可叠加：0：否")
    private Integer isOverlay;
    /**
     * 最后动账时间
     */
    @ApiModelProperty(value = "最后动账时间")
    private Long lastMoveDate;
    /**
     * 币种 AUD:澳币
     */
    @ApiModelProperty(value = "币种 AUD:澳币")
    private String currency;
    /**
     * 使用原因
     */
    @ApiModelProperty(value = "使用原因")
    private String reason;
    /**
     * 账户状态：1:未使用， 2：已使用  99 未激活
     */
    @ApiModelProperty(value = "账户状态：1:未使用， 2：已使用")
    private Integer state;


    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long createdDate;


}
