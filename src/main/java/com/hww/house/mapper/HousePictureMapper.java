package com.hww.house.mapper;

import com.hww.house.entity.HousePicture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:36
 * Description:
 */
@Mapper
public interface HousePictureMapper {
    /**
     * 查询房屋图片
     * @param houseId
     * @return
     */
    List<HousePicture> getHousePicturesByHouseId(long houseId);

    void savePictures(@Param("housePictures") List<HousePicture> housePictures);

    /**
     * 批量修改房屋图片
     * @param housePictures
     */
    void updatePictures(@Param("pictures") List<HousePicture> housePictures);

    /**
     * 删除图片
     * @param id
     */
    void deletePictures(Long id);
}
