package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author ryanw
 * 全局异常处理器
 */
// ControllerAdvice注解会对指定的带有后面包括这些注解的类进行拦截
@ControllerAdvice(annotations = {RestController.class, Controller.class})
// 方法要返回json数据，所以用ResponseBody
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法注解: 一旦controller抛出这个异常，就会在这里进行处理
     * */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());

        return R.error(ex.getMessage());
    }
}
