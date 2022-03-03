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
 * @author: liming
 * @Date: 2019/9/9 19:06
 * @Description: 管理员关联借款记录表
 */

@ApiModel("管理员账户")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminWithBorrowVerifyCountDTO extends BaseDTO implements Serializable {
    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;
    /**
     * 真实姓名
     */
    @ApiModelProperty(value = "真实姓名")
    private String realName;
    /**
     * 密码sha1
     */
    @ApiModelProperty(value = "密码sha1")
    private String password;
    /**
     * 公司位置
     */
    @ApiModelProperty(value = "公司位置")
    private String address;
    /**
     * 公司名称
     */
    @ApiModelProperty(value = "公司名称")
    private String company;
    /**
     * 干扰码
     */
    @ApiModelProperty(value = "干扰码")
    private Integer salt;
    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String phone;
    /**
     * 本次登录时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "本次登录时间")
    private Long loginTime;
    /**
     * 最后登录时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "最后登录时间")
    private Long lastLoginTime;
    /**
     * 本次登录ip
     */
    @ApiModelProperty(value = "本次登录ip")
    private String loginIp;
    /**
     * 最后登录IP
     */
    @ApiModelProperty(value = "最后登录IP")
    private String lastLoginIp;
    /**
     * 登录次数
     */
    @ApiModelProperty(value = "登录次数")
    private Integer loginTimes;
    /**
     * 密码错误次数
     */
    @ApiModelProperty(value = "密码错误次数")
    private Integer errorTimes;
    /**
     * 在职状态：0.离职 1.在职
     */
    @ApiModelProperty(value = "在职状态：0.离职 1.在职")
    private Integer state;
    /**
     * 直属上级id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "直属上级id")
    private Long parentId;
    /**
     * 允许登录IP
     */
    @ApiModelProperty(value = "允许登录IP")
    private String allowLoginIp;
    /**
     * 编号
     */
    @ApiModelProperty(value = "编号")
    private String code;
    /**
     * 在岗状态：0.离岗 1.在岗
     */
    @ApiModelProperty(value = "在岗状态：0.离岗 1.在岗")
    private Integer isJobIng;
    /**
     * 待审批订单数
     */
    private Integer awaitApprovalCount;
    /**
     * 已审批订单数
     */
    private Integer alreadyApprovalCount;
    /**
     * 语言版本：zh-CN中文，en-US英文
     */
    @ApiModelProperty(value = "语言版本：zh-CN中文，en-US英文")
    private String language;

}
