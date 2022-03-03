package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 联系我们
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 联系我们
 * @author: baixinyue
 * @date: Created in 2020-06-17 08:52:22
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "联系我们")
public class ContactUs extends BaseEntity implements Serializable {

  /**
   * 姓名
   */
  @ApiModelProperty(value = "姓名")
  private String name;
  /**
   * 电话
   */
  @ApiModelProperty(value = "电话")
  private String mobile;
  /**
   * 邮箱
   */
  @ApiModelProperty(value = "邮箱")
  private String email;
  /**
   * 内容
   */
  @ApiModelProperty(value = "内容")
  private String message;
  /**
   * 状态
   */
  @ApiModelProperty(value = "状态")
  private Integer state;

}
