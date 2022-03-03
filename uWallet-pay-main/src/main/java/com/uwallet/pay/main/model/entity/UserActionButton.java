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
 * 用户冻结表存在该表的用户可以被冻结和解冻
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户冻结表存在该表的用户可以被冻结和解冻
 * @author: xucl
 * @date: Created in 2021-09-10 09:35:21
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户冻结表存在该表的用户可以被冻结和解冻")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionButton extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 操作类型0账号冻结,1分期付冻结
   */
  @ApiModelProperty(value = "操作类型0账号冻结,1分期付冻结")
  private Integer type;

}
