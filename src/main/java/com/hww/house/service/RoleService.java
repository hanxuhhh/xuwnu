package com.hww.house.service;

import com.hww.house.entity.Role;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 9:32
 * Description:
 */
public interface RoleService {

    List<Role> findRolesByUserId(Long userId);

    void save(Role role);
}
