package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 数据字典
 * </p>
 *
 * @description: 数据字典
 * @author: Strong
 * @date: Created in 2019-12-13 15:35:58
 */
@ApiModel("数据字典")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StaticDataDTO extends BaseDTO implements Serializable {

    /**
     * 键
     */
    @ApiModelProperty(value = "键")
    private String code;
    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;
    /**
     * 英文名称
     */
    @ApiModelProperty(value = "英文名称")
    private String enName;
    /**
     * 值
     */
    @ApiModelProperty(value = "值")
    private String value;
    /**
     * 父级
     */
    @ApiModelProperty(value = "父级")
    private String parent;
    /**
     * 是否内置属性; 0 ：否 1：是
     */
    @ApiModelProperty(value = "是否内置属性; 0 ：否 1：是")
    private Integer builtin;
    /**
     * 子节点
     */
    @ApiModelProperty(value = "子节点")
    private List<StaticDataDTO> staticDataDTOList;

}
