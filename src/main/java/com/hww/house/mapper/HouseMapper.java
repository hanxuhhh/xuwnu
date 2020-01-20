package com.hww.house.mapper;

import com.hww.house.base.es.RentSearch;
import com.hww.house.base.datatables.DatatableSearch;
import com.hww.house.entity.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:13
 * Description:
 */
@Mapper
public interface HouseMapper {
    /**
     * 房子详情
     *
     * @param houseId
     */
    House getHouseById(long houseId);

    /**
     * 添加房子
     *
     * @param house
     * @return
     */
    void saveHouse(House house);

    /**
     * admin查询所有的房屋列表
     *
     * @param searchBody
     * @return
     */
    List<House> adminFindAllHousesBySearch(DatatableSearch searchBody);

    /**
     * user查询所有的房屋列表
     *
     * @param rentSearch
     * @return
     */
    List<House> userFindAllHousesBySearch(RentSearch rentSearch);

    /**
     * 修改房屋
     *
     * @param house
     */
    void update(House house);

    /**
     * 修改房屋封面
     *
     * @param coverId
     * @param targetId
     */
    void updateCover(@Param(value = "coverId") Long coverId, @Param(value = "targetId") Long targetId);

    /**
     * 修改房屋状态
     *
     * @param id
     * @param status
     */
    void updateStatus(@Param("id") Long id, @Param("status") int status);

    /**
     * 根据houseIds查询所有房屋列表
     *
     * @param houseIds
     * @return
     */
    List<House> findHouseByHouseIds(@Param("houseIds") List<Long> houseIds);

    /**
     * 更新观看次数
     *
     * @param houseId
     */
    void updateWatchTimes(Long houseId);
}
