package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
 * 合伙人信息表
 * </p>
 *
 * @description: 合伙人信息表
 * @author: baixinyue
 * @date: Created in 2020-04-21 14:30:36
 */
@ApiModel("合伙人信息表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerDTO extends BaseDTO implements Serializable {

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private Long merchantId;
    /**
     * 名字
     */
    @ApiModelProperty(value = "名字")
    private String firstName;
    /**
     * 中名
     */
    @ApiModelProperty(value = "中名")
    private String middleName;
    /**
     * 姓
     */
    @ApiModelProperty(value = "姓")
    private String lastName;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String birth;
    /**
     * 证件类型
     */
    @ApiModelProperty(value = "证件类型")
    private Integer idType;
    /**
     * 证件号
     */
    @ApiModelProperty(value = "证件号")
    private String idNo;
    /**
     * 记录url
     */
    @ApiModelProperty(value = "记录url")
    private String idUrl;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String address;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String streetNumber;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String streetName;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String suburb;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private String state;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    private Integer country;

    /**
     * 股东份额
     */
    @ApiModelProperty(value = "股东份额")
    private String ownerShip;
    /**
     * 护照
     */
    @ApiModelProperty(value = "护照")
    private String passport;
    @ApiModelProperty("驾照所属州")
    private Integer licenseState;
}