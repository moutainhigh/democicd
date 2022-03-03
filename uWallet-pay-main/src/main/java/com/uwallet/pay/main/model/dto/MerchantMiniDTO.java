package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 商户信息表
 * </p>
 *
 * @description: 商户信息表
 * @author: aaron s
 * @date: Created in 2021-04-08 16:38:52
 */
@ApiModel("商户信息表")
@Builder
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantMiniDTO implements Serializable {
    /**
     *商户id
     */
    @ApiModelProperty(value = "")
    private Long merchantId;
    /**
     *2021-0406新增字段 商户类型, 数据字典同名
     */
    @ApiModelProperty(value = "")
    private Integer categories;

    /**
     * 是否还有整体出售额度, 0:无,1:有
     */
    @ApiModelProperty(value = "是否还有整体出售额度, 0:无,1:有")
    private Integer haveWholeSell;
    /**
     * 是否可用 0：不可用 1：可用
     */
    @ApiModelProperty(value = "是否可用 0：不可用 1：可用")
    private Integer isAvailable;
    /**
     * 实用名称
     */
    @ApiModelProperty(value = "实用名称")
    private String tradingName;
    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址")
    private String logoUrl;
    /**
     * 纬度
     */
    @ApiModelProperty(value = "纬度")
    private String lat;
    /**
     * 经度
     */
    @ApiModelProperty(value = "经度")
    private String lng;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private String city;
    @ApiModelProperty(value = "tag 字符串")
    private List<String> tags ;
    @ApiModelProperty(value = "卡支付折扣")
    private BigDecimal cardPayDiscount;
    @ApiModelProperty(value = "分期付支付折扣")
    private BigDecimal installmentDiscount;
    /**
     * 距离
     */
    private BigDecimal distance;
    /**
     * 用户折扣
     */
    @ApiModelProperty(value = "用户折扣")
    private BigDecimal userDiscount;
    /**
     * 是否被用户收藏
     */
    @ApiModelProperty(value = "是否被用户收藏")
    private Integer isFavorite;


    @Tolerate
    public MerchantMiniDTO(){};
}
