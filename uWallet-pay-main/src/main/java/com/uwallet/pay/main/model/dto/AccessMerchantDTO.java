package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
/**
 * <p>
 * 接入方商户表
 * </p>
 *
 * @description: 接入方商户表
 * @author: zhoutt
 * @date: Created in 2020-09-27 09:50:11
 */
@ApiModel("接入方商户表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessMerchantDTO extends BaseDTO implements Serializable {

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String name;
    /**
     * 商户证件编号
     */
    @ApiModelProperty(value = "商户证件编号")
    private String merchantIdNo;
    /**
     * 平台id
     */
    @ApiModelProperty(value = "平台id")
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
     * 是否平台自身 0：否 1：是
     */
    @ApiModelProperty(value = "是否平台自身 0：否 1：是")
    private Integer platformOwn;
    /**
     * 是否启用: 0.否 1.是
     */
    @ApiModelProperty(value = "是否启用: 0.否 1.是")
    private Integer state;

}
