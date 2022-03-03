package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uwallet.pay.core.config.LongDateSerializer;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户整合列表实体
 */
@ApiModel("用户整合列表实体")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserListDTO extends BaseDTO implements Serializable {
    private String userFirstName;
    private String userLastName;
    private String phone;
    private Integer idType;
    private String email;
//    @JsonSerialize(using = LongDateSerializer.class)
    private String birth;
    private BigDecimal creditAmount;
    private BigDecimal availableCredit;
    private BigDecimal useAmount;
    private Integer payState;
    private Integer installmentState;
    private Integer paymentState;
    private Integer userRepayFailTimes;
    private Integer invitationToRegister;
    private String phoneModel;
    private String version;
    private Integer phoneSystem;
    private String mobileModel;
}
