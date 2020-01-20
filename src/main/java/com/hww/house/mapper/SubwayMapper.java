package com.hww.house.mapper;

import com.hww.house.entity.Subway;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:24
 * Description:
 */
@Mapper
public interface SubwayMapper {

    List<Subway> findAllSubwayByCity(String cityName);

    Subway getSubway(long id);
}
