package com.hww.house.mapper;

import com.hww.house.dto.HouseDto;
import com.hww.house.entity.SupportAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 14:53
 * Description:
 */
@Mapper
public interface SupportAddressMapper {

    List<SupportAddress> findAllByLevel(String level);

    List<SupportAddress> findAllByCity(@Param("level") String level, @Param("cityName") String cityName);

    SupportAddress getByEnNameAndLevel(@Param("cityEnName") String cityName, @Param("level") String level);

    SupportAddress getByEnNameAndBelongTo(@Param("regionEnName") String regionEnName, @Param("belongTo") String belongTo);

    SupportAddress getSupportAddress(@Param("levelName") String levelName, @Param("level") String level);
}
