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
 * 整体出售清算中间表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 整体出售清算中间表
 * @author: joker
 * @date: Created in 2020-10-22 09:28:22
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "整体出售清算中间表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WholeSalesFlowAndClearDetail extends BaseEntity implements Serializable {

  /**
   * 整体出售流水id
   */
  @ApiModelProperty(value = "整体出售流水id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long wholeSalesFlowId;
  /**
   * 清算批次id
   */
  @ApiModelProperty(value = "清算批次id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long clearBatchId;
  /**
   * 清算明细id
   */
  @ApiModelProperty(value = "清算明细id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long clearDetailId;
  /**
   * 清算流水明细id
   */
  @ApiModelProperty(value = "清算流水明细id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long clearFlowDetailId;

}
