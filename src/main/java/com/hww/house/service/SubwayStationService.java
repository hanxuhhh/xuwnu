package com.hww.house.service;

import com.hww.house.entity.SubwayStation;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:45
 * Description:
 */
public interface SubwayStationService {
    /**
     * 查询铁路的所有站
     * @param subwayId
     * @return
     */
    List<SubwayStation> findAllStationBySubway(Long subwayId);

    /**
     * 站点详情
     * @param id
     * @return
     */
    SubwayStation getSubwayStationById(long id);

}
