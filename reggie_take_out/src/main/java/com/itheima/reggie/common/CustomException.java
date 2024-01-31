package com.itheima.reggie.common;

/**
 * 自定义业务异常类
 * @author ryanw
 */
public class CustomException extends RuntimeException {
    public CustomException(String message){
        super(message);
    }
}
