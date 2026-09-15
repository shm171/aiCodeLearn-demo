package com.mylab.ailearn.base.global.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限制带用户 ID 的接口只能由该用户本人或管理员调用。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserOrAdmin {

    /** 用户 ID 在控制器方法参数中的位置。 */
    int idArgumentIndex() default 0;
}
