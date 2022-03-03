package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 电子账户绑卡
 * </p>
 *
 * @description: 电子账户绑卡
 * @author: baixinyue
 * @date: Created in 2019-12-13 15:26:23
 */
@ApiModel("电子账户绑卡")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDTO   implements Serializable {
    private Long id;

    /**
     * 卡号/账户号
     */
    @ApiModelProperty(value = "卡号/账户号")
    private String cardNo;
    /**
     * 账户名
     */
    @ApiModelProperty(value = "账户名")
    private String accountName;
    /**
     * 绑定类型 0：卡 1：账户
     */
    @ApiModelProperty(value = "绑定类型 0：卡 1：账户")
    private Integer type;
    /**
     * 账户表ID
     */
    @ApiModelProperty(value = "账户表ID")
    private Long accountId;
    /**
     * 子账户类型（0：钱包余额子户，1：理财余额子户）
     */
    @ApiModelProperty(value = "子账户类型（0：钱包余额子户，1：理财余额子户）")
    private Integer subAccountType;
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;
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
     * email
     */
    @ApiModelProperty(value = "email")
    private String email;
    /**
     * 持卡人注册卡时，使用的注册地址-第一行
     */
    @ApiModelProperty(value = "持卡人注册卡时，使用的注册地址-第一行")
    private String address1;
    /**
     * 持卡人注册卡时，使用的注册地址-第二行
     */
    @ApiModelProperty(value = "持卡人注册卡时，使用的注册地址-第二行")
    private String address2;
    /**
     * 注册地址的对应城市名
     */
    @ApiModelProperty(value = "注册地址的对应城市名")
    private String city;
    /**
     * 持卡人注册卡时使用的注册地址的对应国家名，**请使用2位的ISO标准对应码
     */
    @ApiModelProperty(value = "持卡人注册卡时使用的注册地址的对应国家名，**请使用2位的ISO标准对应码")
    private String country;
    /**
     * 持卡人注册卡时使用的注册地址的对应州名（如果有）
     */
    @ApiModelProperty(value = "持卡人注册卡时使用的注册地址的对应州名（如果有）")
    private String state;
    /**
     * 持卡人注册卡时使用的注册地址的对应邮编
     */
    @ApiModelProperty(value = "持卡人注册卡时使用的注册地址的对应邮编")
    private String zip;
    /**
     * 卡过期月份（mm）
     */
    @ApiModelProperty(value = "卡过期月份（mm）")
    private String customerCcExpmo;
    /**
     * 卡过期年份（yyyy）
     */
    @ApiModelProperty(value = "卡过期年份（yyyy）")
    private String customerCcExpyr;
    /**
     * 卡类型，允许的值：10、VISA, 20、MAST, 30、 SWITCH,  40、SOLO,  50、DELTA, 60、 AMEX,
     */
    @ApiModelProperty(value = "卡类型，允许的值：10、VISA, 20、MAST, 30、 SWITCH,  40、SOLO,  50、DELTA, 60、 AMEX, ")
    private String customerCcType;
    /**
     * 银行卡背面的安全码（CVN/CVV2）
     */
    @ApiModelProperty(value = "银行卡背面的安全码（CVN/CVV2）")
    private String customerCcCvc;
    /**
     * 由LPS SCSS系统颁发的卡详细信息唯一标识符
     */
    @ApiModelProperty(value = "由LPS SCSS系统颁发的卡详细信息唯一标识符")
    private String crdStrgToken;
    /**
     * BSB
     */
    @ApiModelProperty(value = "BSB")
    private String bsb;
    /**
     * 开户行
     */
    @ApiModelProperty(value = "开户行")
    private String bankName;
    /**
     * 解绑原因
     */
    @ApiModelProperty(value = "解绑原因")
    private String unbindReason;
    /**
     * 解绑时间
     */
    @ApiModelProperty(value = "解绑时间")
    private Long unbindTime;

    private Integer cardCategory;


    /**
     * integrapay唯一id
     */
    private String uniqueReference;

    /**
     * 默认卡
     */
    private Integer preset;
    /**
     * 卡顺序
     */
    private Integer order;


    /**
     * 创建时间，存储时间戳。
     * 获取示例：<code>System.currentTimeMillis()</code>
     */
    private String createdDate;

    private Long createdDateLongValue;

    private String stripeToken;

    /**
     * 是否已经支付成功过 0：否 1：是
     */
    @ApiModelProperty(value = "是否已经支付成功过 0：否 1：是")
    private Integer payState;

}
