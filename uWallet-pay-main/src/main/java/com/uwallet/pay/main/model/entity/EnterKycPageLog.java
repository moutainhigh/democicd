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
 * 
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 
 * @author: zhangzeyuan
 * @date: Created in 2021-08-13 15:47:46
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnterKycPageLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 进入kyc页面次数
   */
  @ApiModelProperty(value = "进入kyc页面次数")
  private Integer times;

}
