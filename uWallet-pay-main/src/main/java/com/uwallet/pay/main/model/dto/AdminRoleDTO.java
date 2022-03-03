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
 * @author: liming
 * @Date: 2019/9/9 19:06
 * @Description: 管理员关联借款记录表
 */

@ApiModel("管理员账户")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminRoleDTO extends BaseDTO implements Serializable {

    /**
     * 管理员id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "管理员id")
    private Long adminId;
    /**
     * 角色id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "角色id")
    private Long roleId;
    /**
     * Role集合
     */
    @ApiModelProperty(value = "备注")
    private List<RoleDTO> roleDTO;
}