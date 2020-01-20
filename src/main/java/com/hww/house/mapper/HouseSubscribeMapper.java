package com.hww.house.mapper;

import com.hww.house.entity.HouseSubscribe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/27
 * @Time: 15:49
 * Description:
 */
@Mapper
public interface HouseSubscribeMapper {
    /**
     * 添加预约
     * @param houseSubscribe
     */
    void addHouseSubscribe(HouseSubscribe houseSubscribe);


    HouseSubscribe getByHouseIdAndUserId(@Param("houseId") Long houseId, @Param("loginUserId") Long loginUserId);



    /**
     * 查询预约看房列表
     * @param houseSubscribe
     * @return
     */
    List<HouseSubscribe> findHouseSubscribesByUserId(HouseSubscribe houseSubscribe);

    /**
     * 修改
     * @param subscribe
     */
    void update(HouseSubscribe subscribe);

    /**
     * 删除
     * @param id
     */
    void delete(Long id);

    /**
     * 管理员查询
     * @param houseSubscribe
     * @return
     */
    HouseSubscribe getByHouseIdAndAdminId(HouseSubscribe houseSubscribe);
}
