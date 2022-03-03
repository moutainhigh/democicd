package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.config.LongJsonSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description: 理财人工审核类
 * @author: baixinyue
 * @date: 2020/03/26
 */
@ApiModel("理财人工审核类")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestApproveDTO implements Serializable {

    /**
     * 记录id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long id;

    /**
     * 用户id
     */
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;

    /**
     * 电话
     */
    private String phone;

    /**
     * 审核申请日期、用户注册日期
     */
    @JsonSerialize(using = LongDateSerializer.class)
    private Long createdDate;

}
