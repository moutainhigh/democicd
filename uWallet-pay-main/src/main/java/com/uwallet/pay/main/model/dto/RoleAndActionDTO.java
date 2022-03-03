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
 * 角色表

 * </p>
 *
 * @description: 角色表

 * @author: Strong
 * @date: Created in 2019-09-16 17:34:46
 */
@ApiModel("角色表(关联查询)")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleAndActionDTO extends BaseDTO implements Serializable {

    /**
     * 权限名称
     */
    @ApiModelProperty(value = "权限名称")
    private String name;
}
