package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 节假日表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 节假日表
 * @author: baixinyue
 * @date: Created in 2020-09-08 11:24:52
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "节假日表")
public class HolidaysConfig extends BaseEntity implements Serializable {

  /**
   * 年
   */
  @ApiModelProperty(value = "年")
  private Integer year;
  /**
   * 节假日 MM-dd
   */
  @ApiModelProperty(value = "节假日 MM-dd")
  private String holidays;

}
