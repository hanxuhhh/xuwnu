package com.hww.house.service.impl;

import com.hww.house.entity.SubwayStation;
import com.hww.house.mapper.SubwayStationMapper;
import com.hww.house.service.SubwayStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:46
 * Description:
 */
@Service
public class SubwayStationServiceImpl implements SubwayStationService {

    @Autowired
    private SubwayStationMapper subwayStationMapper;

    @Override
    public List<SubwayStation> findAllStationBySubway(Long subwayId) {
        return subwayStationMapper.findAllStationBySubway(subwayId);
    }

    @Override
    public SubwayStation getSubwayStationById(long id) {
        return subwayStationMapper.getSubwayStation(id);
    }
}
