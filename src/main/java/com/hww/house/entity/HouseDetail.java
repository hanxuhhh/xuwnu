package com.hww.house.entity;

import lombok.Data;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:24
 * Description:
 */
@Data
public class HouseDetail {

    private Long id;
    /**
     * 详细描述
     */
    private String description;
    /**
     * 户型介绍
     */
    private String layoutDesc;
    /**
     * 交通出行
     */
    private String traffic;
    /**
     * 周边配套
     */
    private String roundService;
    /**
     * 租赁方式
     */
    private int rentWay;
    /**
     * 详细地址  需要映射
     */
    private String address;
    /**
     * 附近地铁线id
     */
    private Long subwayLineId;
    /**
     * 附近地铁线名称
     */
    private String subwayLineName;
    /**
     * 地铁站id
     */
    private Long subwayStationId;
    /**
     * 地铁站名
     */
    private String subwayStationName;
    /**
     * 对应house的id
     */
    private Long houseId;
}
