package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 商户端用户-权限关系表
 * </p>
 *
 * @package:  com.uwallet.pay.main.main.entity
 * @description: 商户端用户-权限关系表
 * @author: baixinyue
 * @date: Created in 2020-02-19 14:02:21
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "商户端用户-权限关系表")
public class UserAction extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  private Long userId;
  /**
   * 权限id
   */
  @ApiModelProperty(value = "权限id")
  private Long actionId;

}
