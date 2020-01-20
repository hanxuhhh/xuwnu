package com.hww.house.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:08
 * Description:
 */
@Data
public class House {

    private Long id;

    private String title;

    private int price;

    private int area;
    /**
     * 卧室数量
     */
    private int room;
    /**
     * 楼层
     */
    private int floor;
    /**
     * 总楼层
     */
    private int totalFloor;
    /**
     * 被看次数
     */

    private int watchTimes;
    /**
     * 建立年限
     */
    private int buildYear;
    /**
     * 房屋状态 0-未审核 1-审核通过 2-已出租 3-逻辑删除
     */
    private int status;

    private Date createTime;

    private Date lastUpdateTime;
    /**
     * 城市标记缩写 如 北京bj
     */
    private String cityEnName;
    /**
     * 地区英文简写 如昌平区 cpq
     */
    private String regionEnName;
    /**
     * 封面
     */
    private String cover;
    /**
     * 房屋朝向
     */
    private int direction;
    /**
     * 距地铁距离 默认-1 附近无地铁
     */
    private int distanceToSubway;
    /**
     * 客厅数量
     */
    private int parlour;
    /**
     * 所在小区
     */
    private String district;
    /**
     * 所属管理员id
     */
    private Long adminId;
    /**
     * 浴室
     */
    private int bathroom;
    /**
     * 街道
     */
    private String street;


}
