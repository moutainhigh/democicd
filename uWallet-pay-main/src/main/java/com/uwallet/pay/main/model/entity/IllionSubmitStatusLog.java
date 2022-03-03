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
 *
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description:
 * @author: xucl
 * @date: Created in 2021-06-22 16:52:43
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IllionSubmitStatusLog extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 提交批次号
   */
  @ApiModelProperty(value = "提交批次号")
  private Long batchNumber;
  /**
   * 状态
   */
  @ApiModelProperty(value = "状态")
  private Integer state;
  /**
   * 错误信息
   */
  @ApiModelProperty(value = "错误信息")
  private String errorMessage;

}
