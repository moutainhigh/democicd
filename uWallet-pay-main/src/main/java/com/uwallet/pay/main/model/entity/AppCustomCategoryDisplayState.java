package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * <p>
 * APP首页自定义分类 每个州展示商户、图片信息
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: APP首页自定义分类 每个州展示商户、图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 16:10:18
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "APP首页自定义分类 每个州展示商户、图片信息")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCustomCategoryDisplayState extends BaseEntity implements Serializable {

  /**
   * 对应 u_app_custom_category display_order字段
   */
  @ApiModelProperty(value = "对应 u_app_custom_category display_order字段")
  private Integer displayOrder;
  /**
   * 澳大利亚州名
   */
  @ApiModelProperty(value = "澳大利亚州名")
  private String stateName;

  private Integer categoryType;
  /**
   * 分类名称
   */
  @ApiModelProperty(value = "分类名称")
  private String categoryName;
  /**
   * 图片数量
   */
  @ApiModelProperty(value = "图片数量")
  private Integer imageTotal;
  /**
   * 图片JSON集合
   */
  @ApiModelProperty(value = "图片JSON集合")
  private String imagesJson;
  /**
   * 商户id ,拼接字符串
   */
  @ApiModelProperty(value = "商户id ,拼接字符串")
  private String merchantIds;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;


    /**
     * 商户展示类型 0 自定义商户 1 和哪个州一样
     */
    @ApiModelProperty(value = "商户展示类型 0 自定义商户 1 和哪个州一样")
    private Integer merchantDisplayType;
    /**
     * 商户展示方式 1 自定义 2 距离
     */
    @ApiModelProperty(value = "商户展示方式 1 自定义 2 距离")
    private Integer merchantShowType;
    /**
     * merchant_display_type为1时 州名
     */
    @ApiModelProperty(value = "merchant_display_type为1时 州名")
    private String merchantSameStateName;

    @Transient
    private String ids;

    /**
     * 更换分类时清空商户信息
     */
    @Transient
    private String clearMerchatStr;
}
