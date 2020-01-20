package com.hww.house.service.impl;

import com.google.common.collect.Lists;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.UserDto;
import com.hww.house.entity.Role;
import com.hww.house.entity.User;
import com.hww.house.mapper.UserMapper;
import com.hww.house.service.RoleService;
import com.hww.house.service.UserService;
import com.hww.house.util.LoginUserUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/5
 * @Time: 20:23
 * Description:
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ModelMapper modelMapper;

    private final Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

    @Override
    public User getUserByName(String userName) {
        User user = userMapper.getUserByUserName(userName);
        if (user == null) {
            return null;
        }
        List<Role> roles = roleService.findRolesByUserId(user.getId());

        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("暂无权限");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        user.setAuthorityList(authorities);
        return user;
    }

    @Override
    public ServiceResponse<UserDto> getUserById(Long adminId) {
        User user = userMapper.getUserByUserId(adminId);
        UserDto userDto = modelMapper.map(user, UserDto.class);
        return new ServiceResponse<UserDto>(true, null, userDto);
    }

    @Override
    public User findUserByTelephone(String telephone) {
        User userByPhone = userMapper.getUserByPhone(telephone);
        if (userByPhone == null) {
            return null;
        }
        List<Role> rolesByUserId = roleService.findRolesByUserId(userByPhone.getId());
        List<GrantedAuthority> authorities = new ArrayList<>();
        rolesByUserId.forEach(item -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + item.getName()));
        });
        userByPhone.setAuthorityList(authorities);
        return userByPhone;
    }

    /**
     * 添加用户
     *
     * @param telephone
     * @return
     */
    @Override
    @Transactional
    public User addUserByPhone(String telephone) {
        User user = new User();
        user.setPhoneNumber(telephone);
        user.setName(telephone);
        // user.setName(telephone.substring(0, 3) + "****" + telephone.substring(7, telephone.length()));
        user.setCreateTime(new Date());
        user.setLastLoginTime(new Date());
        userMapper.addUser(user);

        Role role = new Role();
        role.setName("USER");
        role.setUserId(user.getId());
        roleService.save(role);
        user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        return user;
    }

    /**
     * 修改用户
     *
     * @param profile
     * @param value
     * @return
     */
    @Override
    public ServiceResponse updateUser(String profile, String value) {
        Long loginUserId = LoginUserUtil.getLoginUserId();
        User dbUser = userMapper.getUserById(loginUserId);
        switch (profile) {
            case "name":
                dbUser.setName(value);

                break;
            case "email":
                dbUser.setEmail(value);
                break;
            case "password":
                dbUser.setPassword(this.passwordEncoder.encodePassword(value, loginUserId));
                break;
            default:
                return new ServiceResponse(false, "不支持的属性");
        }
        userMapper.updateUser(dbUser);
        return new ServiceResponse(true);
    }

}
