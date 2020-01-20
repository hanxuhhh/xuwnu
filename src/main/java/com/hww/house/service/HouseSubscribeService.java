package com.hww.house.service;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseSubscribeDto;
import com.hww.house.entity.HouseSubscribe;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/27
 * @Time: 15:56
 * Description:
 */
public interface HouseSubscribeService {

    ServiceResponse addHouseSubscribe(HouseSubscribe houseSubscribe);

    HouseSubscribe getByHouseIdAndUserId(Long houseId, Long loginUserId);

    List<HouseSubscribe> findAllByHouseIdAndUserId(Long userId, int value);


    void updateHouseSubscribe(HouseSubscribe subscribe);

    void delete(Long id);

    List<HouseSubscribe> findAllByAdminIdAndStatus(Long userId, int value);

    HouseSubscribe getByHouseIdAndAdminId(Long houseId, Long adminId);
}
