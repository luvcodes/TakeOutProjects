package com.sky.annotation;

import com.sky.enumeration.OperationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 * 因为这是要做公共字段自动填充的功能
 * 只制定了UPDATE和INSERT是因为只有这两个情况的时候才有公共字段自动填充
 *
 * @author ryanw*/
// 指定注解只能加到方法上
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autofill {
    // 指定当前数据库操作的类型: UPDATE, INSERT
    OperationType value();
}
