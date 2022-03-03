package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description:
 * @author: xucl
 * @date: Created in 2021-04-16 10:42:11
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IllionSubmitLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 提交的银行卡号
   */
  @ApiModelProperty(value = "提交的银行卡号")
  private String accountNumber;
  /**
   * 银行名称
   */
  @ApiModelProperty(value = "银行名称")
  private String bank;
  /**
   * 上送给illion订单号
   */
  @ApiModelProperty(value = "上送给illion订单号")
  private String referralCode;
  /**
   * 提交次数
   */
  @ApiModelProperty(value = "提交次数")
  private Integer submitNumber;
  /**
   * 上送状态 0失败，1成功
   */
  @ApiModelProperty(value = "上送状态 0失败，1成功")
  private Integer submittedStatus;
  /**
   * 三方返回错误信息
   */
  @ApiModelProperty(value = "三方返回错误信息")
  private String submittedError;
  /**
   * 用户手机号
   */
  @ApiModelProperty(value = "用户手机号")
  private String phone;
  /**
   * 状态0 加密失败，1可用，2未获取到，3获取成功
   */
  @ApiModelProperty(value = "状态0 加密失败，1可用，2未获取到，3获取成功")
  private Integer reportStatus;
  /**
   * 链接时间
   */
  @ApiModelProperty(value = "链接时间")
  private Long date;

}
