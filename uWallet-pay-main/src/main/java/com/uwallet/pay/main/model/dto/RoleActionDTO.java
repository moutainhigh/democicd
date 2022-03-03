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
import java.util.List;

/**
 * <p>
 * 角色-权限关系表
 * </p>
 *
 * @description: 角色-权限关系表
 * @author: Strong
 * @date: Created in 2019-09-16 17:51:57
 */
@ApiModel("角色-权限关系表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleActionDTO extends BaseDTO implements Serializable {

    /**
     * 角色id
     */
    @ApiModelProperty(value = "角色id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long roleId;
    /**
     * 权限id
     */
    @ApiModelProperty(value = "权限id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long actionId;
    /**
     * 权限集合
     */
    @ApiModelProperty(value = "备注")
    private List<ActionOnlyDTO> actionDTO;

}
