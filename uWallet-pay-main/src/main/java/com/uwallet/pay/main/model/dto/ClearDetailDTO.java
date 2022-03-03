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
 * 清算批次明细表
 * </p>
 *
 * @description: 清算批次明细表
 * @author: zhoutt
 * @date: Created in 2019-12-20 10:58:28
 */
@ApiModel("清算批次明细表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClearDetailDTO extends BaseDTO implements Serializable {

    /**
     * 清算批次号
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "清算批次号")
    private Long clearBatchId;
    /**
     * 用户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /**
     * 商户号
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "商户号")
    private Long merchantId;
    /**
     * 清算条数
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "清算条数")
    private Long clearNumber;
    /**
     * 实际清算总金额
     */
    @ApiModelProperty(value = "实际清算总金额")
    private BigDecimal clearAmount;
    /**
     * 应清算总金额
     */
    @ApiModelProperty(value = "应清算总金额")
    private BigDecimal transAmount;
    /**
     * 订单总金额
     */
    @ApiModelProperty(value = "订单总金额")
    private BigDecimal borrowAmount;
    /**
     * abn
     */
    @ApiModelProperty(value = "abn")
    private String abn;
    /**
     * BSB
     */
    @ApiModelProperty(value = "BSB")
    private String bsb;
    /**
     * 账户号
     */
    @ApiModelProperty(value = "账户号")
    private String accountNo;
    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;
    /**
     * 清算状态 0：处理中 1：处理成功 2：失败
     */
    @ApiModelProperty(value = "清算状态 0：处理中 1：处理成功 2：失败")
    private Integer state;
    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String corporateName;

    private String accountName;

    private String transTime;

    private String realName;

    private Integer clearType;

    private String fileName;

    private String practicalName;
    private Integer city;

}
