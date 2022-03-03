package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 董事信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 董事信息表
 * @author: Rainc
 * @date: Created in 2020-01-03 13:32:16
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "董事信息表")
public class Director extends BaseEntity implements Serializable {

  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  private Long merchantId;
  /**
   * 董事名字
   */
  @ApiModelProperty(value = "董事名字")
  private String firstName;
  /**
   * 董事中名
   */
  @ApiModelProperty(value = "董事中名")
  private String middleName;
  /**
   * 董事 姓
   */
  @ApiModelProperty(value = "董事 姓")
  private String lastName;
  /**
   * 董事证件类型
   */
  @ApiModelProperty(value = "董事证件类型")
  private String idType;
  /**
   * 董事证件号
   */
  @ApiModelProperty(value = "董事证件号")
  private String idNo;
  /**
   * 董事邮箱
   */
  @ApiModelProperty(value = "董事邮箱")
  private String email;
  /**
   * 董事出生日期
   */
  @ApiModelProperty(value = "董事出生日期")
  private String birth;
  /**
   * 董事图片
   */
  @ApiModelProperty(value = "董事图片")
  private String idUrl;
  /**
   * 董事地址
   */
  @ApiModelProperty(value = "董事地址")
  private String address;
  /**
   * 护照
   */
  @ApiModelProperty(value = "护照")
  private String passport;
  @ApiModelProperty("驾照所属州")
  private Integer licenseState;

}
