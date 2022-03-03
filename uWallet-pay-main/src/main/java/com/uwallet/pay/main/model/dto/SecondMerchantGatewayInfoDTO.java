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
/**
 * <p>
 * 二级商户渠道信息表
 * </p>
 *
 * @description: 二级商户渠道信息表
 * @author: baixinyue
 * @date: Created in 2019-12-26 17:02:13
 */
@ApiModel("二级商户渠道信息表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecondMerchantGatewayInfoDTO extends BaseDTO implements Serializable {

    /**
     * 商户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "商户id")
    private Long merchantId;
    /**
     * 渠道id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "渠道id")
    private Long gatewayId;
    /**
     * 渠道方颁发的商户id
     */
    @ApiModelProperty(value = "渠道方颁发的商户id")
    private String gatewayMerchantId;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String gatewayMerchantPassword;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}
