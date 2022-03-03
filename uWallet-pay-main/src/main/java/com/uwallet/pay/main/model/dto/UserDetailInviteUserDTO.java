package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.math.BigDecimal;

/**
 * <p>
 * 用户详情-推荐用户DTO
 * </p>
 *
 * @description: 推荐用户DTO
 * @author: zhangzeyuan
 * @date: Created in 2021年9月22日13:59:34
 */
@ApiModel("用户详情推荐用户DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailInviteUserDTO implements Serializable {

    /**
     * id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 名字
     */
    @ApiModelProperty(value = "名字")
    private String firstName;

    /**
     * 姓氏
     */
    @ApiModelProperty(value = "姓氏")
    private String lastName;

    /**
     *  性别 1：女 2：男
     */
    @ApiModelProperty(value = "性别")
    private Integer sex;


    /**
     * 证件类型 1 驾照 2 护照 3 医保卡
     */
    @ApiModelProperty(value = "证件类型")
    private Integer idType;


    /**
     * 证件号码
     */
    @ApiModelProperty(value = "证件号码")
    private String idNumber;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;


    /**
     * 生日
     */
    @ApiModelProperty(value = "生日")
    private String birth;


    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 注册时间
     */
    @ApiModelProperty(value = "注册时间")
    private String registrationTime;

    /**
     *街道号
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
     * 洲
     */
    @ApiModelProperty(value = "洲")
    private String state;


    /**
     * 邮编
     */
    @ApiModelProperty(value = "邮编")
    private String postcode;

    @ApiModelProperty(value = "交易时间")
    @JsonSerialize(using = LongDateSerializer.class)
    private Long payTime;

    @ApiModelProperty(value = "交易金额")
    private BigDecimal payAmount;

}
