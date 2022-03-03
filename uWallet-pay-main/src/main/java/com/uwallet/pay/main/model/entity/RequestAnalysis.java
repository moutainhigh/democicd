package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * <p>
 * 接口请求数据统计表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 接口请求数据统计表
 * @author: aaronS
 * @date: Created in 2021-02-06 14:03:58
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "接口请求数据统计表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestAnalysis extends BaseEntity implements Serializable {
  /**
   * userId
   */
  @ApiModelProperty(value = "userId")
  private Long userId;
  /**
   * 请求编号
   */
  @ApiModelProperty(value = "请求编号")
  private String requestId;
  /**
   * APP类型, ios-用户:1,安卓-用户:2,ios-商户:3,安卓-商户:4
   */
  @ApiModelProperty(value = "APP类型, ios-用户:1,安卓-用户:2,ios-商户:3,安卓-商户:4")
  private Integer appType;
  /**
   * 接口名
   */
  @ApiModelProperty(value = "接口名")
  private String gatewayName;
  /**
   * 请求方式
   */
  @ApiModelProperty(value = "请求方式")
  private String requestMethod;
  /**
   * 请求开始时间
   */
  @ApiModelProperty(value = "请求开始时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long startTime;

  @ApiModelProperty(value = "请求结束时间-毫秒")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long finishedTime;
  /**
   * 请求返回时间
   */
  @ApiModelProperty(value = "请求返回时间")
  private String completedTime;
  /**
   * 请求参数
   */
  @ApiModelProperty(value = "请求参数")
  private String requestParams;
  /**
   * 返回数据
   */
  @ApiModelProperty(value = "返回数据")
  private String responseData;
  /**
   * 返回数据大小
   */
  @ApiModelProperty(value = "返回数据大小")
  private Double responseSize;
  /**
   * 请求IP
   */
  @ApiModelProperty(value = "请求IP")
  private String requestIp;
  /**
   * 请求状态: 0.失败 1.成功
   */
  @ApiModelProperty(value = "请求状态: 0.失败 1.成功")
  private Integer state;

}
