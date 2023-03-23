package com.yupi.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author chouchouccc
 */
@Data //自动生成get/set方法
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -7360266844878619800L;
   
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
