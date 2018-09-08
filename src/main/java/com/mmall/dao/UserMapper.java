package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);

    int checkEmail(String email);

    // @Param("...") 定义别名，以...名对应mapper   建议有多个参数的时候都要这样定义
    User selectLogin(@Param("username") String username, @Param("password") String password);

    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username,  @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username, @Param("newPassword") String newpassword);

    int checkPassword(@Param("password") String password, @Param("userId") Integer userId);

    // 是否email被其他用户所使用
    int checkOthersEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);
}