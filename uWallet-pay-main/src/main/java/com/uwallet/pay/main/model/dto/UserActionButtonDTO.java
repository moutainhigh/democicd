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
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
/**
 * <p>
 * 用户冻结表存在该表的用户可以被冻结和解冻
 * </p>
 *
 * @description: 用户冻结表存在该表的用户可以被冻结和解冻
 * @author: xucl
 * @date: Created in 2021-09-10 09:35:21
 */
@ApiModel("用户冻结表存在该表的用户可以被冻结和解冻")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActionButtonDTO extends BaseDTO implements Serializable {

    /**
     * 用户id
     */
    @ApiModelProperty(value = "用户id")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 操作类型0账号冻结,1分期付冻结
     */
    @ApiModelProperty(value = "操作类型0账号冻结,1分期付冻结")
    private Integer type;



}
