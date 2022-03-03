package com.uwallet.pay.main.model.dto;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
/**
 * <p>
 * 用户信息修改记录表
 * </p>
 *
 * @description: 用户信息修改记录表
 * @author: xucl
 * @date: Created in 2021-09-10 16:55:37
 */
@ApiModel("用户信息修改记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoUpdateLogDTO extends BaseDTO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 修改字段
     */
    @ApiModelProperty(value = "修改字段")
    /** 州1  城市2 地址3 etc4 邮编5 手机6 邮箱7 冻结分期付8 冻结账户9  解结账户10  解冻分期付11*/
    private String updateId;
    /**
     * 修改数据
     */
    @ApiModelProperty(value = "修改数据")
    private String updateText;
    /**
     * 修改备注
     */
    @ApiModelProperty(value = "修改备注")
    private String remarks;
    /**
     * 提交JSON数据
     */
    @ApiModelProperty(value = "提交JSON数据")
    private String data;

    private String creatBy;

    private String phone;

    private String userName;

}
