package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int countUser = userMapper.checkUsername(username);
        if( countUser == 0){return ServerResponse.createByErrorMessage("用户名不存在");}


        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }



        user.setPassword(StringUtils.EMPTY);  //密码置空
        return ServerResponse.createBySuccess("登录成功",user);

    }

    public ServerResponse<String> register(User user){
        // 校验用户名，邮箱
        ServerResponse validresponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validresponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户名已存在");
        }
        validresponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validresponse.isSuccess()){
            return ServerResponse.createByErrorMessage("邮箱已存在");
        }
        // 校验通过
        user.setRole(Const.role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int countUser = userMapper.insert(user);  //返回生效的行数
        if (countUser == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str, String type){
        if(StringUtils.isNoneBlank(type)){   // 区别： " " isNoneBlank() false   isNoneEmpty() true
            if(Const.USERNAME.equals(type)){
                int countUser = userMapper.checkUsername(str);
                if(countUser > 0){return ServerResponse.createByErrorMessage("用户名已存在");}
            }
            if(Const.EMAIL.equals(type)){
                int countUser = userMapper.checkEmail(str);
                if(countUser > 0){return ServerResponse.createByErrorMessage("邮箱已存在");}
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }


    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            // 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNoneBlank(question)){
            return ServerResponse.createBySuccess("question");
        }

        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }


    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int count = userMapper.checkAnswer(username,question,answer);
        if (count > 0){
            // 校验成功
            String forgetToken = UUID.randomUUID().toString();  //生成一个几乎可以unique的字符串
            TokenCache.setKey("token_"+username,forgetToken); //放入缓存
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

}
