package com.hww.house.service;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.UserDto;
import com.hww.house.entity.User;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/5
 * @Time: 20:21
 * Description: 用户服务
 */
public interface UserService {

    User getUserByName(String userName);

    ServiceResponse<UserDto> getUserById(Long adminId);

    User findUserByTelephone(String telephone);

    User addUserByPhone(String telephone);

    ServiceResponse updateUser(String profile, String value);

}
