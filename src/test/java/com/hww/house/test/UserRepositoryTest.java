package com.hww.house.test;

import com.hww.house.HouseElkApplicationTests;
import com.hww.house.entity.User;
import com.hww.house.mapper.UserMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/11/29
 * @Time: 14:03
 * Description:
 */

public class UserRepositoryTest extends HouseElkApplicationTests {

    @Autowired
    private UserMapper userMapper;
    @Test
    @Override
    public void test() {
        User waliwali = userMapper.getUserByUserName("waliwali");
        System.out.println("waliwali = " + waliwali);
    }
}
