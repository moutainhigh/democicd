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
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 账户动账交易流水表
 * </p>
 *
 * @description: 账户动账交易流水表
 * @author: baixinyue
 * @date: Created in 2020-11-09 15:30:03
 */
@ApiModel("账户动账交易流水表")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketingFlowDTO extends BaseDTO implements Serializable {

    /**
     * 上层流水(交易流水）
     */
    @ApiModelProperty(value = "上层流水(交易流水）")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long flowId;
    /**
     * 
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long userId;
    /**
     * 交易金额
     */
    @ApiModelProperty(value = "交易金额")
    private BigDecimal amount;
    /**
     * 方向 0：红包入账 ，1：红包出账
     */
    @ApiModelProperty(value = "方向 0：红包入账 ，1：红包出账")
    private Integer direction;
    /**
     * 券码
     */
    @ApiModelProperty(value = "券码")
    private String code;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String description;
    /**
     * 交易类型 19：注册红包 20：推荐红包 21：红包消费 25：营销红包
     */
    @ApiModelProperty(value = "交易类型 19：注册红包 20：推荐红包 21：红包消费 25：营销红包")
    private Integer transType;
    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    private String returnMessage;
    /**
     * 错误码
     */
    @ApiModelProperty(value = "错误码")
    private String returnCode;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 
     */
    @ApiModelProperty(value = "交易状态：0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑 ")
    private Integer state;

    private String transTime;


    /**
     * 券ID
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long marketingId;


    /**
     * 营销管理规则 ID
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long marketingManageId;


    /**
     * 电话
     */
    @Transient
    private String phone;
}
