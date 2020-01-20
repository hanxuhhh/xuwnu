package com.hww.house.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/11
 * @Time: 20:04
 * Description:
 */
@Data
public class UserDto {

    private Long id;

    private String name;

    private String avatar;

    private String phoneNumber;

    private Date lastLoginTime;


}
