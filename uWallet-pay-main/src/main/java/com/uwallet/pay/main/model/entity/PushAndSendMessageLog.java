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
 * @date: Created in 2021-04-16 15:56:48
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PushAndSendMessageLog extends BaseEntity implements Serializable {

  /**
   * 请求json数据
   */
  @ApiModelProperty(value = "请求json数据")
  private String data;
  /**
   * 用户ID
   */
  @ApiModelProperty(value = "用户ID")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 发送类型1push2短信
   */
  @ApiModelProperty(value = "发送类型1push2短信")
  private Integer type;

}
