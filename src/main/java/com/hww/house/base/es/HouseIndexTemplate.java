package com.hww.house.base.es;

import com.hww.house.base.bmap.BaiDuMapLocation;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/12
 * @Time: 14:36
 * Description: 索引结构模板
 */
@Data
public class HouseIndexTemplate {
    private Long houseId;

    private String title;

    private int price;

    private int area;
    private Date createTime;
    private Date lastUpdateTime;

    private String cityEnName;

    private String regionEnName;

    private int direction;

    private int distanceToSubway;

    private String subwayLineName;

    private String subwayStationName;

    private String street;

    private String district;

    private String description;

    private String layoutDesc;

    private String traffic;

    private String roundService;

    private int rentWay;

    private List<String> tags;
    //输入自动补全字段
    private List<HouseSuggest> suggest;

    //百度地图金纬度
    private BaiDuMapLocation location;
}
