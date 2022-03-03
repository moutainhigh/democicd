package com.uwallet.pay.main.exception;

import com.uwallet.pay.core.common.R;
import com.uwallet.pay.core.enumeration.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author faker
 * @since 2018/7/3
 */
@ControllerAdvice
@Slf4j
public class ExceptionHandlerAdvice {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletResponse response, HttpServletRequest request) {
        log.error("global handle Exception", e);

        String code = ErrorCodeEnum.FAIL_CODE.getCode();
        String msg = "Operate unsuccessfully";

        if (e.getClass().getName().contains("TokenException")) {
            code = ((TokenException) e).getCode();
            msg = e.getMessage();
        }

        if (e.getClass().getName().contains("SignException")) {
            code = ((SignException) e).getCode();
            msg = e.getMessage();
        }

        if (e.getClass().getName().contains("PosApiException")) {
            code = ((PosApiException) e).getCode();
            msg = e.getMessage();
        }

        if ("100200403".equals(e.getMessage())) {
            Map<String, Object> codeMap = new HashMap<>(16);
            codeMap.put("code", "forbidden");
            response.reset();
            return ResponseEntity.status(403).body(codeMap);
        }

        if ("100200404".equals(e.getMessage())) {
            Map<String, Object> codeMap = new HashMap<>(16);
            codeMap.put("code", "401");
            codeMap.put("message", "Unauthorized");
            response.reset();
            return ResponseEntity.status(401).body(codeMap);
        }

        if(request.getServletPath().split("/")[1].equals("payments")
                || request.getServletPath().split("/")[1].equals("orders")
                || request.getServletPath().split("/")[1].equals("merchant"))
        {
            Map<String, Object> codeMap = new HashMap<>(16);
            codeMap.put("code", "404");
            codeMap.put("message", "Operate unsuccessfully");
            response.reset();
            return ResponseEntity.status(400).body(codeMap);
        }

        return R.fail(code, msg);
    }
}
