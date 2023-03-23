package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.usercenter.common.ErrorCode;
import com.yupi.usercenter.contant.UserContant;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.domain.User;
import com.yupi.usercenter.service.UserService;
import com.yupi.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.usercenter.contant.UserContant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author chouchouccc
 * @createDate 2023-02-05 16:10:23
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值 混淆密码
     */
    private static final String SALT = "chouchouccc";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "臭臭星球称号过长");
        }
        //账户不能包含特殊字符
        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',.<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入中含有特殊字符");
        }
        //密码和校验码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验码不可相同");
        }
        //账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        //long count1 = this.count(queryWrapper);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已重复");
        }
        // 臭臭星球不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "臭臭星球账号已重复");
        }
        //2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "保存错误");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            // todo 返回 修改为自定义异常
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            return null;
        }
        //2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userAccount", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("user login failed . userAccount cannot match userPassword");
            return null;
        }
        //3用户脱敏（隐藏敏感信息  防止数据库的数据泄露给前端）
        User safetyUser = getSafetyUser(user);
        //4 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        //5 返回安全的用户信息
        return safetyUser;
    }


    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public int updateUser(User user, User userLogin) {
        //long默认为0
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员 允许更新其他用户
        //如果是普通用户 只允许更新当前的(自己)用户信息
        if (isAdmin(userLogin) && userId != userLogin.getId()) {
            User oldUser = userMapper.selectById(userId);
            if (oldUser == null) {
                throw new BusinessException(ErrorCode.NULL_ERROR);
            }
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObject == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObject;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserContant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserContant.ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User userLogin) {
        //普通用户自己
        return userLogin != null && userLogin.getUserRole() == UserContant.ADMIN_ROLE;
    }

    /**
     * 根据标签搜素用户(内存过滤)
     *
     * @param tagNameList
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //把数据全部存入内存中，然后在内存中去判断，是否包含这个标签
        // 1、 先查询所有用户
        long startTime = System.currentTimeMillis();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2、 在内容中判断是否含有要求的标签
        userList.parallelStream().filter(user -> {
            String tagsStr = user.getTags();
            //如果从数据库中取出 就没有tags这个值 则返回为空
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            //它是一个链式调用用ofnullable去封装一个可能为空的对象
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            /*如果需要反序列化的话 反序列化
             gson.toJson(tempTagNameSet);*/
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        //         内存
        log.info("memory query time = " + (System.currentTimeMillis() - startTime));
        return userList;
    }

    /**
     * 根据标签搜素用户(sql 查询 “like模糊查询”)
     *
     * @Deprecated 表示这个方法过期了 不再使用
     */
    @Deprecated
    private List<User> searchUserByTags2(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long startTime = System.currentTimeMillis();
        // 第一种方式 根据like 模糊查询直接在数据库中查询指定的标签，
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接 and 查询  like '%java%' and like '%python%'
        for (String tags2 : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagNameList);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        log.info("sql query time = " + (System.currentTimeMillis() - startTime));
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




