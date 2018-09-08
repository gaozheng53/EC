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
        int countUser = userMapper.checkUsername(username);   //  <--username参数传进去是null
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
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken); //放入缓存
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String token){
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            // 用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String getToken = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(getToken)){
            return ServerResponse.createByErrorMessage("token无效或过期");
        }

        // 如果直接是用s1.equals(s2)不安全，万一s1是null那么就报错空指针了，StringUtils则可以规避。
        if(StringUtils.equals(token,getToken)){
            // token匹配成功
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int count  = userMapper.updatePasswordByUsername(username,md5Password);
            if(count > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请重新获取");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user){
        // 防止横向越权，要校验一下这个用户的旧密码匹配这个用户
        int count = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if(count == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);  // 选择性更新而不是全部更新
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }else{
            return ServerResponse.createByErrorMessage("密码更新失败");
        }
    }

    public ServerResponse<User> updateUserInfo(User user){
        // username不能被更新； email也要校验是否已被其他用户使用
        int count = userMapper.checkOthersEmailByUserId(user.getEmail(),user.getId());
        if(count > 0){
            return ServerResponse.createByErrorMessage("email已被占用");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setPhone(user.getPhone());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getUserInfo(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到用户");
        }
        user.setPassword(StringUtils.EMPTY);   // 安全起见，密码置空
        return  ServerResponse.createBySuccess(user);
    }


    /**
     * Back-end
     */

    // 校验是否是管理员
    public ServerResponse checkAdmin(User user){
        if(user != null && user.getRole().intValue() == Const.role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }



}
