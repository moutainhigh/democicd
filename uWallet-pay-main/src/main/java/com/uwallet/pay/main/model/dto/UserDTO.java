package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Tolerate;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 用户主表
 * </p>
 *
 * @description: 用户主表
 * @author: baixinyue
 * @date: Created in 2019-12-13 15:11:14
 */
@ApiModel("用户主表")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO extends BaseDTO implements Serializable {

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
    private String pinNumber;
    /**
     *
     */
    @ApiModelProperty(value = "")
    private String payPassword;
    /**
     * 支付业务状态 0：不可用 1：可用
     */
    @ApiModelProperty(value = "支付业务状态 0：不可用 1：可用")
    private Integer paymentState;
    /**
     * 分期付业务状态 0：不可用 1：可用 2：禁用
     */
    @ApiModelProperty(value = "分期付业务状态 0：不可用 1：可用 2：禁用")
    private Integer installmentState;
    /**
     * 理财业务状态 0：不可用 1：可用
     */
    @ApiModelProperty(value = "理财业务状态 0：不可用 1：可用")
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
    private Long merchantId;

    /**
     * 角色
     */
    @ApiModelProperty(value = "角色")
    private Integer role;

    /**
     * 二维码code
     */
    @ApiModelProperty(value = "二维码code")
    private String code;

    /**
     * 验证码
     */
    @ApiModelProperty(value = "验证码")
    private String securityCode;

    /**
     * 旧登陆密码
     */
    @ApiModelProperty(value = "旧登陆密码")
    private String oldPassword;
    /**
     * 新登陆密码
     */
    @ApiModelProperty(value = "新登陆密码")
    private String newPassword;
    /**
     * 确认新登陆密码
     */
    @ApiModelProperty(value = "确认新登陆密码")
    private String confirmPassword;
    /**
     * 用户名
     */
    private String fullName;
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
     * 州
     */
    @ApiModelProperty(value = "州")
    private String userState;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private String userCity;
    /**
     * 推送token
     */
    @ApiModelProperty(value = "推送token")
    private String pushToken;
    /**
     * 设备号
     */
    @ApiModelProperty(value = "设备号")
    private String imeiNo;
    /**
     * 是否同意理财
     * 0:否 1：同意
     */
    @ApiModelProperty(value = "0:否 1：同意")
    private Integer isInvestAgree;

    @ApiModelProperty(value = "是否同意分期付：0:否 1：同意")
    private Integer isCreditAgree;
    /**
     * 名
     */
    private String userFirstName;
    /**
     * 姓
     */
    private String userLastName;
    /**
     * 生日
     */
    private String birth;
    /**
     * 用户来源
     */
    private String channel;

    @ApiModelProperty(value = "邀请码")
    private String inviteCode;

    @ApiModelProperty(value = "输入邀请码")
    private String enterInviteCode;

    @ApiModelProperty(value = "邀请人")
    private Long inviterId;

    @ApiModelProperty(value = "邀请注册次数")
    private Integer invitationToRegister;

    @ApiModelProperty(value = "邀请消费")
    private Integer inviteConsumption;

    @ApiModelProperty(value = "预计红包")
    private BigDecimal expectAmount;

    @ApiModelProperty(value = "实的红包")
    private BigDecimal actualAmount;
    /**
     * split补充还款账户信息标志位 0：不需要 1：需要
     */
    @ApiModelProperty(value = "split补充还款账户信息标志位 0：不需要 1：需要")
    private Integer splitAddInfoState;
    /**
     * 卡支付绑卡状态
     */
    @ApiModelProperty(value = "卡支付绑卡状态 0：没绑卡 1：绑卡")
    private Integer cardState;

    /**
     * 首次消费标识
     */
    @ApiModelProperty(value = "首次消费标识")
    private Integer firstDealState;

    @Tolerate
    public UserDTO(){};
    /**
     * 邮编
     */
    private String postcode;

    /**
     * 分期付绑卡状态 0：新用户未绑卡 1：已绑卡 2：老用户未绑卡'
     */
    private Integer creditCardState;

    @ApiModelProperty(value = "医保卡号")
    private String medicare;
    @ApiModelProperty(value = "护照号码")
    private String passport;
    @ApiModelProperty(value = "驾驶证号码")
    private String driverLicence;
    /**
     * 是否H5 注册用户
     * */
    private Boolean isH5;
    /**
     * 用户中间名
     * */
    private String userMiddleName;
    @ApiModelProperty(value = "注册来源,0app，1h5")

    private Integer registerFrom;

    /**
     * 性别
     * */
    private Integer sex;


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
    @ApiModelProperty(value = "手机型号 如vivonex")
    private String mobileModel;

    /**
     * 红包余额
     * */
    @Transient
    private BigDecimal balance;


    private Long subAccountId;
    private Long accountId;


    /**
     * 拆分红包状态  1 已拆分 0 未拆分
     * */
    private Integer splitRedEnvelopeState;

    private String appVersionStr;

    /**
     * stripe老用户标识 0 ：否 1：是
     */
    @ApiModelProperty(value = "stripe老用户标识 0 ：否 1：是")
    private Integer stripeState;
}
