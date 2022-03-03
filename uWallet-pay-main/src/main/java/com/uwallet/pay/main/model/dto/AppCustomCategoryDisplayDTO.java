package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * APP首页自定义分类展示信息
 * </p>
 *
 * @description: APP首页自定义分类展示信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 15:09:11
 */
@ApiModel("APP首页自定义分类展示信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCustomCategoryDisplayDTO extends BaseDTO implements Serializable {

    /**
     * 展示在APP的位置（app从上到下 1 2 3 4 5，不重复，支持上下移动）
     */
    @ApiModelProperty(value = "展示在APP的位置（app从上到下 1 2 3 4 5，不重复，支持上下移动）")
    private Integer displayOrder;
    /**
     * 对应的种类（值对应u_static_data value?）
     */
    @ApiModelProperty(value = "对应的种类（值对应u_static_data value?）")
    private Integer categoryType;
    /**
     * 种类名称
     */
    @ApiModelProperty(value = "种类名称")
    private String categoryName;
    /**
     * APP显示的描述信息
     */
    @ApiModelProperty(value = "APP显示的描述信息")
    private String description;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;


    /**
     * 商户ID集合
     */
    @Transient
    private String merchantIds;

    /**
     * 商户数据
     */
    @Transient
    private List<MerchantAppHomePageDTO> merchants;


}
