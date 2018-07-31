package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("iUserService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int countUser = userMapper.checkUsername(username);
        if( countUser == 0){return ServerResponse.createByErrorMessage("Username not exist!");}

        // todo 密码登录md5

        User user = userMapper.selectLogin(username,password);
        if(user == null){
            return ServerResponse.createByErrorMessage("Password error");
        }



        user.setPassword(StringUtils.EMPTY);  //密码置空
        return ServerResponse.createBySuccess("登录成功",user);

    }





}
