package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 消息表
 * @author: baixinyue
 * @date: Created in 2019-12-13 17:55:07
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "消息表")
public class Notice extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  private Long userId;
  /**
   * 标题
   */
  @ApiModelProperty(value = "标题")
  private String title;
  /**
   * 内容
   */
  @ApiModelProperty(value = "内容")
  private String content;
  /**
   * 0：未读 1：已读
   */
  @ApiModelProperty(value = "0：未读 1：已读")
  private Integer isRead;
  /**
   * 消息类型
   */
  @ApiModelProperty(value = "消息类型")
  private Integer type;

}
