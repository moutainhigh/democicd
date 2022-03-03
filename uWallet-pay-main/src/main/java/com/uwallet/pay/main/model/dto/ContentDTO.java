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
 * 广告表
 * </p>
 *
 * @description: 广告表
 * @author: Strong
 * @date: Created in 2020-01-14 11:10:06
 */
@ApiModel("广告表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentDTO extends BaseDTO implements Serializable {

    /**
     * 广告标题
     */
    @ApiModelProperty(value = "广告标题")
    private String title;
    /**
     * 内容描述
     */
    @ApiModelProperty(value = "内容描述")
    private String description;
    /**
     * 跳转地址
     */
    @ApiModelProperty(value = "跳转地址")
    private String path;

}
