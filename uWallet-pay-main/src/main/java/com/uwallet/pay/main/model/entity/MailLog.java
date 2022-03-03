package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 邮件发送记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 邮件发送记录表
 * @author: zhoutt
 * @date: Created in 2020-01-07 15:46:48
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "邮件发送记录表")
public class MailLog extends BaseEntity implements Serializable {

  /**
   * 收件人地址
   */
  @ApiModelProperty(value = "收件人地址")
  private String address;
  /**
   * 邮件内容
   */
  @ApiModelProperty(value = "邮件内容")
  private String content;
  /**
   * 发送方式  0自动发送   1手动发送
   */
  @ApiModelProperty(value = "发送方式  0自动发送   1手动发送")
  private Integer sendType;

}
