package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @description: 权限表
 * @author: Strong
 * @date: Created in 2019-09-16 17:55:12
 */
@ApiModel("权限表")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FirebaseDTO implements Serializable {

    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    @ApiModelProperty(value = "分期付订单id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long borrowId;

    /**
     * 渠道名字，也是APP的名字
     */
    @ApiModelProperty(value = "渠道名字，也是APP的名字")
    private String appName;
    /**
     * 设备的token值
     */
    @ApiModelProperty(value = "设备的token值")
    private String token;
    /**
     * 通知消息题目
     */
    @ApiModelProperty(value = "通知消息题目")
    private String title;
    /**
     * 通知消息内容
     */
    @ApiModelProperty(value = "通知消息内容")
    private String body;
    /**
     * 设备的token集合
     */
    @ApiModelProperty(value = "设备的token集合")
    private List<String> tokens;
    /**
     * 主题名字
     */
    @ApiModelProperty(value = "主题名字")
    private String topic;

    @ApiModelProperty(value = "是否语音播报 0: 否 1:是")
    private Integer voice;

    @ApiModelProperty(value = "路径跳转 0:不跳 1、还款页面 2、主页面 ")
    private Integer route;

}
