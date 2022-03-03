package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 受益人信息表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 受益人信息表
 * @author: baixinyue
 * @date: Created in 2020-04-20 17:16:45
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "受益人信息表")
public class Shareholder extends BaseEntity implements Serializable {

  /**
   * 商户号
   */
  @ApiModelProperty(value = "商户号")
  private Long merchantId;
  /**
   * 名字
   */
  @ApiModelProperty(value = "名字")
  private String firstName;
  /**
   * 中名
   */
  @ApiModelProperty(value = "中名")
  private String middleName;
  /**
   * 姓
   */
  @ApiModelProperty(value = "姓")
  private String lastName;
  /**
   * 证件类型
   */
  @ApiModelProperty(value = "证件类型")
  private Integer idType;
  /**
   * 证件号
   */
  @ApiModelProperty(value = "证件号")
  private String idNo;
  /**
   * 记录url
   */
  @ApiModelProperty(value = "记录url")
  private String idUrl;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String address;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String birth;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String streetNumber;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String streetName;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String suburb;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String state;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private Integer country;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String postcode;

  /**
   * 股东份额
   */
  @ApiModelProperty(value = "股东份额")
  private String ownerShip;
  /**
   * 护照
   */
  @ApiModelProperty(value = "护照")
  private String passport;
  @ApiModelProperty("驾照所属州")
  private Integer licenseState;

}
