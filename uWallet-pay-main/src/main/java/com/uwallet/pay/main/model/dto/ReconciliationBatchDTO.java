package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.Tolerate;

import java.io.Serializable;
/**
 * <p>
 * 对账表
 * </p>
 *
 * @description: 对账表
 * @author: aaronS
 * @date: Created in 2021-01-25 16:11:20
 */
@ApiModel("对账表")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReconciliationBatchDTO extends BaseDTO implements Serializable {

    /**
     * 三方对账文件名
     */
    @ApiModelProperty(value = "三方对账文件名")
    private String thirdFileName;
    /**
     * 文件路径
     */
    @ApiModelProperty(value = "文件路径")
    private String path;
    /**
     * 存储文件名
     */
    @ApiModelProperty(value = "存储文件名")
    private String fileName;
    /**
     * 通道类型：
     */
    @ApiModelProperty(value = "通道类型：")
    private Integer type;
    /**
     * 交易类型: 0、支付 1、退款
     *
     * 2021/01/25: 商定 默认0
     */
    @ApiModelProperty(value = "交易类型: 0、支付 1、退款")
    private Integer transactionType;
    /**
     * 对账文件总条数
     */
    @ApiModelProperty(value = "对账文件总条数")
    private Integer totalNumber;
    /**
     * 失败条数
     */
    @ApiModelProperty(value = "失败条数")
    private Integer failNumber;
    /**
     * 对账状态：0:处理中， 1：成功， 2：处理失败
     */
    @ApiModelProperty(value = "对账状态：0:处理中， 1：成功， 2：处理失败")
    private Integer state;

    @Tolerate
    public ReconciliationBatchDTO(){};

}
