package com.hww.house;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hww
 */
@MapperScan("com.hww.house.mapper")
@SpringBootApplication
@EnableAsync
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)
public class HouseElkApplication {

    public static void main(String[] args) {
        SpringApplication.run(HouseElkApplication.class, args);
    }

}
