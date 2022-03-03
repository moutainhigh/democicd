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
import java.math.BigDecimal;

/**
 * <p>
 * Tag数据
 * </p>
 *
 * @description: Tag数据
 * @author: aaronS
 * @date: Created in 2021-01-07 11:19:48
 */
@ApiModel("Tag数据")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagDTO extends BaseDTO implements Serializable {

    /**
     *
     */
    @ApiModelProperty(value = "")
    @JsonSerialize(using = LongJsonSerializer.class)
    private Long parentId;
    /**
     * tag中文名
     */
    @ApiModelProperty(value = "tag中文名")
    private String cnName;
    /**
     * tag英文名
     */
    @ApiModelProperty(value = "tag英文名")
    private String enValue;
    /**
     * 搜索计数
     */
    @ApiModelProperty(value = "搜索计数")
    private BigDecimal popular;
    /**
     * 是否展示: 1.展示 0.不展示
     */
    @ApiModelProperty(value = "是否展示: 1.展示 0.不展示")
    private Integer showState;

}
