package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 管理员 -角色关系表
 * </p>
 *
 * @package:  com.loancloud.rloan.main.entity
 * @description: 管理员 -角色关系表
 * @author: Strong
 * @date: Created in 2019-09-16 16:25:12
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "管理员 -角色关系表")
public class AdminRole extends BaseEntity implements Serializable {

  /**
   * 管理员id
   */
  @ApiModelProperty(value = "管理员id")
  private Long adminId;
  /**
   * 角色id
   */
  @ApiModelProperty(value = "角色id")
  private Long roleId;

}
