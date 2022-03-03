package com.uwallet.pay.main.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: 用户主表
 * @author: xucl
 * @date: Created in 2021-08-19 08:43:25
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "用户主表")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends BaseEntity implements Serializable {

  /**
   * 用户三要素md5
   */
  @ApiModelProperty(value = "用户三要素md5")
  private String uuid;
  /**
   * 账户类型（10：客户 ；20：商户）
   */
  @ApiModelProperty(value = "账户类型（10：客户 ；20：商户）")
  private Integer userType;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String password;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String payPassword;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String pinNumber;
  /**
   * 支付业务状态 0：不可用 1：可用 3:人工审核
   */
  @ApiModelProperty(value = "支付业务状态 0：不可用 1：可用 3:人工审核")
  private Integer paymentState;
  /**
   * 卡支付绑卡,状态,0:未开通,1:已经开通
   */
  @ApiModelProperty(value = "卡支付绑卡,状态,0:未开通,1:已经开通")
  private Integer cardState;
  /**
   * 分期付业务状态 0：不可用 1：可用 2：禁用 3:人工审核
   */
  @ApiModelProperty(value = "分期付业务状态 0：不可用 1：可用 2：禁用 3:人工审核")
  private Integer installmentState;
  /**
   * 理财业务状态 0：不可用 1：可用 3:人工审核
   */
  @ApiModelProperty(value = "理财业务状态 0：不可用 1：可用 3:人工审核")
  private Integer investState;
  /**
   * 手机号码
   */
  @ApiModelProperty(value = "手机号码")
  private String phone;
  /**
   * 邮箱
   */
  @ApiModelProperty(value = "邮箱")
  private String email;
  /**
   * 商户id
   */
  @ApiModelProperty(value = "商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long merchantId;
  /**
   * 用户角色 0：员工，1：店长
   */
  @ApiModelProperty(value = "用户角色 0：员工，1：店长")
  private Integer role;
  /**
   * 纬度
   */
  @ApiModelProperty(value = "纬度")
  private String lat;
  /**
   * 经度
   */
  @ApiModelProperty(value = "经度")
  private String lng;
  /**
   * 定位所在州
   */
  @ApiModelProperty(value = "定位所在州")
  private String userState;
  /**
   * 定位所在市
   */
  @ApiModelProperty(value = "定位所在市")
  private String userCity;
  /**
   * 推送token
   */
  @ApiModelProperty(value = "推送token")
  private String pushToken;
  /**
   * 是否同意理财 0:否 1：同意
   */
  @ApiModelProperty(value = "是否同意理财 0:否 1：同意")
  private Integer isInvestAgree;
  /**
   * 是否同意开通分期付
   */
  @ApiModelProperty(value = "是否同意开通分期付")
  private Integer isCreditAgree;
  /**
   * imeiNo
   */
  @ApiModelProperty(value = "imeiNo")
  private String imeiNo;
  /**
   * 邀请人
   */
  @ApiModelProperty(value = "邀请人")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long inviterId;
  /**
   * 邀请注册次数
   */
  @ApiModelProperty(value = "邀请注册次数")
  private Integer invitationToRegister;
  /**
   * 邀请消费
   */
  @ApiModelProperty(value = "邀请消费")
  private Integer inviteConsumption;
  /**
   * 用户邀请码
   */
  @ApiModelProperty(value = "用户邀请码")
  private String inviteCode;
  /**
   * 预计红包
   */
  @ApiModelProperty(value = "预计红包")
  private BigDecimal expectAmount;
  /**
   * 实得红包
   */
  @ApiModelProperty(value = "实得红包")
  private BigDecimal actualAmount;
  /**
   * 首次消费标识
   */
  @ApiModelProperty(value = "首次消费标识")
  private Integer firstDealState;
  /**
   * split补充还款账户信息标志位 0：不需要 1：需要
   */
  @ApiModelProperty(value = "split补充还款账户信息标志位 0：不需要 1：需要")
  private Integer splitAddInfoState;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String userFirstName;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String userLastName;
  /**
   * 注册来源,0app，1h5
   */
  @ApiModelProperty(value = "注册来源,0app，1h5")
  private Integer registerFrom;


    /**
     * 阅读协议状态 0 未读 1 已读
     * */
    private Integer readAgreementState;


    /**
     *   分期付卡还款协议勾选状态  0 未勾选过 1 已勾选
     * */
    private Integer creditCardAgreementState;

  /**
   * 最近一次登陆时间
   */
  @ApiModelProperty(value = "最近一次登陆时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long loginTime;


    /**
     * 机型
     * */
    @ApiModelProperty(value = "机型")
    private String phoneModel;

    /**
     * 版本号ID
     * */
    @ApiModelProperty(value = "版本号ID")
    private String appVersionId;

    /**
     * 拆分红包状态  1 已拆分 0 未拆分
     * */
    private Integer splitRedEnvelopeState;

  /**
   * 手机系统 0安卓 1IOS
   */
  @ApiModelProperty(value = "手机系统 1安卓 2IOS")
  private Integer phoneSystem;
  /**
   * 手机系统版本 如安卓10
   */
  @ApiModelProperty(value = "手机系统版本 如安卓10")
  private String phoneSystemVersion;
  /**
   * 手机型号 如vivonew
   */
  @ApiModelProperty(value = "手机型号 如vivonew")
  private String mobileModel;
  /**
   * stripe老用户标识 0 ：否 1：是
   */
  @ApiModelProperty(value = "stripe老用户标识 0 ：否 1：是")
  private Integer stripeState;
}
