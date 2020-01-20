package com.hww.house.service;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseDetailDto;
import com.hww.house.entity.HouseDetail;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:32
 * Description:
 */
public interface HouseDetailService {
    /**
     * 查询房屋详情
     * @param houseId
     * @return
     */
    HouseDetail getHouseById(long houseId);

    /**
     * 添加房屋
     * @param houseDetail
     * @return
     */
    void saveHouseDetail(HouseDetail houseDetail);

    /**
     * 修改房屋
     * @param result
     */
    void update(HouseDetail result);

    /**
     * 查询房屋细节根据房屋id
     * @param houseIds
     * @return
     */
    List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds);
}
