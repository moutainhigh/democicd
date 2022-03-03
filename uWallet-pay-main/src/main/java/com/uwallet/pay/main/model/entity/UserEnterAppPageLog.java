package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 用户APP页面流程记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户APP页面流程记录表
 * @author: zhangzeyuan
 * @date: Created in 2021-09-01 16:35:17
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户APP页面流程记录表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserEnterAppPageLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 页面类型
   */
  @ApiModelProperty(value = "页面类型")
  private Integer pageType;

}
