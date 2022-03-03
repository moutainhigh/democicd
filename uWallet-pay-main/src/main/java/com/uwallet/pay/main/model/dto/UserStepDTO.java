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
 * 用户权限阶段
 * </p>
 *
 * @description: 用户权限阶段
 * @author: baixinyue
 * @date: Created in 2020-06-30 16:51:35
 */
@ApiModel("用户权限阶段")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserStepDTO extends BaseDTO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 步骤：1：kyc 2：illion 3：分期付风控
     */
    @ApiModelProperty(value = "步骤：1：kyc 2：illion 3：分期付风控")
    private Integer step;
    /**
     * 0: 未开始 1：成功 2：失败 3：进行中 5 failed
     */
    @ApiModelProperty(value = "0: 未开始 1：成功 2：失败 3：进行中")
    private Integer stepState;

}
