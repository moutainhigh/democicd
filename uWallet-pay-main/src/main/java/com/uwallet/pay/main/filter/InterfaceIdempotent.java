package com.uwallet.pay.main.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在需要保证幂等性的接口上 Controller上使用此注解
 * @Author: aarons
 * @Date: 2020/10/27
 * @Version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterfaceIdempotent {

}
