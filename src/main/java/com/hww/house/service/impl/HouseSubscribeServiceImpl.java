package com.hww.house.service.impl;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.entity.HouseSubscribe;
import com.hww.house.mapper.HouseSubscribeMapper;
import com.hww.house.service.HouseSubscribeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/27
 * @Time: 15:56
 * Description:
 */
@Service
public class HouseSubscribeServiceImpl implements HouseSubscribeService {

    @Autowired
    private HouseSubscribeMapper houseSubscribeMapper;

    @Override
    public ServiceResponse addHouseSubscribe(HouseSubscribe houseSubscribe) {
        houseSubscribeMapper.addHouseSubscribe(houseSubscribe);
        return new ServiceResponse(true);
    }

    @Override
    public HouseSubscribe getByHouseIdAndUserId(Long houseId, Long loginUserId) {
        return houseSubscribeMapper.getByHouseIdAndUserId(houseId, loginUserId);
    }

    /**
     * 查询一个用户所有预约的预约看房
     *
     * @param userId
     * @param value
     * @return
     */
    @Override
    public List<HouseSubscribe> findAllByHouseIdAndUserId(Long userId, int value) {

        HouseSubscribe houseSubscribe = new HouseSubscribe();
        houseSubscribe.setUserId(userId);
        houseSubscribe.setStatus(value);
        return houseSubscribeMapper.findHouseSubscribesByUserId(houseSubscribe);
    }

    @Override
    public void updateHouseSubscribe(HouseSubscribe subscribe) {
        houseSubscribeMapper.update(subscribe);
    }

    @Override
    public void delete(Long id) {
        houseSubscribeMapper.delete(id);
    }

    @Override
    public List<HouseSubscribe> findAllByAdminIdAndStatus(Long userId, int value) {
        HouseSubscribe houseSubscribe=new HouseSubscribe();
        houseSubscribe.setAdminId(userId);
        houseSubscribe.setStatus(value);
        return houseSubscribeMapper.findHouseSubscribesByUserId(houseSubscribe);
    }

    @Override
    public HouseSubscribe getByHouseIdAndAdminId(Long houseId, Long adminId) {
        HouseSubscribe houseSubscribe=new HouseSubscribe();
        houseSubscribe.setAdminId(adminId);
        houseSubscribe.setHouseId(houseId);
        return houseSubscribeMapper.getByHouseIdAndAdminId(houseSubscribe);
    }
}
