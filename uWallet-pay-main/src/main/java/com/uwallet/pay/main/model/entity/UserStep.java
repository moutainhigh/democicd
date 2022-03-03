package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户权限阶段
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户权限阶段
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:51:35
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "用户权限阶段")
public class UserStep extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 步骤：1：kyc 2：illion 3：分期付风控
   */
  @ApiModelProperty(value = "步骤：1：kyc 2：illion 3：分期付风控")
  private Integer step;
  /**
   * 0: 未开始 1：成功 2：失败 3：进行中
   */
  @ApiModelProperty(value = "0: 未开始 1：成功 2：失败 3：进行中")
  private Integer stepState;

}
