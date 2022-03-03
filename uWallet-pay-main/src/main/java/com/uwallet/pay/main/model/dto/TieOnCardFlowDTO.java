package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
 * 绑卡交易流水表
 * </p>
 *
 * @description: 绑卡交易流水表
 * @author: baixinyue
 * @date: Created in 2020-01-06 16:28:59
 */
@ApiModel("绑卡交易流水表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TieOnCardFlowDTO extends BaseDTO implements Serializable {

    /**
     *
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 通道id ，与pay_channel表通道对应
     */
    @ApiModelProperty(value = "通道id ，与pay_channel表通道对应")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long channelId;
    /**
     * 卡号/账户号
     */
    @ApiModelProperty(value = "卡号/账户号")
    private String cardNo;
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
     * integrapay唯一id
     */
    @ApiModelProperty(value = "integrapay唯一id")
    private String uniqueReference;
    /**
     * integraPay payerId
     */
    @ApiModelProperty(value = "integraPay payerId")
    private String payerId;
    /**
     * integraPay accountId
     */
    @ApiModelProperty(value = "integraPay accountId")
    private String integraPayAccountId;
    /**
     * 解绑卡id
     */
    @ApiModelProperty(value = "解绑卡id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long cardId;
    /**
     * 账户名
     */
    @ApiModelProperty(value = "账户名")
    private String accountName;
    /**
     * 名字
     */
    @ApiModelProperty(value = "名字")
    private String name;
    /**
     * bsb
     */
    @ApiModelProperty(value = "bsb")
    private String bsb;
    /**
     * split返回联系人id
     */
    @ApiModelProperty(value = "split返回联系人id")
    private String splitContactId;
    /**
     * split返回协议号
     */
    @ApiModelProperty(value = "split返回协议号")
    private String splitAgreementId;
    /**
     * 解绑卡状态，0：解绑中， 1：解绑成功，2：三方解绑失败，3：账户解绑失败
     */
    @ApiModelProperty(value = "解绑卡状态，0：解绑中， 1：解绑成功，2：三方解绑失败，3：账户解绑失败")
    private Integer unBundlingState;
    /**
     * 解绑原因
     */
    @ApiModelProperty(value = "解绑原因")
    private String unBundlingReason;
    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    private String errorMessage;
    /**
     * 错误码
     */
    @ApiModelProperty(value = "错误码")
    private String errorCode;

}
