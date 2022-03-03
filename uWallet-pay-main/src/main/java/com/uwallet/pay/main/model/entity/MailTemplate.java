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
 * @date: Created in 2020-01-04 13:56:55
 * @copyright: Copyright (c) 2020
 */
@Data
@ApiModel(description = "模板")
public class MailTemplate extends BaseEntity implements Serializable {

  /**
   * 模板名称
   */
  @ApiModelProperty(value = "模板名称")
  private String name;
  /**
   * 发送节点
   */
  @ApiModelProperty(value = "发送节点")
  private String sendNode;
  /**
   * 阿里云短信模板编码
   */
  @ApiModelProperty(value = "阿里云短信模板编码")
  private String aliCode;
  /**
   * 邮件主题
   */
  @ApiModelProperty(value = "邮件主题")
  private String mailTheme;
  /**
   * 邮件主题
   */
  @ApiModelProperty(value = "邮件主题")
  private String enMailTheme;
  /**
   * 收件人类型
   */
  @ApiModelProperty(value = "收件人类型")
  private Integer receiverType;
  /**
   * 内容模板
   */
  @ApiModelProperty(value = "内容模板")
  private String sendContent;
  /**
   * 内容模板
   */
  @ApiModelProperty(value = "内容模板")
  private String enSendContent;
  /**
   * 是否启用1：启用，0：禁用
   */
  @ApiModelProperty(value = "是否启用1：启用，0：禁用")
  private Integer state;
  @ApiModelProperty(value = "0:不发语音 1: 发语音")
  private Integer voice;
  /**
   * 跳转页面 1：还款页面 2：主页面
   */
  @ApiModelProperty(value = "跳转页面 1：还款页面 2：主页面")
  private Integer route;

}
