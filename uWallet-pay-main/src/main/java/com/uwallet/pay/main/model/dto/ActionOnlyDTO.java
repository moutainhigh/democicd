package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 权限表
 * </p>
 *
 * @description: 权限表
 * @author: Rainc
 * @date: Created in 2019-09-02 16:04:04
 */
@ApiModel("权限表")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionOnlyDTO implements Serializable {

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;
    /**
     * 图标
     */
    @ApiModelProperty(value = "图标")
    private String icon;
    /**
     * 权限类型 1：菜单  2：动作
     */
    @ApiModelProperty(value = "权限类型 1：菜单  2：动作")
    private Integer type;

    /**
     * 子权限
     */
    private List<ActionOnlyDTO> children;
    /**
     * admin标识
     */
    @ApiModelProperty(value = "admin标识")
    private String authority;
    /**
     * 权限
     */
    @ApiModelProperty(value = "权限")
    private String flag;
    /**
     * hideChildrenInMenu标识
     */
    @ApiModelProperty(value = "hideChildrenInMenu标识")
    private Integer hideChildrenInMenu;
    /**
     * hideInMenu标识
     */
    @ApiModelProperty(value = "hideInMenu标识")
    private Integer hideInMenu;
    /**
     * 父级ID(前端只能接收这种格式)
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "父级ID")
    private Long parent_id;
    /**
     * 权限(前端只能接收这种格式)
     */
    @ApiModelProperty(value = "前端只能接收这种格式")
    private String identification;
    /**
     * 前台路由地址(前端只能接收这种格式)
     */
    @ApiModelProperty(value = "前台路由地址")
    private String path;
    /**
     * menu_id(前端只能接收这种格式id)
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    @ApiModelProperty(value = "menu_id")
    private Long menu_id;
    /**
     * 页面显示用的名
     */
    @ApiModelProperty(value = "页面显示用的名")
    private String title;

    @ApiModelProperty(value = "是否展开")
    private Boolean expand;

    @ApiModelProperty(value = "是否选中")
    private Boolean checked;

}
