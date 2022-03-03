package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.uwallet.pay.core.model.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@ApiModel("用户整合列表实体")
@Builder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequestDataDTO extends BaseDTO implements Serializable {
    private String phone;
    private String userName;
    private Integer payType;
    private Integer userType;
    private Integer idType;
    private Integer useState;
    private Integer state;
    private Integer city;
    private Long start;
    private Long end;
    private Long birthEnd;
    private Long birthStart;
}
