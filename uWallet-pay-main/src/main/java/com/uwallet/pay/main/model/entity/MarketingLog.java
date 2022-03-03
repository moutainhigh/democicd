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
 * 邀请查询记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 邀请查询记录表
 * @author: xucl
 * @date: Created in 2021-04-26 16:49:42
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "邀请查询记录表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarketingLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 红包金额
   */
  @ApiModelProperty(value = "红包金额")
  private BigDecimal amount;
  /**
   * 用户名集合
   */
  @ApiModelProperty(value = "用户名集合")
  private String userNameList;
  /**
   * 请求时间撮
   */
  @ApiModelProperty(value = "请求时间撮")
  private Long time;
  /**
   * 是否展示成功 1是 ，2否
   */
  @ApiModelProperty(value = "是否展示成功 1是 ，2否")
  private Integer isShow;

}
