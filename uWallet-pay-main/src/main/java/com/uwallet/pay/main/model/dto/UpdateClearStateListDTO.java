package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@ApiModel("分期付更新清算状态列表数据")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateClearStateListDTO {
    /**
     * 分期付订单号
     */
    private Long tripartiteTransactionNo;

}
