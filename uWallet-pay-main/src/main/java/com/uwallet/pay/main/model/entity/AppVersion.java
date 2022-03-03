package com.uwallet.pay.main.model.entity;

import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * <p>
 * APP版本号管理表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: APP版本号管理表
 * @author: aaron.S
 * @date: Created in 2020-12-02 14:07:13
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "APP版本号管理表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppVersion extends BaseEntity implements Serializable {

  /**
   * 版本号
   */
  @ApiModelProperty(value = "版本号")
  private String version;
  /**
   * 是否强制更新,  0: 非强制更新, 1: 强制更新
   */
  @ApiModelProperty(value = "是否强制更新,  0: 非强制更新, 1: 强制更新")
  private Integer needUpdate;
  /**
   * 设备类型, 1:苹果IOS, 2:安卓
   */
  @ApiModelProperty(value = "设备类型, 1:苹果IOS, 2:安卓")
  private Integer deviceType;
  /**
   * 上架国家, 1: 澳大利亚, 2: 中国
   */
  @ApiModelProperty(value = "上架国家, 1: 澳大利亚, 2: 中国")
  private Integer storeCountry;
  /**
   * 商店类型: 1: 苹果商店, 2: Google Play, 3: 华为商店
   */
  @ApiModelProperty(value = "商店类型: 1: 苹果商店, 2: Google Play, 3: 华为商店")
  private Integer storeType;
  /**
   * 修复内容(内部展示用).多条间','分割
   */
  @ApiModelProperty(value = "修复内容(内部展示用).多条间','分割")
  private String fixedInfo;
  /**
   * app展示用修复内容-中文版
   */
  @ApiModelProperty(value = "app展示用修复内容-中文版")
  private String displayInfoCn;
  /**
   * app展示用修复内容-英文版
   */
  @ApiModelProperty(value = "app展示用修复内容-英文版")
  private String displayInfoEn;
  /**
   * app类型: 1:payo 2: uBiz
   */
  @ApiModelProperty(value = "app类型: 1:payo 2: uBiz")
  private Integer appType;
  /**
   * 人工触发强制更新: 1:强制更新 0: 非强制更新
   */
  @ApiModelProperty(value = "人工触发强制更新: 1:强制更新 0: 非强制更新")
  private Integer manualForceUpdate;
}
