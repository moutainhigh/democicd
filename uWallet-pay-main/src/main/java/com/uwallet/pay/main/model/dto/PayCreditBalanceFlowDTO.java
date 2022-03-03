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
 * @author: fenmi
 * @date: Created in 2021-07-07 10:38:54
 */
@ApiModel("")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayCreditBalanceFlowDTO extends BaseDTO implements Serializable {

    /**
     * 订单流水id
     */
    @ApiModelProperty(value = "订单流水id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long qrPayFlowId;
    /**
     * 1 冻结 2 解冻
     */
    @ApiModelProperty(value = "1 冻结 2 解冻")
    private Integer operateType;
    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 操作金额
     */
    @ApiModelProperty(value = "操作金额")
    private BigDecimal creditQuotaAmount;
    /**
     * 0 处理中 1 成功 2 失败 3 解冻可疑
     */
    @ApiModelProperty(value = "0 处理中 1 成功 2 失败 3 解冻可疑")
    private Integer state;

}
