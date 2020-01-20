package com.hww.house.service.impl;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseDetailDto;
import com.hww.house.entity.HouseDetail;
import com.hww.house.mapper.HouseDetailMapper;
import com.hww.house.service.HouseDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:33
 * Description:
 */
@Service
public class HouseDetailServiceImpl implements HouseDetailService {

    @Autowired
    private HouseDetailMapper houseDetailMapper;

    @Override
    public HouseDetail getHouseById(long houseId) {
        return houseDetailMapper.getHouseDetailByHouseId(houseId);
    }

    @Override
    public void saveHouseDetail(HouseDetail houseDetail) {
        houseDetailMapper.saveHouseDetail(houseDetail);
    }

    @Override
    public void update(HouseDetail result) {
        houseDetailMapper.updateDetail(result);
    }

    /**
     * 批量查询房屋细节
     * @param houseIds
     * @return
     */
    @Override
    public List<HouseDetail> findAllByHouseIdIn(List<Long> houseIds) {
        return houseDetailMapper.findAllByHouseIdIn(houseIds);
    }
}
