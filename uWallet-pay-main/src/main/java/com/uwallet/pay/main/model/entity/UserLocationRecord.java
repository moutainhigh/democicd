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
 * 用户地理位置信息记录表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户地理位置信息记录表
 * @author: xucl
 * @date: Created in 2021-05-15 10:22:46
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户地理位置信息记录表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserLocationRecord extends BaseEntity implements Serializable {

  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 经度
   */
  @ApiModelProperty(value = "经度")
  private String lat;
  /**
   * 纬度
   */
  @ApiModelProperty(value = "纬度")
  private String lng;
  /**
   * 用户所在州
   */
  @ApiModelProperty(value = "用户所在州")
  private String userState;
  /**
   * 用户所在城市
   */
  @ApiModelProperty(value = "用户所在城市")
  private String userCity;
  /**
   * 用户所在街道
   */
  @ApiModelProperty(value = "用户所在街道")
  private String street;
  /**
   * 用户地址全量信息
   */
  @ApiModelProperty(value = "用户地址全量信息")
  private String fulladdress;

}
