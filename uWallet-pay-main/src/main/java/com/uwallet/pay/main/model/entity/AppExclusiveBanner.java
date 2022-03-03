package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * APP首页banner、市场推广图片配置表
 * </p>
 *
 * @package:  com.fenmi.generator.entity
 * @description: APP首页banner、市场推广图片配置表
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 10:03:42
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "APP首页banner、市场推广图片配置表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppExclusiveBanner extends BaseEntity implements Serializable {

  /**
   * 展示类型 1 app 首页  banner  2 app 首页市场推广Payo exclusives
   */
  @ApiModelProperty(value = "展示类型 1 app 首页  banner  2 app 首页市场推广Payo exclusives")
  private Integer displayType;
  /**
   * 展示顺序（唯一索引，值越大显示越靠后 ）
   */
  @ApiModelProperty(value = "展示顺序（唯一索引，值越大显示越靠后 ）")
  private Integer displayOrder;
  /**
   * 标题
   */
  @ApiModelProperty(value = "标题")
  private String title;
  /**
   * 副标题
   */
  @ApiModelProperty(value = "副标题")
  private String subTitle;
  /**
   * 展示图片地址
   */
  @ApiModelProperty(value = "展示图片地址")
  private String imageUrl;
  /**
   * 展示次数限制（如填写9999999， 代表舞次数限制）
   */
  @ApiModelProperty(value = "展示次数限制（如填写9999999， 代表舞次数限制）")
  private Integer limitTimes;
  /**
   * 指向类别（ 值对应u_static_data value. 1、 H5 link 2、APP link 3、No link 4、Customized）
   */
  @ApiModelProperty(value = "指向类别（ 值对应u_static_data value. 1、 H5 link 2、APP link 3、No link 4、Customized）")
  private Integer redirectType;
  /**
   * 当redirect_type 为1  时指向的H5链接地址
   */
  @ApiModelProperty(value = "当redirect_type 为1  时指向的H5链接地址")
  private String redirectH5LinkAddress;
  /**
   * 当redirect_type 为2  时的APP指向地址（ 值对应u_static_data:appRedirectAppLink 1、 Share to earn 2、About us dedicated 3、Apply for Instalment 4、Map）
   */
  @ApiModelProperty(value = "当redirect_type 为2  时的APP指向地址（ 值对应u_static_data:appRedirectAppLink 1、 Share to earn 2、About us dedicated 3、Apply for Instalment 4、Map）")
  private Integer redirectAppLinkType;
  /**
   * 自定义展示方式 1Full screen  2  Semi Popup
   */
  @ApiModelProperty(value = "自定义展示方式 1Full screen  2  Semi Popup")
  private Integer redirectCustomizedDisplayType;
  /**
   * 当redirect_type 为4时的自定义标题
   */
  @ApiModelProperty(value = "当redirect_type 为4时的自定义标题")
  private String redirectCustomizedTitle;
  /**
   * 当redirect_type 为4时的自定义图片地址
   */
  @ApiModelProperty(value = "当redirect_type 为4时的自定义图片地址")
  private String redirectCustomizedImageUrl;
  /**
   * 当redirect_type 为4时的自定义内容（带HTML格式）
   */
  @ApiModelProperty(value = "当redirect_type 为4时的自定义内容（带HTML格式）")
  private String redirectCustomizedContent;
  /**
   * 当display_type 为1时使用 关闭效果状态 0 默认 不显示 1 显示
   */
  @ApiModelProperty(value = "当display_type 为1时使用 关闭效果状态 0 默认 不显示 1 显示")
  private Integer turnOffEffectStatus;
  /**
   * 当display_type 为1时使用 turn_off_effect_status 为 1 时 展示的蚊子描述
   */
  @ApiModelProperty(value = "当display_type 为1时使用 turn_off_effect_status 为 1 时 展示的蚊子描述")
  private String turnOffTextDisplay;
  /**
   * 0 不可用 1 可用
   */
  @ApiModelProperty(value = "0 不可用 1 可用")
  private Integer state;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;
  /**
   * 关闭效果点击跳转类型1、 H5 link 2、APP link 3、No link 
   */
  @ApiModelProperty(value = "关闭效果点击跳转类型1、 H5 link 2、APP link 3、No link ")
  private Integer turnOffRedirectType;
  /**
   * 当turn_off_redirect_type为1  时指向的H5链接地址
   */
  @ApiModelProperty(value = "当turn_off_redirect_type为1  时指向的H5链接地址")
  private String turnOffRedirectH5Link;
  /**
   * 当redirect_type 为2  时的APP指向地址（ 值对应u_static_data:appRedirectAppLink 1、 Share to earn 2、About us dedicated 3、Apply for Instalment 4、Map）
   */
  @ApiModelProperty(value = "当redirect_type 为2  时的APP指向地址（ 值对应u_static_data:appRedirectAppLink 1、 Share to earn 2、About us dedicated 3、Apply for Instalment 4、Map）")
  private Integer turnOffRedirectAppLinkType;

}
