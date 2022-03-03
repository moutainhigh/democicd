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
 * 商户表
 * </p>
 *
 * @description: 商户表
 * @author: fenmi
 * @date: Created in 2019-12-16 15:54:18
 */
@ApiModel("商户表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditMerchantDTO extends BaseDTO implements Serializable {

    /**
     * 商户Id
     */
    @ApiModelProperty(value = "商户Id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;
    private Long userId;
    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;
    /**
     * 生意实体类型
     */
    @ApiModelProperty(value = "生意实体类型")
    private Integer businessType;
    /**
     * 主营业务
     */
    @ApiModelProperty(value = "主营业务")
    private Integer mainBusiness;
    /**
     * ABN 生意注册号
     */
    @ApiModelProperty(value = "ABN 生意注册号")
    private String abn;
    /**
     * 经营者名字
     */
    @ApiModelProperty(value = "经营者名字")
    private String soleTraderFirstName;
    /**
     * 经营者中名
     */
    @ApiModelProperty(value = "经营者中名")
    private String soleTraderMiddleName;
    /**
     * 经营者姓
     */
    @ApiModelProperty(value = "经营者姓")
    private String soleTraderLastName;
    /**
     * 银行账号
     */
    @ApiModelProperty(value = "银行账号")
    private String accountNo;
    /**
     * 让利用户
     */
    @ApiModelProperty(value = "让利用户")
    private BigDecimal percentageToUser;
    /**
     * 让利平台
     */
    @ApiModelProperty(value = "让利平台")
    private BigDecimal percentageToPlatform;
    /**
     * 商户促销打折费率
     */
    @ApiModelProperty(value = "商户促销打折费率")
    private BigDecimal discountOfMerchant;
    /**
     * 分期付开通状态 0：未开通，1：开通
     */
    @ApiModelProperty(value = "分期付开通状态 0：未开通，1：开通")
    private Integer installmentState;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;
    /**
     * 生意地址
     */
    @ApiModelProperty(value = "生意地址")
    private String address;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private String city;
    /**
     * 地区
     */
    @ApiModelProperty(value = "地区")
    private String region;
    /**
     * 邮编
     */
    @ApiModelProperty(value = "邮编")
    private String postcode;
    /**
     * 国家
     */
    @ApiModelProperty(value = "国家")
    private String country;
    /**
     * 税收类型
     */
    @ApiModelProperty(value = "税收类型")
    private Integer taxType;
    /**
     * acn
     */
    @ApiModelProperty(value = "acn")
    private String acn;
    @ApiModelProperty(value = "额外折扣")
    private BigDecimal extraDiscount;
    @ApiModelProperty(value = "额外折扣期限")
    private Long extraDiscountPeriod;

    private BigDecimal discountPackage;
    /**
     * 三方商户平台id
     */
    private Long accessSideId;

    /**
     * bsb
     */
    private String bsb;
    /**
     * 户名
     */
    private String accountName;
    /**
     * 商户来源
     */
    private Integer merchantSource;

}
