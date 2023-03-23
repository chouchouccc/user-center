package com.yupi.usercenter.model.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 用户
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {
    /**
     * TableId 自动生成这个主键
     */
    @TableId(type = IdType.AUTO)
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 角色 0-普通用户 1-管理员
     */
    private Integer userRole;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态 0-正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 是否删除 TableLogic 用来表示逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 臭臭星球编号
     */
    private String planetCode;

    /**
     * 标签  json  列表
     */
    private String tags;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}