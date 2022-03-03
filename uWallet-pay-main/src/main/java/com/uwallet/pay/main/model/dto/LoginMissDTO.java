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
 * 登陆错误次数记录表
 * </p>
 *
 * @description: 登陆错误次数记录表
 * @author: baixinyue
 * @date: Created in 2020-01-02 13:56:13
 */
@ApiModel("登陆错误次数记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginMissDTO extends BaseDTO implements Serializable {

    /**
     * 用户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /**
     * 机会
     */
    @ApiModelProperty(value = "机会")
    private Integer chance;
    /**
     * 最后错误时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "最后错误时间")
    private Long lastErrorTime;

}
