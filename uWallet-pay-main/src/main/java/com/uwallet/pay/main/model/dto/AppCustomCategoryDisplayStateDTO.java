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
 * APP首页自定义分类 每个州展示商户、图片信息
 * </p>
 *
 * @description: APP首页自定义分类 每个州展示商户、图片信息
 * @author: zhangzeyuan
 * @date: Created in 2021-04-13 16:10:18
 */
@ApiModel("APP首页自定义分类 每个州展示商户、图片信息")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppCustomCategoryDisplayStateDTO extends BaseDTO implements Serializable {

    /**
     * 对应 u_app_custom_category display_order字段
     */
    @ApiModelProperty(value = "对应 u_app_custom_category display_order字段")
    private Integer displayOrder;

    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private Integer categoryType;


    /**
     * 澳大利亚州名
     */
    @ApiModelProperty(value = "澳大利亚州名")
    private String stateName;
    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称")
    private String categoryName;
    /**
     * 图片数量
     */
    @ApiModelProperty(value = "图片数量")
    private Integer imageTotal;
    /**
     * 图片JSON集合
     */
    @ApiModelProperty(value = "图片JSON集合")
    private String imagesJson;
    /**
     * 商户id ,拼接字符串
     */
    @ApiModelProperty(value = "商户id ,拼接字符串")
    private String merchantIds;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;


    /**
     * 商户展示类型 0 自定义商户 1 和哪个州一样
     */
    @ApiModelProperty(value = "商户展示类型 0 自定义商户 1 和哪个州一样")
    private Integer merchantDisplayType;
    /**
     * 商户展示方式 1 自定义 2 距离
     */
    @ApiModelProperty(value = "商户展示方式 1 自定义 2 距离")
    private Integer merchantShowType;
    /**
     * merchant_display_type为1时 州名
     */
    @ApiModelProperty(value = "merchant_display_type为1时 州名")
    private String merchantSameStateName;

    /**
     * 商户数组
     */
    @Transient
    private MerchantDTO[] merchantData;

    /**
     * 操作类型 1 view all 修改图片  2  修改商户信息
     */
    @Transient
    private Integer operateType;

    private List<MerchantAppHomePageDTO> merchantList;

    private List<AppCustomCategoryImageDTO> imageList;

}
