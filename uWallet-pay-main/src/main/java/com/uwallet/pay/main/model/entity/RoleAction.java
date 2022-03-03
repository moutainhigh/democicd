package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 角色-权限关系表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.entity
 * @description: 角色-权限关系表
 * @author: Strong
 * @date: Created in 2019-09-16 17:51:57
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "角色-权限关系表")
public class RoleAction extends BaseEntity implements Serializable {

  /**
   * 角色id
   */
  @ApiModelProperty(value = "角色id")
  private Long roleId;
  /**
   * 权限id
   */
  @ApiModelProperty(value = "权限id")
  private Long actionId;

}
