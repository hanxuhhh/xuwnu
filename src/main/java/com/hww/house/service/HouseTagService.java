package com.hww.house.service;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.entity.HouseTag;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:49
 * Description:
 */
public interface HouseTagService {

    List<HouseTag> getHouseTagByHouseId(long houseId);

    void save(List<HouseTag> houseTags);

    /**
     * 删除房屋标签
     * @param id
     */
    ServiceResponse deleteTagsByHouseIdAndTag(Long id,String tag);

    /**
     * 添加标签
     * @param houseId
     * @param tag
     * @return
     */
    ServiceResponse addTag(Long houseId, String tag);

    /**
     * 根据房屋id查询房屋标签
     * @param houseIds
     * @return
     */
    List<HouseTag> findAllByHouseIdIn(List<Long> houseIds);
}
