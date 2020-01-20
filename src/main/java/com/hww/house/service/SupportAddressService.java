package com.hww.house.service;

import com.hww.house.base.bmap.BaiDuMapLocation;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.SupportAddressDto;
import com.hww.house.entity.Subway;
import com.hww.house.entity.SubwayStation;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.entity.SupportAddress;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 14:53
 * Description:
 */
public interface SupportAddressService {
    /**
     * 城市
     *
     * @return
     */
    BaseServiceResponse<SupportAddressDto> findAll();

    /**
     * 根据城市英文简写获取城市详细信息
     *
     * @param cityName
     * @return
     */
    SupportAddress getByEnNameAndLevel(String cityName);

    /**
     * @param levelName
     * @param level
     * @return
     */
    SupportAddress getSupportAddress(String levelName, String level);

    /**
     * 查询具体的城市和区域
     *
     * @return
     */
    SupportAddress getByEnNameAndBelongTo(String regionEnName, String belongTo);


    /**
     * 区域
     *
     * @param cityEnName
     * @return
     */
    BaseServiceResponse<SupportAddressDto> findAllRegionsByCityName(String cityEnName);

    /**
     * 城市下的所有铁路线
     *
     * @param cityEnName
     * @return
     */
    BaseServiceResponse<Subway> findAllSubwayByCity(String cityEnName);

    /**
     * 根据铁路线获取站点
     *
     * @param subwayId
     * @return
     */
    List<SubwayStation> findAllStationBySubway(Long subwayId);

    /**
     * 根据城市以及具体地位名称获取经纬度
     *
     * @param city
     * @return
     */
    ServiceResponse<BaiDuMapLocation> getBaiDuMapLocationByCity(String city, String region);

    /**
     * 上传至百度云地里服务lbs
     *
     * @param baiDuMapLocation
     * @param area
     * @param title
     * @param price
     * @param address
     * @param houseId
     * @return
     */
    ServiceResponse lbsUpload(BaiDuMapLocation baiDuMapLocation, int area, String title, int price, String address, Long houseId,String tags,String imageUrl);

    /**
     * 删除lbs的数据
     *
     * @param houseId
     * @return
     */
    ServiceResponse removeLbs(Long houseId);
}
