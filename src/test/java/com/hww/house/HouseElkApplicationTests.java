package com.hww.house;

import com.hww.house.entity.User;
import com.hww.house.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
//@ActiveProfiles("dev")
public abstract   class HouseElkApplicationTests {


    public  abstract  void test();

}
