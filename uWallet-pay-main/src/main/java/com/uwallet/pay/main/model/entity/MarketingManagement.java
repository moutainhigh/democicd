package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 商户营销码
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 商户营销码
 * @author: fenmi
 * @date: Created in 2021-10-27 15:38:04
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "商户营销码")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketingManagement extends BaseEntity implements Serializable {

  /**
   * 营销码单个金额
   */
  @ApiModelProperty(value = "营销码单个金额")
  private BigDecimal amount;
  /**
   * 营销总费用
   */
  @ApiModelProperty(value = "营销总费用")
  private BigDecimal totalAmount;
  /**
   * 营销码数量
   */
  @ApiModelProperty(value = "营销码数量")
  private Integer number;
  /**
   * 券码code
   */
  @ApiModelProperty(value = "券码code")
  private String code;
  /**
   * 可用状态 0：不可用 1：可用
   */
  @ApiModelProperty(value = "可用状态 0：不可用 1：可用")
  private Integer state;
  /**
   * 已使用个数
   */
  @ApiModelProperty(value = "已使用个数")
  private Integer usedNumber;
  /**
   * 已领取数量
   */
  @ApiModelProperty(value = "已领取数量")
  private Integer receiveNumber;
  /**
   * 最低消费金额
   */
  @ApiModelProperty(value = "最低消费金额")
  private BigDecimal minTransAmount;
  /**
   * 开始有效时间
   */
  @ApiModelProperty(value = "开始有效时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long validStartTime;
  /**
   * 结束有效时间
   */
  @ApiModelProperty(value = "结束有效时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long validEndTime;
  /**
   * 生成方式 0：系统生成 1：自定义
   */
  @ApiModelProperty(value = "生成方式 0：系统生成 1：自定义")
  private Integer createMethod;
  /**
   * 活动说明
   */
  @ApiModelProperty(value = "活动说明")
  private String activityDescription;
  /**
   * app展示说明
   */
  @ApiModelProperty(value = "app展示说明")
  private String description;
  /**
   * 营销码类型 1:营销  2.邀请码 100：其他(自定义）
   */
  @ApiModelProperty(value = "营销码类型 1:营销  2.邀请码 100：其他(自定义）")
  private Integer type;
  /**
   * 活动状态0 未开始 1 进行中 2 已结束 3 终止
   */
  @ApiModelProperty(value = "活动状态0 未开始 1 进行中 2 已结束 3 终止")
  private Integer activityState;
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
     * 邀请码 限制有效期 类型 一个月 一年 ...
     */
    @ApiModelProperty(value = "已领取数量")
    private Integer inviteValidityType;



    /**
     * 限制使用城市状态 0 不限制  大于0 限制 值 对应 城市的 static data code
     */
    private Integer cityLimitState;

    /**
     * 限制使用餐厅状态 0 不限制  大于0 限制 值 对应 商户ID
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long restaurantLimitState;

}
