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
 * api商户信息表j
 * </p>
 *
 * @package:  com.uwallet.pay.main.entity
 * @description: api商户信息表j
 * @author: zhoutt
 * @date: Created in 2021-09-02 17:35:15
 * @copyright: Copyright (c) 2021
 */
@Data
@ApiModel(description = "api商户信息表j")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMerchant extends BaseEntity implements Serializable {

  /**
   * 上级商户id
   */
  @ApiModelProperty(value = "上级商户id")
  @JsonSerialize(using = LongJsonSerializer.class)
  private Long superMerchantId;
  /**
   * 商户等级
   */
  @ApiModelProperty(value = "商户等级")
  private Integer merchantClass;
  /**
   * 生意实体类型
   */
  @ApiModelProperty(value = "生意实体类型")
  private Integer entityType;
  /**
   * 商户文件
   */
  @ApiModelProperty(value = "商户文件")
  private String fileList;
  /**
   * 信托文件
   */
  @ApiModelProperty(value = "信托文件")
  private String trusteeFileList;
  /**
   * 是否可用 0：不可用 1：可用
   */
  @ApiModelProperty(value = "是否可用 0：不可用 1：可用")
  private Integer isAvailable;
  /**
   * ABN 生意注册号
   */
  @ApiModelProperty(value = "ABN 生意注册号")
  private String abn;
  /**
   * ACN  公司注册号
   */
  @ApiModelProperty(value = "ACN  公司注册号")
  private String acn;
  /**
   * 公司名称
   */
  @ApiModelProperty(value = "公司名称")
  private String corporateName;
  /**
   * 实用名称
   */
  @ApiModelProperty(value = "实用名称")
  private String practicalName;
  /**
   * 信托类型
   */
  @ApiModelProperty(value = "信托类型")
  private Integer trusteeType;
  /**
   * 商户电话
   */
  @ApiModelProperty(value = "商户电话")
  private String businessPhone;
  /**
   * email
   */
  @ApiModelProperty(value = "email")
  private String email;
  /**
   * 邮编
   */
  @ApiModelProperty(value = "邮编")
  private String postcode;
  /**
   * 银行账号BSB
   */
  @ApiModelProperty(value = "银行账号BSB")
  private String bsb;
  /**
   * 银行账号
   */
  @ApiModelProperty(value = "银行账号")
  private String accountNo;
  /**
   * 开户人姓名
   */
  @ApiModelProperty(value = "开户人姓名")
  private String accountName;
  /**
   * 银行账号开户行
   */
  @ApiModelProperty(value = "银行账号开户行")
  private String bankName;
  /**
   * 国家
   */
  @ApiModelProperty(value = "国家")
  private String county;
  /**
   * 生意地址(经营地址)
   */
  @ApiModelProperty(value = "生意地址(经营地址)")
  private String address;
  /**
   * 街道号
   */
  @ApiModelProperty(value = "街道号")
  private String streetNumber;
  /**
   * 街道名
   */
  @ApiModelProperty(value = "街道名")
  private String streetName;
  /**
   * 区
   */
  @ApiModelProperty(value = "区")
  private String suburb;
  /**
   * 生意地址 城市
   */
  @ApiModelProperty(value = "生意地址 城市")
  private String city;
  /**
   * 生意地址 省
   */
  @ApiModelProperty(value = "生意地址 省")
  private String province;
  /**
   * 生意地址 州
   */
  @ApiModelProperty(value = "生意地址 州")
  private String merchantState;
  /**
   * 官网
   */
  @ApiModelProperty(value = "官网")
  private String websites;
  /**
   * 运营时间
   */
  @ApiModelProperty(value = "运营时间")
  private Integer operationTime;
  /**
   * 预计年销售额
   */
  @ApiModelProperty(value = "预计年销售额")
  private Integer estimatedAnnualSales;
  /**
   * 每笔交易平均销售额
   */
  @ApiModelProperty(value = "每笔交易平均销售额")
  private Integer avgSalesValue;
  /**
   * 借记卡/信用卡交易额度
   */
  @ApiModelProperty(value = "借记卡/信用卡交易额度")
  private Integer salesValueByCard;
  /**
   * 商户平台服务费费率
   */
  @ApiModelProperty(value = "商户平台服务费费率")
  private BigDecimal marchantRate;
  /**
   * 支付合约
   */
  @ApiModelProperty(value = "支付合约")
  private String paymentContract;
  /**
   * 分期付合约
   */
  @ApiModelProperty(value = "分期付合约")
  private String installmentContract;
  /**
   * 备注
   */
  @ApiModelProperty(value = "备注")
  private String remark;
  /**
   * 审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中 3:变更审核中 4：变更审核拒绝
   */
  @ApiModelProperty(value = "审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中 3:变更审核中 4：变更审核拒绝")
  private Integer state;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String asicExtractFileUrl;
  /**
   * 
   */
  @ApiModelProperty(value = "")
  private String asicExtractFileName;
  /**
   * docusign返回envelopeId
   */
  @ApiModelProperty(value = "docusign返回envelopeId")
  private String docusignEnvelopeid;
  /**
   * 是否签署docusign， 0：未签 1：已签署
   */
  @ApiModelProperty(value = "是否签署docusign， 0：未签 1：已签署")
  private Integer docusignHasSigned;
  /**
   * docusign文件路径
   */
  @ApiModelProperty(value = "docusign文件路径")
  private String docusignFiles;
  /**
   * 是否有trustee文件
   */
  @ApiModelProperty(value = "是否有trustee文件")
  private Integer trusteeDeed;
  /**
   * 0:personal 1:corporate
   */
  @ApiModelProperty(value = "0:personal 1:corporate")
  private Integer trusteeCompanyType;
  /**
   * 签署人
   */
  @ApiModelProperty(value = "签署人")
  private String docusignSigner;
  /**
   * 注册名和交易名是否相同 1：相同 0：不同
   */
  @ApiModelProperty(value = "注册名和交易名是否相同 1：相同 0：不同")
  private String tradingIsNoRegistered;
  /**
   * 合同类型 0：全部 1：非全部
   */
  @ApiModelProperty(value = "合同类型 0：全部 1：非全部")
  private Integer contractType;
  /**
   * 签署人标题
   */
  @ApiModelProperty(value = "签署人标题")
  private Integer authorisedTitle;
  /**
   * docusign签署开始时间
   */
  @ApiModelProperty(value = "docusign签署开始时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long contractStartTime;
  /**
   * docusign签署结束时间
   */
  @ApiModelProperty(value = "docusign签署结束时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long contractEndTime;
  /**
   * 审核通过时间
   */
  @ApiModelProperty(value = "审核通过时间")
  @JsonSerialize(using = LongDateSerializer.class)
  private Long merchantApprovePassTime;
  /**
   * 用户支付基础折扣
   */
  @ApiModelProperty(value = "用户支付基础折扣")
  private BigDecimal basePayRate;
  /**
   * 合同类型 默认 0 电子合同 Electronic Contract  1 纸质合同paper contract
   */
  @ApiModelProperty(value = "合同类型 默认 0 电子合同 Electronic Contract  1 纸质合同paper contract")
  private Integer docusignContractType;
  /**
   *
   */
  @ApiModelProperty(value = "")
  private String logoUrl;
  /**
   * 商家简介
   */
  @ApiModelProperty(value = "商家简介")
  private String intro;
  /**
   * 关键词
   */
  @ApiModelProperty(value = "关键词")
  private String keyword;
  /**
   * 类别, 1. casual dining, 2.cafe, 3, bar,4.asian,5. fast food, 6. desserts
   */
  @ApiModelProperty(value = "类别, 1. casual dining, 2.cafe, 3, bar,4.asian,5. fast food, 6. desserts")
  private Integer categories;

  /**
   * 商户IDkey
   */
  @ApiModelProperty(value = "商户IDkey")
  private String key;
  /**
   * 商户令牌
   */
  @ApiModelProperty(value = "商户令牌")
  private String secret;
  /**
   * 商户平台服务费费率选择
   */
  @ApiModelProperty(value = "商户平台服务费费率选择")
  private Integer marchantRateChoice;
  /**
   * 商户基础折扣选择
   */
  @ApiModelProperty(value = "商户基础折扣选择")
  private Integer basePayRateChoice;
  /**
   * 推销员
   */
  @ApiModelProperty(value = "推销员")
  private String representativeName;
  @ApiModelProperty(value = "其他实体")
  private String otherEntity;
}
