package com.hww.house.mapper;

import com.hww.house.entity.SubwayStation;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:42
 * Description:
 */
@Mapper
public interface SubwayStationMapper {
    List<SubwayStation> findAllStationBySubway(Long subwayId);

    SubwayStation getSubwayStation(long id);
}
