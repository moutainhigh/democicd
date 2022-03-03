package com.uwallet.pay.main.model.excel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author xuchenglong
 * Illion记录excl类
 * @date 2021/4/15
 */
@ApiModel("Illion记录excl类")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IllionLogExcel implements Serializable {
    private String name;
    private String customerAccount;
    private Integer accountsSubmittedTimes;
    private String bank;
    private String referralCode;
    private String bankConnectedStatusAndErrorMessage;
    private String reportStatus;
    private String date;
}
