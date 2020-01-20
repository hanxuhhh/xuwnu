package com.hww.house.mapper;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.entity.HouseTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:47
 * Description:
 */
public interface HouseTagMapper {
    /**
     * 房子标签
     * @param houseId
     * @return
     */
    List<HouseTag> getHouseTagByHouseId(long houseId);

    void saveHouseTags(@Param("houseTags") List<HouseTag> houseTags);

    void deleteTagsById(Long id);

    HouseTag getHouseTagByNameAndHouseId(@Param("houseId") long houseId,@Param("tag") String tag);

    void saveOne(HouseTag houseTag);

    List<HouseTag> findAllByHouseIdIn(@Param("houseIds") List<Long> houseIds);
}
