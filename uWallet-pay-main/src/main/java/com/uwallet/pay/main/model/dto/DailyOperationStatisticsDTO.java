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


@ApiModel("每日运营数据统计")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyOperationStatisticsDTO extends BaseDTO implements Serializable {

    /**
     * 广告标题
     */
    @ApiModelProperty(value = "广告标题")
    private String title;
    /**
     * 广告副标题
     */
    @ApiModelProperty(value = "广告副标题")
    private String subtitle;
    /**
     * 形式，0：表示弹窗popup，1：嵌入embeded
     */
    @ApiModelProperty(value = "形式，0：表示弹窗popup，1：嵌入embeded")
    private Integer form;
    /**
     * 广告位，0表示首页
     */
    @ApiModelProperty(value = "广告位，0表示首页")
    private Integer position;
    /**
     * 内容描述
     */
    @ApiModelProperty(value = "内容描述")
    private String description;
    /**
     * 是否自动关闭，0表示不自动关闭，>0则表示多少秒之后自动关闭
     */
    @ApiModelProperty(value = "是否自动关闭，0表示不自动关闭，>0则表示多少秒之后自动关闭")
    private Integer autoclose;
    /**
     * 优先级
     */
    @ApiModelProperty(value = "优先级")
    private Integer priority;
    /**
     * 图片地址
     */
    @ApiModelProperty(value = "图片地址")
    private String path;
    /**
     * 外链地址
     */
    @ApiModelProperty(value = "外链地址")
    private String link;
    /**
     * 发布时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "发布时间")
    private Long start;
    /**
     * 截止时间
     */
    @JsonSerialize(using = LongDateSerializer.class)
    @ApiModelProperty(value = "截止时间")
    private Long end;
    /**
     * 1表示上线状态，0表示下线状态
     */
    @ApiModelProperty(value = "1表示上线状态，0表示下线状态")
    private Integer state;

}
