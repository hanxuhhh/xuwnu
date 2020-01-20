package com.hww.house.service;

import com.hww.house.base.bmap.MapSearch;
import com.hww.house.base.enums.HouseSubscribeStatus;
import com.hww.house.base.es.RentSearch;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.base.datatables.DatatableSearch;
import com.hww.house.dto.HouseDto;
import com.hww.house.dto.HouseSubscribeDto;
import com.hww.house.entity.House;
import com.hww.house.form.HouseForm;
import com.hww.house.base.service.response.BaseServiceResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.util.Pair;

import java.util.Date;
import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:55
 * Description:
 */
public interface HouseService {

    /**
     * 添加房屋
     *
     * @param houseForm
     * @return
     */
    ServiceResponse<HouseDto> save(HouseForm houseForm);

    /**
     * 查询所有的房屋列表
     *
     * @param searchBody
     * @return
     */
    BaseServiceResponse<HouseDto> adminQuery(DatatableSearch searchBody);

    /**
     * 查询房屋详情
     *
     * @param id
     * @return
     */
    ServiceResponse<HouseDto> findCompleteOne(Long id);

    /**
     * 修改房屋
     *
     * @param houseForm
     * @return
     */
    ServiceResponse update(HouseForm houseForm);


    ServiceResponse updateCover(Long coverId, Long targetId);

    House getHouseById(long id);

    /**
     * 删除，发布。。。。。。
     *
     * @param id
     * @param i
     * @return
     */
    ServiceResponse updateStatus(Long id, int i);

    /**
     * 用户房屋查询
     *
     * @param rentSearch
     * @return
     */
    BaseServiceResponse<HouseDto> userQuery(RentSearch rentSearch);


    /**
     * 根据houseIds查询所有房屋列表
     *
     * @param houseIds
     * @return
     */
    List<House> findHouseByHouseIds(@Param("houseIds") List<Long> houseIds);


    /**
     * 全地图查询
     *
     * @param mapSearch
     * @return
     */
    BaseServiceResponse<HouseDto> wholeMapQuery(MapSearch mapSearch);

    /**
     * 缩放地图查询
     *
     * @param mapSearch
     * @return
     */
    BaseServiceResponse<HouseDto> boundMapQuery(MapSearch mapSearch);

    /**
     * 加入待看清单
     *
     * @param houseId
     * @return
     */
    ServiceResponse addSubscribeOrder(Long houseId);

    /**
     * 分页查询预约看房的列表
     * 获取对应状态的预约列表
     *
     * @param status
     * @param start
     * @param size
     * @return
     */
    BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> querySubscribeList(HouseSubscribeStatus status, int start, int size);

    /**
     * 用户预约看房
     *
     * @param houseId
     * @param orderTime
     * @param telephone
     * @param desc
     * @return
     */
    ServiceResponse subscribe(Long houseId, Date orderTime, String telephone, String desc);

    /**
     * 取消预约
     *
     * @param houseId
     * @return
     */
    ServiceResponse cancelSubscribe(Long houseId);

    /**
     * 管理员查看用户预约记录
     *
     * @param start
     * @param size
     * @return
     */
    BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> adminFindSubscribeList(int start, int size);

    /**
     * 管理员完成预约
     * @param houseId
     * @return
     */
    ServiceResponse finishSubscribe(Long houseId);

    /**
     * 更新观看次数
     * @param houseId
     */
    void updateWatchTimes(Long houseId);
}
