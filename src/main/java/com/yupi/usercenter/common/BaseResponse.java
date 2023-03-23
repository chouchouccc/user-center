package com.yupi.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * <T>表示这个类是泛型，通用类，可以返回类似String，int各种类型等等
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;
    private T data;
    private String massage;
    private String description;

    public BaseResponse(int code, T data, String massage,String description) {
        this.code = code;
        this.data = data;
        this.massage = massage;
        this.description=description;
    }

    public BaseResponse(int code, T data,String massage) {
        this(code, data, massage,"");

    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getDescription());
    }

}
