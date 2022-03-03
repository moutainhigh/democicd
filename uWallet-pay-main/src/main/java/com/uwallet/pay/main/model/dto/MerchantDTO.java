package com.uwallet.pay.main.model.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import com.uwallet.pay.core.util.MathUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Tolerate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 商户信息表
 * </p>
 *
 * @description: 商户信息表
 * @author: baixinyue
 * @date: Created in 2020-04-20 16:38:52
 */
@ApiModel("商户信息表")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantDTO extends BaseDTO implements Serializable {
    /**
     * 是否被用户收藏, 0:未收藏, 1: 收藏
     */
    @ApiModelProperty(value = "是否被用户收藏")
    private Integer isFavorite;

    /**
     * 类别的显示名
     */
    @ApiModelProperty(value = "类别的显示名")
    private String categoriesStr;
    /**
     *2021-0406新增字段 商户类型, 数据字典同名
     */
    @ApiModelProperty(value = "")
    private Integer categories;

    /**
     *
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    @ApiModelProperty(value = "")
    private JSONObject value;
    /**
     * 是否还有整体出售额度, 0:无,1:有
     */
    @ApiModelProperty(value = "是否还有整体出售额度, 0:无,1:有")
    private Integer haveWholeSell;
    /**
     * 生意实体类型
     */
    @ApiModelProperty(value = "生意实体类型")
    private Integer entityType;
    /**
     * 公司类型
     */
    @ApiModelProperty(value = "公司类型 ")
    private Integer companyType;
    /**
     * ASIC注册的公司类型 0:上市公司 1:私人有限公司
     */
    @ApiModelProperty(value = "ASIC注册的公司类型 0:上市公司 1:私人有限公司 ")
    private Integer asicCompanyType;
    /**
     * 非澳洲注册的公司类型 0:上市公司 1:私人有限公司 2：其他
     */
    @ApiModelProperty(value = "非澳洲注册的公司类型 0:上市公司 1:私人有限公司 2：其他 ")
    private Integer foreignCompanyType;
    /**
     * 是否非澳洲公司 0：否 1：是
     */
    @ApiModelProperty(value = "是否非澳洲公司 0：否 1：是")
    private Integer isForeign;
    /**
     * 是否国外注册 0：否 1：是
     */
    @ApiModelProperty(value = "是否国外注册 0：否 1：是")
    private Integer isForeignRegistered;
    /**
     * 是否可用 0：不可用 1：可用
     */
    @ApiModelProperty(value = "是否可用 0：不可用 1：可用")
    private Integer isAvailable;
    /**
     * 主营业务
     */
    @ApiModelProperty(value = "主营业务")
    private Integer mainBusiness;
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
     * 老板名字
     */
    @ApiModelProperty(value = "老板名字")
    private String ownerFirstName;
    /**
     * 老板中名
     */
    @ApiModelProperty(value = "老板中名")
    private String ownerMiddleName;
    /**
     * 老板姓
     */
    @ApiModelProperty(value = "老板姓")
    private String ownerLastName;
    /**
     * 老板生日
     */
    @ApiModelProperty(value = "老板生日")
    private String ownerBirth;
    /**
     * 经营者名字
     */
    @ApiModelProperty(value = "经营者名字")
    private String soleTraderFirstName;
    /**
     * 经营者中名
     */
    @ApiModelProperty(value = "经营者中名")
    private String soleTraderMiddleName;
    /**
     * 经营者姓
     */
    @ApiModelProperty(value = "经营者姓")
    private String soleTraderLastName;
    /**
     * 经营者证件类型
     */
    @ApiModelProperty(value = "经营者证件类型")
    private Integer soleTraderIdType;
    /**
     * 经营者证件号
     */
    @ApiModelProperty(value = "经营者证件号")
    private String soleTraderIdNo;
    /**
     * 经营者证件url
     */
    @ApiModelProperty(value = "经营者证件url")
    private String soleTraderIdUrl;
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
     * ASIC公司注册名称
     */
    @ApiModelProperty(value = "ASIC公司注册名称")
    private String registeredName;
    /**
     * 信托全名
     */
    @ApiModelProperty(value = "信托全名")
    private String trustFullName;
    /**
     * 建立信托的国家
     */
    @ApiModelProperty(value = "建立信托的国家")
    private Integer trustCountry;
    /**
     * 基金复选框信息
     */
    @ApiModelProperty(value = "基金复选框信息")
    private String trustCheck;
    /**
     * 1:personal 0：corporate
     */
    @ApiModelProperty(value = "1:personal 0：corporate")
    private Integer trusteeType;
    /**
     * 0:personal 1:corporate
     */
    @ApiModelProperty(value = "0:personal 1:corporate")
    private Integer trusteeCompanyType;
    /**
     * 是否有信托文件
     */
    @ApiModelProperty(value = "是否有信托文件")
    private Integer trusteeDeed;
    /**
     * 受托人完整商业名称
     */
    @ApiModelProperty(value = "受托人完整商业名称")
    private String trusteeFullBusinessName;
    /**
     * 信托建立人名字
     */
    @ApiModelProperty(value = "信托建立人名字")
    private String trustBuilderFirstName;
    /**
     * 信托建立人中名
     */
    @ApiModelProperty(value = "信托建立人中名")
    private String trustBuilderMiddleName;
    /**
     * 信托建立人姓
     */
    @ApiModelProperty(value = "信托建立人姓")
    private String trustBuilderLastName;
    /**
     * 受托人名字
     */
    @ApiModelProperty(value = "受托人名字")
    private String trusteeFirstName;
    /**
     * 受托人中名
     */
    @ApiModelProperty(value = "受托人中名")
    private String trusteeMiddleName;
    /**
     * 受托人姓
     */
    @ApiModelProperty(value = "受托人姓")
    private String trusteeLastName;
    /**
     * 受托人证件类型
     */
    @ApiModelProperty(value = "受托人证件类型")
    private Integer trusteeIdType;
    /**
     * 受托人证件号
     */
    @ApiModelProperty(value = "受托人证件号")
    private Integer trusteeIdNo;
    /**
     * 受托人证件url
     */
    @ApiModelProperty(value = "受托人证件url")
    private String trusteeIdUrl;
    /**
     * 受托人地址
     */
    @ApiModelProperty(value = "受托人地址")
    private String trusteeAddress;
    /**
     * 股东名字
     */
    @ApiModelProperty(value = "股东名字")
    private String shareholderFirstName;
    /**
     * 股东中名
     */
    @ApiModelProperty(value = "股东中名")
    private String shareholderMiddleName;
    /**
     * 股东姓
     */
    @ApiModelProperty(value = "股东姓")
    private String shareholderLastName;
    /**
     * 股东证件类型
     */
    @ApiModelProperty(value = "股东证件类型")
    private Integer shareholderIdType;
    /**
     * 股东证件号
     */
    @ApiModelProperty(value = "股东证件号")
    private String shareholderIdNo;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String shareholderIdUrl;
    /**
     * 股东地址
     */
    @ApiModelProperty(value = "股东地址")
    private String shareholderAddress;
    /**
     * 关键联系人名字
     */
    @ApiModelProperty(value = "关键联系人名字")
    private String liaisonFirstName;
    /**
     * 关键联系人 中名
     */
    @ApiModelProperty(value = "关键联系人 中名")
    private String liaisonMiddleName;
    /**
     * 关键联系人 姓
     */
    @ApiModelProperty(value = "关键联系人 姓")
    private String liaisonLastName;
    /**
     * 电话
     */
    @ApiModelProperty(value = "电话")
    private String phone;
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
     * 州名
     */
    private String stateName;
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
     * 是否有消费税/增值税 0：无 1：有
     */
    @ApiModelProperty(value = "是否有消费税/增值税 0：无 1：有 ")
    private Integer taxRegistered;
    /**
     * 税收类型
     */
    @ApiModelProperty(value = "税收类型")
    private Integer taxType;
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
     * 让利用户
     */
    @ApiModelProperty(value = "让利用户")
    private BigDecimal sellDiscount;
    /**
     * 平台所得
     */
    @ApiModelProperty(value = "平台所得")
    private BigDecimal rebateDiscount;
    /**
     * 支付让利用户
     */
    @ApiModelProperty(value = "支付让利用户")
    private BigDecimal paySellDiscount;
    /**
     * 支付平台所得
     */
    @ApiModelProperty(value = "支付平台所得")
    private BigDecimal payRebateDiscount;
    /**
     * 营销折扣率
     */
    @ApiModelProperty(value = "营销折扣率")
    private BigDecimal marketingDiscount;
    /**
     * 收费方式 0：商户付费 1：客户付费
     */
    @ApiModelProperty(value = "收费方式 0：商户付费 1：客户付费")
    private Integer chargeMode;
    /**
     * 商户费率
     */
    @ApiModelProperty(value = "商户费率")
    private String marchantRate;
    /**
     * 用户费率
     */
    @ApiModelProperty(value = "用户费率")
    private String userRate;
    /**
     * 支付业务状态 0：不可用 1：可用
     */
    @ApiModelProperty(value = "支付业务状态 0：不可用 1：可用")
    private Integer paymentState;
    /**
     * 分期付业务状态 0：不可用 1：可用
     */
    @ApiModelProperty(value = "分期付业务状态 0：不可用 1：可用")
    private Integer installmentState;
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
     * 推荐标志： 0：否 1：是
     */
    @ApiModelProperty(value = "推荐标志： 0：否 1：是")
    private Integer isTop;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
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
     * 审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中 3:变更审核中 4：变更审核拒绝
     */
    @ApiModelProperty(value = "审核状态：-1：审核拒绝 0:待审核  1：审核通过 2：审核中 3:变更审核中 4：变更审核拒绝")
    private Integer state;
    /**
     * 经纬度
     */
    @ApiModelProperty(value = "经纬度")
    private String longitudeLatitude;
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
     * 关键词
     */
    @ApiModelProperty(value = "关键词")
    private String keyword;
    /**
     * 公司地址（澳洲注册地址）
     */
    @ApiModelProperty(value = "公司地址（澳洲注册地址）")
    private String registeredAddress;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String registeredStreetNumber;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String registeredStreetName;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String registeredSuburb;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String registeredState;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private Integer registeredCountry;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String registeredPostcode;
    /**
     * 公司地址（非澳洲注册地址）
     */
    @ApiModelProperty(value = "公司地址（非澳洲注册地址）")
    private String foreignRegisteredAddress;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String foreignRegisteredStreetNo;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String foreignRegisteredStreetName;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String foreignRegisteredSuburb;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String foreignRegisteredState;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private Integer foreignRegisteredCountry;
    /**
     * 邮编
     */
    @ApiModelProperty(value = "邮编")
    private String foreignRegisteredPostcode;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private Integer partnerEstablishCountry;
    /**
     * 经营地址
     */
    @ApiModelProperty(value = "经营地址")
    private String businessAddress;
    /**
     * 经营地址：街道号
     */
    @ApiModelProperty(value = "经营地址：街道号")
    private String businessStreetNumber;
    /**
     * 经营地址：街道名称
     */
    @ApiModelProperty(value = "经营地址：街道名称")
    private String businessStreetName;
    /**
     * 经营地址：区
     */
    @ApiModelProperty(value = "经营地址：区")
    private String businessSuburb;
    /**
     * 经营地址：州
     */
    @ApiModelProperty(value = "经营地址：州")
    private String businessState;
    /**
     * 经营地址：邮编
     */
    @ApiModelProperty(value = "经营地址：邮编")
    private String businessPostcode;
    /**
     * 经营地址：国家
     */
    @ApiModelProperty(value = "经营地址：国家")
    private Integer businessCountry;

    // =============================================================================

    /**
     * 股东信息
     */
    @ApiModelProperty(value = "股东信息")
    private List<ShareholderDTO> shareholderDTOList;

    /**
     * 合伙人信息
     */
    @ApiModelProperty(value = "合伙人信息")
    private List<PartnerDTO> partnerDTOList;

    /**
     * 受托人信息
     */
    @ApiModelProperty(value = "受托人信息")
    private List<TrusteeDTO> trusteeDTOList;

    /**
     * 受益人信息
     */
    @ApiModelProperty(value = "受益人信息")
    private List<BeneficiaryDTO> beneficiaryDTOList;

    /**
     * 董事信息集合
     */
    @ApiModelProperty(value = "董事信息集合")
    private List<DirectorDTO> directorDTOList;

    /**
     * 联系人
     */
    @ApiModelProperty(value = "联系人")
    private List<ContactPersonDTO> contactPersonDTOList;

    /**
     * 用户折扣
     */
    @ApiModelProperty(value = "用户折扣")
    private BigDecimal userDiscount;
    /**
     * h5 主营业务
     */
    private String mainBusinessEnName;

    /**
     * 二维码
     */
    @ApiModelProperty(value = "二维码")
    private String qrCode;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String arbn;

    /**
     * 距离
     */
    private BigDecimal distance;
    /**
     * 发现h5折扣
     */
    private BigDecimal discoverDiscount;

    /**
     * 推荐排序
     */
    @ApiModelProperty(value = "推荐排序")
    private Integer topSort;

    /**
     * 推销员
     */
    @ApiModelProperty(value = "推销员")
    private String representativeName;

    /**
     * 商户电话
     */
    @ApiModelProperty(value = "商户电话")
    private String businessPhone;
    @ApiModelProperty(value = "额外折扣")
    private BigDecimal extraDiscount;
    @ApiModelProperty(value = "额外折扣期限")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long extraDiscountPeriod;
    @ApiModelProperty(value = "其他实体")
    private String otherEntity;
    @ApiModelProperty(value = "商户文件")
    private String fileList;

    @ApiModelProperty(value = "信托文件")
    private String trusteeFileList;

    @ApiModelProperty(value = "额外占比时长选择")
    private Integer extraDiscountPeriodChoice;

    @ApiModelProperty(value = "单笔交易限额")
    private Integer singleTransactionLimit;

    @ApiModelProperty(value = "docusign返回envelopeId")
    private String docusignEnvelopeid;

    @ApiModelProperty(value = "是否签署docusign")
    private Integer docusignHasSigned;

    @ApiModelProperty(value = "docusign文件路径")
    private String docusignFiles;

    private List<JSONObject> files;

    private List<JSONObject> trusteeFiles;

    private List<JSONObject> docusignFileList;

    /**
     * 支付平台占比选择
     */
    private String payPlatFormChoice;

    /**
     * 支付平台让利用户选择
     */
    private String creditPlatFormChoice;

    @ApiModelProperty(value = "app折扣")
    private BigDecimal appChargeRate;

    @ApiModelProperty(value = "基础折扣")
    private BigDecimal baseRate;

    /**
     * app折扣选择
     */
    private String appChargeChoice;
    /**
     * app折扣选择
     */
    private String appChargePayChoice;

    /**
     * 支付渠道信息集合
     */
    @ApiModelProperty(value = "支付渠道信息集合")
    private List<RouteDTO> routeDTOList;

    private Integer rateType;

    @ApiModelProperty(value = "签署人")
    private String docusignSigner;

    @ApiModelProperty(value = "合同类型 0：全部 1：非全部")
    private Integer contractType ;

    @ApiModelProperty(value = "注册名和交易名是否相同 1：相同 0：不同")
    private String tradingIsNoRegistered ;

    @ApiModelProperty(value = "签署人标题")
    private Integer authorisedTitle;

    @ApiModelProperty(value = "合同开始时间")
    private Long contractStartTime;

    @ApiModelProperty(value = "合同结束时间")
    private Long contractEndTime;
    /**
     * 整体销售用户折扣
     */
    @ApiModelProperty(value = "整体销售用户折扣")
    private BigDecimal wholeSaleUserDiscount;
    /**
     * 整体销售用户折扣
     */
    @ApiModelProperty(value = "整体销售商户折扣")
    private BigDecimal wholeSaleMerchantDiscount;
    /**
     * 整体出售申请状态
     */
    @ApiModelProperty(value = "整体出售申请状态")
    private Integer wholeSaleApproveState;

    /**
     * 二级商户渠道信息表
     */
    @ApiModelProperty(value = "二级商户渠道信息表")
    private SecondMerchantGatewayInfoDTO secondMerchantGatewayInfoDTO;

    /**
     * 账户变更申请标志位 1：无审核提交/审核成功 3：审核中 4：审核拒绝
     */
    @ApiModelProperty(value = "账户变更申请标志位 1：无审核提交/审核成功 3：审核中 4：审核拒绝")
    private Integer accountApplyState;

    @ApiModelProperty(value = "商户审核通过时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long merchantApprovePassTime;
    /**
     * 整体出售支付用户折扣
     */
    @ApiModelProperty(value = "整体出售支付用户折扣")
    private BigDecimal wholeSaleUserPayDiscount;
    /**
     * 用户支付基础折扣
     */
    @ApiModelProperty(value = "用户支付基础折扣")
    private BigDecimal basePayRate;
    /**
     * 平台支付服务费
     */
    @ApiModelProperty(value = "平台支付服务费")
    private BigDecimal appChargePayRate;

    @ApiModelProperty(value = "tag 字符串")
    private List<String> tags ;
    @ApiModelProperty(value = "城市名英文")
    private String cityName ;

    @ApiModelProperty(value = "商户完整地址")
    private String fullAddress ;
    @ApiModelProperty(value = "详情页图片地址")
    private String detailPhotoUrl;
    @ApiModelProperty(value = "州显示名")
    private String stateNameStr;
    @ApiModelProperty(value = "详情照片list")
    private List<String> detailPhotoList;
    @ApiModelProperty(value = "卡支付折扣")
    private BigDecimal cardPayDiscount;
    @ApiModelProperty(value = "分期付支付折扣")
    private BigDecimal installmentDiscount;
    @ApiModelProperty(value = "实用名称")
    private String tradeName;
    @Tolerate
    public MerchantDTO(){};

    /**
     * 支付成功通知地址
     */
    @ApiModelProperty(value = "支付成功通知地址")
    private String paySuccessNotifyUrl;


    /**
     * 合同类型
     */
    @ApiModelProperty(value = "合同类型 默认 0 电子合同 Electronic Contract  1 纸质合同paper contract")
    private Integer docusignContractType;
}