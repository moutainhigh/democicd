package com.uwallet.pay.core.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Response {
    private String errorCode;
    private String errorMessage;
    @JsonProperty(value = "data")
    private Object body;
}
