package com.hww.house.service;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HousePictureDto;
import com.hww.house.entity.HousePicture;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:40
 * Description:
 */
public interface HousePictureService {
    /**
     * 房子图片
     * @param houseId
     * @return
     */
    List<HousePicture> getHousePictureByHouseId(long houseId);

    /**
     * 房屋图片
     * @param housePictures
     */
    void savePictures(List<HousePicture> housePictures);

    /**
     * 修改房屋图片
     * @param housePictures
     */
    void update(List<HousePicture> housePictures);

    /**
     * 删除图片
     * @param id
     */
    ServiceResponse deletePictures(Long id);


}
