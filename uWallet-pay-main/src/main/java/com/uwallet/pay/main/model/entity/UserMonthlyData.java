package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户每月统计表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户每月统计表
 * @author: zhoutt
 * @date: Created in 2021-04-08 16:40:22
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户每月统计表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMonthlyData extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   *
   */
  @ApiModelProperty(value = "日期")
  private Long date;
  /**
   *
   */
  @ApiModelProperty(value = "节省金额")
  private BigDecimal savedAmount;
  /**
   *
   */
  @ApiModelProperty(value = "实付金额")
  private BigDecimal payAmount;
  /**
   *
   */
  @ApiModelProperty(value = "交易金额")
  private BigDecimal transAmount;

}
