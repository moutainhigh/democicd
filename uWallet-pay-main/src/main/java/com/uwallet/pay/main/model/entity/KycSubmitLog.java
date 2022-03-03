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
 * @date: Created in 2021-04-08 13:24:29
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycSubmitLog extends BaseEntity implements Serializable {

  /**
   * kyc数据
   */
  @ApiModelProperty(value = "kyc数据")
  private String kycData;
  /**
   * 用户id
   */
  @ApiModelProperty(value = "用户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long userId;
  /**
   * 是否请求三方成功1:成功，2失败，0未请求
   */
  @ApiModelProperty(value = "是否请求三方成功1:成功，2失败，0未请求")
  private Integer isRequest;
  /**
   * 用户账号提交KYC次数
   */
  @ApiModelProperty(value = "用户账号提交KYC次数")
  private Integer accountSubmittedTimes;
  /**
   * kyc状态0,1 Accept, 2: reject, 3 DataSource not available
   */
  @ApiModelProperty(value = "kyc状态0,1 Accept, 2: reject, 3 DataSource not available")
  private Integer kycStatus;
  /**
   * 是否调用watchlist 1是，2不是
   */
  @ApiModelProperty(value = "是否调用watchlist 1是，2不是")
  private Integer isWatchlist;
  /**
   * 连接时间
   */
  @ApiModelProperty(value = "连接时间")
  private Long date;
  /**
   * 用户手机号
   */
  @ApiModelProperty(value = "用户手机号")
  private String phone;

}
