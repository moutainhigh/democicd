package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 捐赠机构
 * </p>
 *
 * @description: 捐赠机构
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:38:12
 */
@ApiModel("捐赠机构")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationInstituteDataDTO implements Serializable {


    /**
     * 捐赠机构ID
     */
    @ApiModelProperty(value = "捐赠机构ID")
    private Long id;

    /**
     * 捐赠机构名称
     */
    @ApiModelProperty(value = "捐赠机构名称")
    private String instituteName;


    /**
     * 捐赠机构金额
     */
    @ApiModelProperty(value = "捐赠机构金额")
    private BigDecimal donationAmount;
}
