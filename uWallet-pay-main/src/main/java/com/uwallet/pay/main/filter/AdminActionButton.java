package com.uwallet.pay.main.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户整合权限按钮
 * 0,账户冻结,
 * 1,分期付冻结,
 * 2,协助注册,
 * 3,协助KYC,
 * 4,延迟还款,
 * 5,修改用户信息,
 * 6,提额,
 * 7,降额,
 * */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminActionButton {
     int value() default -1;
}
