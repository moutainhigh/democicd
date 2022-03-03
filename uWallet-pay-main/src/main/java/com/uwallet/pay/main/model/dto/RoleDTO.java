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
 * 角色表

 * </p>
 *
 * @description: 角色表

 * @author: Strong
 * @date: Created in 2019-09-16 17:34:46
 */
@ApiModel("角色表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleDTO extends BaseDTO implements Serializable {

    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    private String name;
    /**
     * 别名
     */
    @ApiModelProperty(value = "别名")
    private String remarkName;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 0：禁用 1：可用
     */
    @ApiModelProperty(value = "0：禁用 1：可用")
    private Integer stats;
    /**
     * RoleAction集合
     */
    @ApiModelProperty(value = "RoleAction集合")
    private List<RoleActionDTO> roleActionDTO;
    /**
     * 权限id
     */
    @ApiModelProperty(value = "权限id")
    private List<Long> actions;
}
