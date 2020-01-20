package com.hww.house.mapper;

import com.hww.house.entity.HouseDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:28
 * Description:
 */
@Mapper
public interface HouseDetailMapper {
    /**
     * 查询房屋详情
     * @param houseId
     * @return
     */
    HouseDetail getHouseDetailByHouseId(long houseId);

    void saveHouseDetail(HouseDetail houseDetail);

    void updateDetail(HouseDetail result);

    List<HouseDetail> findAllByHouseIdIn(@Param("houseIds") List<Long> houseIds);
}
