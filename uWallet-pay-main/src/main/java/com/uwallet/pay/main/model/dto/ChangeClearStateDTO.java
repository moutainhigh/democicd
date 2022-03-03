package com.uwallet.pay.main.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ApiModel("清算状态未清算/延迟互转请求DTO")
@Data
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeClearStateDTO {


    /**
     * 类型  0：未清算转延迟  1：延迟转未清算
     */
    Integer type ;

    /**
     * 订单号列表
     */
    List<String> idList;

}
