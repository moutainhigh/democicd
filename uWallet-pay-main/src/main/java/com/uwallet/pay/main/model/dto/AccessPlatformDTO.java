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
import java.math.BigDecimal;

/**
 * <p>
 * 接入方平台表
 * </p>
 *
 * @description: 接入方平台表
 * @author: zhoutt
 * @date: Created in 2020-09-27 09:50:12
 */
@ApiModel("接入方平台表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessPlatformDTO extends BaseDTO implements Serializable {

    /**
     * 接入方名称
     */
    @ApiModelProperty(value = "接入方名称")
    private String name;
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

}
