package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@ApiModel("平台商户信息")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessPlatformInfoDTO  extends BaseDTO implements Serializable {
    /**
     * 接入方名称
     */
    @ApiModelProperty(value = "接入方名称")
    private String name;
    /**
     * 商户证件编号
     */
    @ApiModelProperty(value = "商户证件编号")
    private String merchantIdNo;
    /**
     * 平台证件id
     */
    @ApiModelProperty(value = "平台证件id")
    private String platformIdNo;
    /**
     * 接入方密钥
     */
    @ApiModelProperty(value = "接入方密钥")
    private String uuid;
    /**
     * 接入方信息
     */
    @ApiModelProperty(value = "接入方信息")
    private String accessSideInfo;
    /**
     * 是否启用: 0.否 1.是
     */
    @ApiModelProperty(value = "是否启用: 0.否 1.是")
    private Integer state;
    /**
     * 服务费费率
     */
    @ApiModelProperty(value = "服务费费率")
    private BigDecimal serverFeeRate;
    /**
     * 折扣费率
     */
    @ApiModelProperty(value = "折扣费率")
    private BigDecimal discountRate;

    /**
     * 平台编号
     */
    @ApiModelProperty(value = "平台编号")
    private String platformId;
    /**
     * bsb
     */
    @ApiModelProperty(value = "bsb")
    private String bsb;
    /**
     * 账户名
     */
    @ApiModelProperty(value = "账户名")
    private String accountName;
    /**
     * 结算账号
     */
    @ApiModelProperty(value = "结算账号")
    private String accountNo;
    /**
     * 平台商户号
     */
    @ApiModelProperty(value = "平台商户号")
    private String merchantId;
}
