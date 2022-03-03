package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
/**
 * <p>
 * 邮件发送记录表
 * </p>
 *
 * @description: 邮件发送记录表
 * @author: zhoutt
 * @date: Created in 2020-01-07 15:46:48
 */
@ApiModel("邮件发送记录表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailLogDTO extends BaseDTO implements Serializable {

    /**
     * 收件人地址
     */
    @ApiModelProperty(value = "收件人地址")
    private String address;
    /**
     * 邮件内容
     */
    @ApiModelProperty(value = "邮件内容")
    private String content;
    /**
     * 发送方式  0自动发送   1手动发送
     */
    @ApiModelProperty(value = "发送方式  0自动发送   1手动发送")
    private Integer sendType;

}
