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
 * pos基本信息
 * </p>
 *
 * @description: pos基本信息
 * @author: zhangzeyuan
 * @date: Created in 2021-03-19 15:17:59
 */
@ApiModel("pos基本信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PosInfoDTO extends BaseDTO implements Serializable {

    /**
     * POS机设备号
     */
    @ApiModelProperty(value = "POS机设备号")
    private String posId;
    /**
     * 商户ID
     */
    @ApiModelProperty(value = "商户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    /**
     * pos机型
     */
    @ApiModelProperty(value = "pos机型")
    private Integer posType;
    /**
     * 生产厂商
     */
    @ApiModelProperty(value = "生产厂商")
    private String manufacturer;
    /**
     * 系统厂商（对接三方厂商）
     */
    @ApiModelProperty(value = "系统厂商（对接三方厂商）")
    private String systemManufacturer;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 回调通知地址
     */
    @ApiModelProperty(value = "回调通知地址")
    private String callbackUrl;
    /**
     * 累计交易金额
     */
    @ApiModelProperty(value = "累计交易金额")
    private BigDecimal transAmount;
    /**
     * 累计交易订单数
     */
    @ApiModelProperty(value = "累计交易订单数")
    private Integer transOrderCount;

}
