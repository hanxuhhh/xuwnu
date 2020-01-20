package com.hww.house.service;

import com.hww.house.entity.Subway;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:28
 * Description:
 */
public interface SubwayService {

    List<Subway> findAllSubwayByCity(String cityName);

    Subway getSubwayById(long id);
}
