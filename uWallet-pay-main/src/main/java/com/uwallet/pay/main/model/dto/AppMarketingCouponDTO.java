package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.BigDecimalJsonSerializer;
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

/**
 * <p>
 * 商户营销码
 * </p>
 *
 * @description: APP展示营销码信息
 * @author: fenmi
 * @date: Created in 2021-10-27 15:38:04
 */
@ApiModel("商户营销码")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppMarketingCouponDTO implements Serializable {

    /**
     * 券ID
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long couponId;

    /**
     * 营销ID
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long marketingId;

    /**
     * 营销码单个金额
     */
    @ApiModelProperty(value = "营销码单个金额")
    private String amount;

    /**
     * 券码code
     */
    @ApiModelProperty(value = "券码code")
    private String code;

    /**
     * 有效期限制状态  0 不限制 1 有时间限制
     */
    @ApiModelProperty(value = "有效期限制状态  0 不限制 1 有时间限制")
    private Integer validityLimitState;
    /**
     * 限制使用金额状态 0 不限制 1 限制
     */
    @ApiModelProperty(value = "限制使用金额状态 0 不限制 1 限制")
    private Integer amountLimitState;

    /**
     * 最低消费金额
     */
    @ApiModelProperty(value = "最低消费金额")
    private String minTransAmount;

    /**
     * 结束有效时间
     */
    @ApiModelProperty(value = "结束有效时间")
    private String validEndTimeStr;

    /**
     * app展示说明
     */
    @ApiModelProperty(value = "app展示说明")
    private String description;

    /**
     * app展示状态 1可用 2 已使用  3 过期 4 终止
     */
    @ApiModelProperty(value = "app展示状态 1可用 2 已使用  3 过期 4 终止")
    private Integer showState;

    /**
     * 营销码类型 1:营销  2.邀请码 100：其他(自定义）
     */
    @ApiModelProperty(value = "营销码类型 1:营销  2.邀请码 100：其他(自定义）")
    private Integer type;

    /**
     * 使用时间
     */
    @ApiModelProperty(value = "使用时间")
    private String usedTimeStr;
    /**
     * 使用时间
     */
    @ApiModelProperty(value = "使用时间")
    private String usedAtMerchantName;

    /**
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间")
    private String expiredTimeStr;

    /**
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间")
    private String terminatedTimeStr;

    /**
     *  app展示券状态 1 可用 黄use 2 不可用 灰use
     */
    @ApiModelProperty(value = "app展示券状态 1 可用 黄use 2 不可用 灰use")
    private Integer payUseState;


    /**
     *  排序用时间 创建时间
     */
    private Long orderTime;
    /**
     *  排序用时间 动账时间
     */
    private Long modifyTime;

    /**
     *  分组用数据类型  1 可用 2 过期或已使用
     */
    private Integer dataType;

}
