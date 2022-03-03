package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

/**
 * @author xuchenglong
 * 导出excl实体类
 * @date 2021/4/8
 */
@ApiModel("")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycSubmitLogExcl {
    private String name;
    private String customerAccount;
    private String accountSubmittedTimes;
    private String referralCode;
    private String kycStatus;
    private String date;
}
