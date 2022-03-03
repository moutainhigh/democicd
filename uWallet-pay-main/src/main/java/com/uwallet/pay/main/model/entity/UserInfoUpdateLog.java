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
 * 用户信息修改记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户信息修改记录表
 * @author: xucl
 * @date: Created in 2021-09-10 16:55:37
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户信息修改记录表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoUpdateLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 修改字段
   */
  @ApiModelProperty(value = "修改字段")
  private String updateId;
  /**
   * 修改数据
   */
  @ApiModelProperty(value = "修改数据")
  private String updateText;
  /**
   * 修改备注
   */
  @ApiModelProperty(value = "修改备注")
  private String remarks;
  /**
   * 提交JSON数据
   */
  @ApiModelProperty(value = "提交JSON数据")
  private String data;

}
