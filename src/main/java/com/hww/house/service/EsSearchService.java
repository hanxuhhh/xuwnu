package com.hww.house.service;

import com.hww.house.base.bmap.MapSearch;
import com.hww.house.base.es.RentSearch;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseBucketDto;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/12
 * @Time: 15:24
 * Description:索引相当于关系数据库的数据库
 */
public interface EsSearchService {
    /**
     * 检索目标房源
     *
     * @param houseId
     */
    void index(long houseId);

    /**
     * 从索引中移除信息
     *
     * @param houseId
     */
    void remove(long houseId);

    /**
     * 获取补全建议关键词
     *
     * @param rentSearch
     * @return
     */
    BaseServiceResponse<Long> userEsQuery(RentSearch rentSearch);

    /**
     * 关键字模糊匹配
     *
     * @param prefix
     * @return
     */
    ServiceResponse<List<String>> suggest(String prefix);

    /**
     * 聚合统计小区的房源
     */
    ServiceResponse<Long> aggregateDistrictHouse(String cityEnName, String regionName, String district);

    /**
     * 聚合统计城市房源信息数量
     */
    BaseServiceResponse<HouseBucketDto> aggregateHouseCountByCityEnName(String cityEnName);

    /**
     * es地图查询==>房屋id
     * @return
     */
    BaseServiceResponse<Long> mapQuery(String cityEnNAme,String orderBy,String orderDirection,int start,int size);

    /**
     * 小地图查询
     * @param mapSearch
     * @return
     */
    BaseServiceResponse<Long> mapBoundQuery(MapSearch mapSearch);
}
