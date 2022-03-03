package com.uwallet.pay.main.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Strong
 * @Date 2019/8/26
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionFlag {

    boolean required() default true;

    /**
     * 获取方法描述
     */
    String detail() default "";
}