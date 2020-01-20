package com.hww.house.mapper;

import com.hww.house.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 9:24
 * Description:
 */
@Mapper
public interface RoleMapper {

    List<Role> findRolesByUserId(Long userId);

    void addRole(Role role);
}
