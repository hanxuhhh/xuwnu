package com.hww.house.service.impl;

import com.hww.house.entity.Role;
import com.hww.house.mapper.RoleMapper;
import com.hww.house.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 9:33
 * Description:
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        return roleMapper.findRolesByUserId(userId);
    }

    @Override
    public void save(Role role) {
        roleMapper.addRole(role);
    }
}
