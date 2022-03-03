package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@ApiModel("分期付更新清算状态请求一条商户数据DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneMerchantClearDataDTO {

    /**
     * 清算流水号
     */
    private Long clearFlow;
    /**
     * 清算商户编号
     */
    private Long merchantId;
    /**
     * 清算明细列表
     */
    private List<UpdateClearStateListDTO> borrowList ;
}
