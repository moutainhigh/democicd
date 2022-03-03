package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 管理员表
 * </p>
 *
 * @description: 管理员表
 * @author: Rainc
 * @date: Created in 2019-09-02 16:14:08
 */
@ApiModel("管理员表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminOnlyDTO extends BaseDTO implements Serializable {

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
     * 密码（sha1加密）
     */
    @ApiModelProperty(value = "密码（sha1加密）")
    private String password;
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
     * 本次登录IP
     */
    @ApiModelProperty(value = "本次登录IP")
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
     * 在职状态：0.离职 1.在职
     */
    @ApiModelProperty(value = "在职状态：0.离职 1.在职")
    private Integer state;
    /**
     * AdminRole集合
     */
    @ApiModelProperty(value = "备注")
    private List<AdminRoleDTO> adminRoleDTO;
    /**
     * 角色ids
     */
    @ApiModelProperty(value = "角色ids")
    private List<String> ids;
    /**
     * 角色roleIds
     */
    @ApiModelProperty(value = "角色roleIds")
    private List<String> roleIds;
    /**
     * 角色名
     */
    @ApiModelProperty(value = "角色名")
    private List<String> names;
    /**
     * 在岗状态：0.离岗 1.在岗
     */
    @ApiModelProperty(value = "在岗状态：0.离岗 1.在岗")
    private Integer isJobIng;
}
