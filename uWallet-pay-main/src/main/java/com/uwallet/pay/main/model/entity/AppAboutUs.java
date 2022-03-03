package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * app 关于我们
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: app 关于我们
 * @author: baixinyue
 * @date: Created in 2019-12-19 11:28:53
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "app 关于我们")
public class AppAboutUs extends BaseEntity implements Serializable {

  /**
   * logo路径
   */
  @ApiModelProperty(value = "logo路径")
  private String path;
  /**
   * 简介
   */
  @ApiModelProperty(value = "简介")
  private String appIntro;
  /**
   * 电话
   */
  @ApiModelProperty(value = "电话")
  private String phone;
  /**
   * 邮箱
   */
  @ApiModelProperty(value = "邮箱")
  private String email;

}
