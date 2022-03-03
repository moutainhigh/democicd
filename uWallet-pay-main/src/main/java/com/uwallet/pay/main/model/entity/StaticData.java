package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 数据字典
 * @author: Strong
 * @date: Created in 2019-12-13 15:35:58
 * @copyright: Copyright (c) 2019
 */
@Data
@ApiModel(description = "数据字典")
public class StaticData extends BaseEntity implements Serializable {

  /**
   * 键
   */
  @ApiModelProperty(value = "键")
  private String code;
  /**
   * 名称
   */
  @ApiModelProperty(value = "名称")
  private String name;
  /**
   * 英文名称
   */
  @ApiModelProperty(value = "英文名称")
  private String enName;
  /**
   * 值
   */
  @ApiModelProperty(value = "值")
  private String value;
  /**
   * 父级
   */
  @ApiModelProperty(value = "父级")
  private String parent;
  /**
   * 是否内置属性; 0 ：否 1：是
   */
  @ApiModelProperty(value = "是否内置属性; 0 ：否 1：是")
  private Integer builtin;

}
