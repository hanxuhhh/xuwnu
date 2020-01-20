package com.hww.house.service.impl;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.entity.HousePicture;
import com.hww.house.mapper.HousePictureMapper;
import com.hww.house.service.HousePictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:44
 * Description:
 */
@Service
public class HousePictureServiceImpl implements HousePictureService {

    @Autowired
    private HousePictureMapper housePictureMapper;

    /**
     * 根据房屋id查询房屋图片
     *
     * @param houseId
     */
    @Override
    public List<HousePicture> getHousePictureByHouseId(long houseId) {
        return housePictureMapper.getHousePicturesByHouseId(houseId);
    }

    @Override
    public void savePictures(List<HousePicture> housePictures) {
        housePictureMapper.savePictures(housePictures);
    }

    @Override
    public void update(List<HousePicture> housePictures) {
        housePictureMapper.updatePictures(housePictures);
    }

    @Override
    public ServiceResponse deletePictures(Long id) {
        housePictureMapper.deletePictures(id);
        return new ServiceResponse(true);
    }
}
