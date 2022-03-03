package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * APP首页自定义分类展示商户信息
 * </p>
 *
 * @description: APP首页自定义分类展示商户信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-08 13:35:28
 */
@ApiModel("APP首页自定义分类展示信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantAppHomePageDTO extends BaseDTO implements Serializable {

    /**
     * 商户ID
     */
    @ApiModelProperty(value = "商户ID")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long merchantId;

    /**
     * 实用名称
     */
    @ApiModelProperty(value = "实用名称")
    private String practicalName;

    /**
     * 商户主图
     */
    @ApiModelProperty(value = "商户主图")
    private String logoUrl;


    /**
     * 商户tag拼接信息
     */
    private List<String> tags;

    /**
     * 收藏状态 1 收藏 0 未收藏
     */
    private Integer collectionStatus;

    /**
     * 商户tag拼接信息
     */
    private String keyword;

    /**
     * 分类显示名-数据字典en_name
     */
    @ApiModelProperty(value = "分类显示名")
    private String categoriesStr;
    /**
     * 分类显示名-数据字典value
     */
    @ApiModelProperty(value = "分类显示名")
    private Integer categories;
    /**
     * 到用户的距离
     */
    @ApiModelProperty(value = "到用户的距离")
    private String distance;


    /**
     * 到用户的距离 排序用
     */
    @ApiModelProperty(value = "到用户的距离")
    private BigDecimal distanceOrder;

    /**
     * 商户地址经纬度
     */
    @ApiModelProperty(value = "")
    private String lng;

    @ApiModelProperty(value = "")
    private String lat;


    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称")
    private String categoryName;


    /**
     * 是否还有整体出售额度, 0:无,1:有
     */
    @ApiModelProperty(value = "是否还有整体出售额度, 0:无,1:有")
    private Integer haveWholeSell;

    /**
     * 整体出售支付用户折扣
     */
    @ApiModelProperty(value = "整体出售支付用户折扣")
    private BigDecimal wholeSaleUserPayDiscount;


    /**
     * 整体销售用户折扣
     */
    @ApiModelProperty(value = "整体销售用户折扣")
    private BigDecimal wholeSaleUserDiscount;

    /**
     * 营销折扣率
     */
    @ApiModelProperty(value = "营销折扣率")
    private BigDecimal marketingDiscount;


    @ApiModelProperty(value = "额外折扣期限")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long extraDiscountPeriod;


    @ApiModelProperty(value = "额外折扣")
    private BigDecimal extraDiscount;


    /**
     * 用户支付基础折扣
     */
    @ApiModelProperty(value = "用户支付基础折扣")
    private BigDecimal basePayRate;



    @ApiModelProperty(value = "基础折扣")
    private BigDecimal baseRate;

    /**
     * 用户折扣
     */
    @ApiModelProperty(value = "用户折扣")
    private String userDiscount;

}
