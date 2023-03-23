package com.yupi.usercenter.service;

import com.yupi.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author chouchouccc
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * 用户脱敏（隐藏敏感信息  防止数据库的数据泄露给前端）
     */
    User getSafetyUser(User user);

    /**
     * 请求用户注销
     * @param request
     */
    int userLogout(HttpServletRequest request);

    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    int updateUser(User user,User loginUser);


    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User userLogin);

    /**
     * 获取当前用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 根据标签搜素用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUserByTags(List<String> tagNameList);

}
