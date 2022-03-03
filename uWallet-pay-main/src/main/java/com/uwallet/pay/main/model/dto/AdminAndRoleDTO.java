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

/**
 * <p>
 * 管理员表和角色表关联查询
 * </p>
 *
 * @author: yangchao
 * @date: Created in 2019-09-17 10:37:20
 */

@ApiModel("管理员表和角色表关联查询")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminAndRoleDTO extends BaseDTO implements Serializable {

    /**
     * 流程组名
     */
    @ApiModelProperty(value = "流程组名")
    private Object name;
    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户名")
    private String userName;
    /**
     * 父子id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long parentId;
    /**
     * 角色名称
     */
    @ApiModelProperty(value = "角色名称")
    private String roleName;
    /**
     * 待审批
     */
    @ApiModelProperty(value = "待审批")
    private Object pendingNum;
    /**
     * 已审批
     */
    @ApiModelProperty(value = "已审批")
    private Object approvedNum;
    /**
     * 在岗状态
     */
    @ApiModelProperty(value = "在岗状态")
    private Integer isJobIng;
    /**
     * 在岗总人数
     */
    @ApiModelProperty(value = "在岗总人数")
    private Integer isJobIngCount;
}
