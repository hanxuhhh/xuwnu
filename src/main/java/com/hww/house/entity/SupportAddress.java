package com.hww.house.entity;

import lombok.Data;

@Data
public class SupportAddress {

    private Long id;
    // 上一级行政单位
    private String belongTo;

    private String enName;

    private String cnName;

    private String level;

    private double baiDuMapLongitude;

    private double baiDuMapLatitude;


}
