package com.hww.house.service.impl;

import com.hww.house.entity.Subway;
import com.hww.house.mapper.SubwayMapper;
import com.hww.house.service.SubwayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:29
 * Description:
 */
@Service
public class SubwayServiceImpl implements SubwayService {

    @Autowired
    private SubwayMapper subwayMapper;

    @Override
    public List<Subway> findAllSubwayByCity(String cityName) {
        return subwayMapper.findAllSubwayByCity(cityName);
    }

    @Override
    public Subway getSubwayById(long id) {
        return subwayMapper.getSubway(id);
    }
}
