package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * APP首页自定义分类 图片信息
 * </p>
 *
 * @description: APP首页自定义分类  图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 16:10:18
 */
@ApiModel("APP首页自定义分类 图片信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCustomCategoryImageDTO extends BaseDTO implements Serializable {

    /**
     * 对图片路径
     */
    @ApiModelProperty(value = "对图片路径")
    private String path;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题")
    private String title;
    /**
     * 副标题
     */
    @ApiModelProperty(value = "副标题")
    private String subTitle;

}
