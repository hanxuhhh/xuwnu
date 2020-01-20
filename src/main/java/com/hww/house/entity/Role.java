package com.hww.house.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

/**
 * Created by 瓦力.
 */
@Data
@ToString
public class Role {
    private Long id;
    private Long userId;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
