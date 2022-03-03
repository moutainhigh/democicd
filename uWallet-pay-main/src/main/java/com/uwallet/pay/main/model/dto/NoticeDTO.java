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
 * 消息表
 * </p>
 *
 * @description: 消息表
 * @author: baixinyue
 * @date: Created in 2019-12-13 17:55:07
 */
@ApiModel("消息表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NoticeDTO extends BaseDTO implements Serializable {

    /**
     * 用户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "用户id")
    private Long userId;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;
    /**
     * 0：未读 1：已读
     */
    @ApiModelProperty(value = "0：未读 1：已读")
    private Integer isRead;
    /**
     * 消息类型
     */
    @ApiModelProperty(value = "消息类型")
    private Integer type;

}
