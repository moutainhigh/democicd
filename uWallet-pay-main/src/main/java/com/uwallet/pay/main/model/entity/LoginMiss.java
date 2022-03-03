package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 登陆错误次数记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 登陆错误次数记录表
 * @author: baixinyue
 * @date: Created in 2020-01-02 13:56:13
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "登陆错误次数记录表")
public class LoginMiss extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  private Long userId;
  /**
   * 机会
   */
  @ApiModelProperty(value = "机会")
  private Integer chance;
  /**
   * 最后错误时间
   */
  @ApiModelProperty(value = "最后错误时间")
  private Long lastErrorTime;

}
