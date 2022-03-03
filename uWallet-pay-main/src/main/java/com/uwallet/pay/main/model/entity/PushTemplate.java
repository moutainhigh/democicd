package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 模板
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 模板
 * @author: zhoutt
 * @date: Created in 2020-01-04 13:52:28
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "模板")
public class PushTemplate extends BaseEntity implements Serializable {

  /**
   * 模板名称
   */
  @ApiModelProperty(value = "模板名称")
  private String name;
  /**
   * 发送节点
   */
  @ApiModelProperty(value = "发送节点")
  private Integer sendingNode;
  /**
   * 模板内容
   */
  @ApiModelProperty(value = "模板内容")
  private String content;
  /**
   * 状态
   */
  @ApiModelProperty(value = "状态")
  private Integer state;

}
