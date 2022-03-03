package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * <p>
 * 捐赠流水
 * </p>
 *
 * @description: 捐赠流水
 * @author: zhangzeyuan
 * @date: Created in 2021-07-22 08:37:50
 */
@ApiModel("捐赠流水")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonationFlowClearDTO {

    private List<Long> userIds ;

    private String userName;

    private Long start;

    private Long end;

    private String phone;

    private Integer settlementState;
    private String userIdList ;


}
