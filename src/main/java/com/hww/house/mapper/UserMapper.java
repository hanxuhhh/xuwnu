package com.hww.house.mapper;

import com.hww.house.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/11/29
 * @Time: 13:52
 * Description:
 */
@Repository
public interface UserMapper {


    User getUserByUserName(String userName);


    User getUserById(long userId);

    User getUserByUserId(Long adminId);

    User getUserByPhone(String telephone);

    void addUser(User user);


    void updateUser(User user);
}
