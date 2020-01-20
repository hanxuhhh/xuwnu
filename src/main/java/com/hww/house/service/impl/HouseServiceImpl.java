package com.hww.house.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hww.house.base.bmap.MapSearch;
import com.hww.house.base.enums.HouseSubscribeStatus;
import com.hww.house.base.es.RentSearch;
import com.hww.house.base.datatables.DatatableSearch;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseDetailDto;
import com.hww.house.dto.HouseDto;
import com.hww.house.dto.HousePictureDto;
import com.hww.house.dto.HouseSubscribeDto;
import com.hww.house.entity.*;
import com.hww.house.exception.HouseException;
import com.hww.house.form.HouseForm;
import com.hww.house.form.PhotoForm;
import com.hww.house.mapper.HouseMapper;
import com.hww.house.service.*;
import com.hww.house.util.jdbc.DataProcessUtil;
import com.hww.house.util.LoginUserUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:56
 * Description:
 */
@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private HousePictureService housePictureService;
    @Autowired
    private HouseDetailService houseDetailService;
    @Autowired
    private HouseTagService houseTagService;

    @Autowired
    private SubwayStationService subwayStationService;

    @Autowired
    private SubwayService subwayService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EsSearchService searchService;


    @Value("${qiniu.cdnPath}")
    private String cdnPath;

    @Autowired
    private HouseSubscribeService houseSubscribeService;

    /**
     * 添加·房屋
     *
     * @param houseForm
     * @return
     */
    @Override
    @Transactional
    public ServiceResponse<HouseDto> save(HouseForm houseForm) {

        /*添加房屋信息--start*/
        House house = new House();
        modelMapper.map(houseForm, house);
        Date date = new Date();
        house.setCreateTime(date);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        houseMapper.saveHouse(house);
        /*添加房屋信息--end*/

        /*添加房屋详细信息--start*/
        HouseDetail houseDetail = wrapperDetailInfo(houseForm).getResult();
        houseDetail.setHouseId(house.getId());
        houseDetailService.saveHouseDetail(houseDetail);
        /*添加房屋详细信息--end*/

        /*添加房屋图片信息--start*/
        List<HousePicture> housePictures = generatePictures(houseForm, house.getId());
        housePictureService.savePictures(housePictures);
        /*添加房屋图片信息--end*/

        /*添加房屋标签信息--start*/
        List<String> tags = addHouseTags(house.getId(), houseForm);
        /*添加房屋标签信息--end*/


        /*返回结果信息--start*/
        HouseDto houseDto = modelMapper.map(house, HouseDto.class);
        HouseDetailDto houseDetailDTO = modelMapper.map(houseDetail, HouseDetailDto.class);
        houseDto.setHouseDetail(houseDetailDTO);
        List<HousePictureDto> pictureDtoS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDtoS.add(modelMapper.map(housePicture, HousePictureDto.class)));
        houseDto.setPictures(pictureDtoS);
        houseDto.setCover(this.cdnPath + houseDto.getCover());
        houseDto.setTags(tags);
        /*返回结果信息--end*/
        return new ServiceResponse<HouseDto>(true, null, houseDto);
    }

    /**
     * admin查询所有房屋信息
     *
     * @param searchBody
     * @return
     */
    @Override
    public BaseServiceResponse<HouseDto> adminQuery(DatatableSearch searchBody) {
        List<HouseDto> houseDtos = new ArrayList<>();
        //关键之处：只会对紧跟着的查询做分页处理，如果有两次分页，则还需调用一次
        int pageNum = searchBody.getStart() / searchBody.getLength();
        PageHelper.startPage(pageNum + 1, searchBody.getLength());
        //查询所有的房屋列表
        List<House> houses = houseMapper.adminFindAllHousesBySearch(searchBody);

        houses.forEach(item -> {
            HouseDto houseDto = modelMapper.map(item, HouseDto.class);
            houseDto.setCover(this.cdnPath + item.getCover());
            houseDtos.add(houseDto);
        });

        String sqlCount = "select count(*) from house ";
        Integer houseSize = DataProcessUtil.getOneCount(sqlCount);
        return new BaseServiceResponse<HouseDto>(houseSize, houseDtos);
    }


    /**
     * user用户房屋查询
     *
     * @param webRentSearch
     * @return
     */
    @Override
    public BaseServiceResponse<HouseDto> userQuery(RentSearch webRentSearch) {
        //keyWord就是es查询
        if (webRentSearch.getKeywords() != null && !webRentSearch.getKeywords().isEmpty()) {
            BaseServiceResponse<Long> esServiceResponse = searchService.userEsQuery(webRentSearch);
            if (esServiceResponse.getTotal() == 0) {
                return new BaseServiceResponse(0, new ArrayList<HouseDto>());
            }
            return new BaseServiceResponse(esServiceResponse.getTotal(), wrapperHouseResults(esServiceResponse.getResult()));
        }
        //去mysql里面简单查询
        return simpleQuery(webRentSearch);
    }

    @Override
    public List<House> findHouseByHouseIds(List<Long> houseIds) {
        return houseMapper.findHouseByHouseIds(houseIds);
    }

    /**
     * 全地图查询
     *
     * @param mapSearch
     * @return
     */
    @Override
    public BaseServiceResponse<HouseDto> wholeMapQuery(MapSearch mapSearch) {

        BaseServiceResponse<Long> serviceResult = searchService.mapQuery(mapSearch.getCityEnName(), mapSearch.getOrderBy(), mapSearch.getOrderDirection(), mapSearch.getStart(), mapSearch.getSize());
        if (serviceResult.getTotal() == 0) {
            return new BaseServiceResponse<>(0, new ArrayList<>());
        }
        //根据房屋id查询出房屋的信息=house+detail
        List<HouseDto> houses = wrapperHouseResults(serviceResult.getResult());
        return new BaseServiceResponse<>(serviceResult.getTotal(), houses);

    }

    /**
     * 边界查询
     *
     * @param mapSearch
     * @return
     */
    @Override
    public BaseServiceResponse<HouseDto> boundMapQuery(MapSearch mapSearch) {

        BaseServiceResponse<Long> serviceResult = searchService.mapBoundQuery(mapSearch);
        if (serviceResult.getTotal() == 0) {
            return new BaseServiceResponse<>(0, new ArrayList<>());
        }
        List<HouseDto> houses = wrapperHouseResults(serviceResult.getResult());
        return new BaseServiceResponse<>(serviceResult.getTotal(), houses);


    }

    /**
     * 加入待看清单
     *
     * @param houseId
     * @return
     */
    @Override
    public ServiceResponse addSubscribeOrder(Long houseId) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = houseSubscribeService.getByHouseIdAndUserId(houseId, userId);
        if (subscribe != null) {
            return new ServiceResponse(false, "已加入预约");
        }

        House house = this.getHouseById(houseId);
        if (house == null) {
            return new ServiceResponse(false, "查无此房");
        }

        subscribe = new HouseSubscribe();
        Date now = new Date();
        subscribe.setCreateTime(now);
        subscribe.setLastUpdateTime(now);
        subscribe.setUserId(userId);
        subscribe.setStatus(1);
        subscribe.setHouseId(houseId);
        subscribe.setStatus(HouseSubscribeStatus.IN_ORDER_LIST.getValue());
        subscribe.setAdminId(house.getAdminId());
        houseSubscribeService.addHouseSubscribe(subscribe);
        return new ServiceResponse(true);

    }

    /**
     * 分页查询于于看房的列表
     * 获取对应状态的预约列表
     *
     * @param status
     * @param start
     * @param size
     * @return
     */
    @Override
    public BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> querySubscribeList(HouseSubscribeStatus status, int start, int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        int pageNum = start / size;
        PageHelper.startPage(pageNum + 1, size);
        List<HouseSubscribe> houseSubscribes = houseSubscribeService.findAllByHouseIdAndUserId(userId, status.getValue());
        String sql = "select count(*) from house_subscribe where user_id = " + userId + " and status = " + status.getValue();
        Integer userSubscribeCount = DataProcessUtil.getOneCount(sql);

        return wrapper(userSubscribeCount, houseSubscribes);
    }


    private BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> wrapper(Integer userSubscribeCount, List<HouseSubscribe> houseSubscribes) {
        List<Pair<HouseDto, HouseSubscribeDto>> result = new ArrayList<>();

        if (userSubscribeCount < 1) {
            return new BaseServiceResponse<>(0, result);
        }
        List<HouseSubscribeDto> subscribeDtos = new ArrayList<>();

        List<Long> houseIds = new ArrayList<>();
        houseSubscribes.forEach(houseSubscribe -> {
            subscribeDtos.add(modelMapper.map(houseSubscribe, HouseSubscribeDto.class));
            houseIds.add(houseSubscribe.getHouseId());
        });

        Map<Long, HouseDto> idToHouseMap = new HashMap<>();
        Iterable<House> houses = this.findHouseByHouseIds(houseIds);
        houses.forEach(house -> {
            idToHouseMap.put(house.getId(), modelMapper.map(house, HouseDto.class));
        });
        for (HouseSubscribeDto subscribeDTO : subscribeDtos) {
            Pair<HouseDto, HouseSubscribeDto> pair = Pair.of(idToHouseMap.get(subscribeDTO.getHouseId()), subscribeDTO);
            result.add(pair);
        }
        return new BaseServiceResponse<>(userSubscribeCount, result);
    }

    /**
     * 用户预约看房
     *
     * @param houseId
     * @param orderTime
     * @param telephone
     * @param desc
     * @return
     */
    @Override
    public ServiceResponse subscribe(Long houseId, Date orderTime, String telephone, String desc) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = houseSubscribeService.getByHouseIdAndUserId(houseId, userId);
        if (subscribe == null) {
            return new ServiceResponse(false, "无预约记录");
        }

        if (subscribe.getStatus() != HouseSubscribeStatus.IN_ORDER_LIST.getValue()) {
            return new ServiceResponse(false, "无法预约");
        }

        subscribe.setStatus(HouseSubscribeStatus.IN_ORDER_TIME.getValue());
        subscribe.setLastUpdateTime(new Date());
        subscribe.setTelephone(telephone);
        subscribe.setDesc(desc);
        subscribe.setOrderTime(orderTime);
        houseSubscribeService.updateHouseSubscribe(subscribe);
        return new ServiceResponse(true);
    }

    /**
     * 取消预约
     *
     * @param houseId
     * @return
     */
    @Override
    public ServiceResponse cancelSubscribe(Long houseId) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = houseSubscribeService.getByHouseIdAndUserId(houseId, userId);
        if (subscribe == null) {
            return new ServiceResponse(false, "无预约记录");
        }

        houseSubscribeService.delete(subscribe.getId());
        return new ServiceResponse(true);
    }

    /**
     * 管理员查看用户预约记录
     * 分页查询
     *
     * @param start
     * @param size
     * @return
     */
    @Override
    public BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> adminFindSubscribeList(int start, int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        int pageNum = start / size;
        PageHelper.startPage(pageNum + 1, size);
        List<HouseSubscribe> subscribes = houseSubscribeService.findAllByAdminIdAndStatus(userId, HouseSubscribeStatus.IN_ORDER_TIME.getValue());
        String sql = "select count(*) from house_subscribe where admin_id = " + userId + " and status = " + HouseSubscribeStatus.IN_ORDER_TIME.getValue();
        Integer userSubscribeCount = DataProcessUtil.getOneCount(sql);
        return wrapper(userSubscribeCount, subscribes);
    }

    /**
     * 管理员完成预约
     *
     * @param houseId
     * @return
     */
    @Override
    @Transactional
    public ServiceResponse finishSubscribe(Long houseId) {
        Long adminId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = houseSubscribeService.getByHouseIdAndAdminId(houseId, adminId);
        if (subscribe == null) {
            return new ServiceResponse(false, "无预约记录");
        }
        subscribe.setStatus(HouseSubscribeStatus.FINISH.getValue());
        houseSubscribeService.updateHouseSubscribe(subscribe);
        this.updateWatchTimes(houseId);
        return new ServiceResponse(true);
    }

    /**
     * 更观看次数
     *
     * @param houseId
     */
    @Override
    public void updateWatchTimes(Long houseId) {
        houseMapper.updateWatchTimes(houseId);
    }


    /**
     * 查询房屋详情=detail+tag+pictures
     *
     * @param id
     * @return
     */
    @Override
    public ServiceResponse<HouseDto> findCompleteOne(Long id) {

        House house = houseMapper.getHouseById(id);
        if (house == null) {
            return new ServiceResponse(false, "not found", null);
        }
        HouseDto houseDto = modelMapper.map(house, HouseDto.class);

        HouseDetail houseDetail = houseDetailService.getHouseById(id);
        HouseDetailDto houseDetailDto = modelMapper.map(houseDetail, HouseDetailDto.class);


        List<HousePicture> housePictures = housePictureService.getHousePictureByHouseId(id);
        List<HousePictureDto> pictureDtos = new ArrayList<>();
        housePictures.forEach(item -> {
            HousePictureDto housePictureDto = modelMapper.map(item, HousePictureDto.class);
            pictureDtos.add(housePictureDto);
        });

        List<HouseTag> houseTags = houseTagService.getHouseTagByHouseId(id);
        List<String> tags = new ArrayList<>();
        houseTags.forEach(item -> {
            tags.add(item.getName());
        });
        houseDto.setHouseDetail(houseDetailDto);
        houseDto.setPictures(pictureDtos);
        houseDto.setTags(tags);

        if (LoginUserUtil.getLoginUserId() > 0) { // 已登录用户
            HouseSubscribe subscribe = houseSubscribeService.getByHouseIdAndUserId(house.getId(), LoginUserUtil.getLoginUserId());
            if (subscribe != null) {
                houseDto.setSubscribeStatus(subscribe.getStatus());
            } else {
                houseDto.setSubscribeStatus(0);
            }
        }
        return new ServiceResponse<>(true, null, houseDto);
    }

    /**
     * 修改房屋
     *
     * @param houseForm
     * @return
     */
    @Override
    @Transactional
    public ServiceResponse update(HouseForm houseForm) {
        House house = houseMapper.getHouseById(houseForm.getId());
        if (house == null) {
            throw new HouseException("房屋不存在");
        }
        HouseDetail detail = houseDetailService.getHouseById(house.getId());
        if (detail == null) {
            throw new HouseException("房屋详情不存在");
        }
        //修改房屋详情
        ServiceResponse<HouseDetail> houseDetailServiceResponse = wrapperDetailInfo(houseForm);
        HouseDetail result = houseDetailServiceResponse.getResult();
        result.setId(detail.getId());
        houseDetailService.update(result);
        //房屋图片添加
        if (houseForm.getPhotos() != null) {
            List<HousePicture> housePictures = generatePictures(houseForm, houseForm.getId());
            housePictureService.savePictures(housePictures);
        }
        if (houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }
        //修改房屋
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseMapper.update(house);
        //房屋处于上架状态
        if (house.getStatus() == 1) {
            searchService.index(house.getId());
        }

        return new ServiceResponse(true, null, null);
    }

    /**
     * 修改封面
     *
     * @param coverId
     * @param targetId
     * @return
     */
    @Override
    public ServiceResponse updateCover(Long coverId, Long targetId) {
        houseMapper.updateCover(coverId, targetId);
        return new ServiceResponse(true, null, null);
    }

    /**
     * 一条house
     *
     * @param id
     * @return
     */
    @Override
    public House getHouseById(long id) {
        return houseMapper.getHouseById(id);
    }

    /**
     * 修改房屋状态-审核
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public ServiceResponse updateStatus(Long id, int status) {
        House house = houseMapper.getHouseById(id);
        if (house == null) {
            throw new HouseException("房屋不存在");
        }
        if (house.getStatus() == status) {
            return new ServiceResponse(false, "状态没有发生变化", null);
        }

        if (house.getStatus() == 2) {
            return new ServiceResponse(false, "已出租的房源不允许修改状态", null);
        }

        if (house.getStatus() == 3) {
            return new ServiceResponse(false, "已删除的资源不允许操作", null);
        }
        houseMapper.updateStatus(id, status);
        //上架更新索引，其他情况删除索引
        if (status == 1) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }


        return new ServiceResponse(true, null, null);
    }


    /**
     * 简单查询
     *
     * @param webRentSearch
     * @return
     */
    private BaseServiceResponse<HouseDto> simpleQuery(RentSearch webRentSearch) {
        RentSearch rentSearch = validateSearchParam(webRentSearch);
        List<HouseDto> houseDtos = new ArrayList<>();
        int pageNum = rentSearch.getStart() / rentSearch.getSize();
        PageHelper.startPage(pageNum + 1, rentSearch.getSize());
        //查询所有的房屋列表
        List<House> houses = houseMapper.userFindAllHousesBySearch(rentSearch);
        if (houses.size() == 0) {
            return new BaseServiceResponse(0, new ArrayList());
        }
        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDto> idToHouseMap = Maps.newHashMap();
        houses.forEach(item -> {
            HouseDto houseDto = modelMapper.map(item, HouseDto.class);
            houseDto.setCover(this.cdnPath + item.getCover());
            houseDtos.add(houseDto);
            houseIds.add(item.getId());
            idToHouseMap.put(item.getId(), houseDto);
        });
        String sqlCount = "select count(*) from house ";
        Integer houseSize = DataProcessUtil.getOneCount(sqlCount);
        /*包装房屋细节*/
        wrapperHouseDetailList(houseIds, idToHouseMap);
        return new BaseServiceResponse<>(houseSize, houseDtos);
    }


    /**
     * 图片对象列表信息填充
     *
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }
        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPath);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setLocation(photoForm.getLocation());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    /**
     * 房源详细信息对象填充
     *
     * @param houseForm
     * @return
     */
    private ServiceResponse<HouseDetail> wrapperDetailInfo(HouseForm houseForm) {
        HouseDetail houseDetail = new HouseDetail();
        Subway subway = subwayService.getSubwayById(houseForm.getSubwayLineId());
        if (subway == null) {
            return new ServiceResponse<>(false, "Not valid subway line!", null);
        }
        SubwayStation subwayStation = subwayStationService.getSubwayStationById(houseForm.getSubwayStationId());
        if (subwayStation == null) {
            return new ServiceResponse<>(false, "Not valid subway station!", null);
        }
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());
        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());
        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return new ServiceResponse<>(true, null, houseDetail);

    }

    /**
     * 包装房屋细节=deratil+tags
     *
     * @param houseIds
     * @param idToHouseMap
     */
    private void wrapperHouseDetailList(List<Long> houseIds, Map<Long, HouseDto> idToHouseMap) {
        //根据房屋ids查询出所有的detail
        List<HouseDetail> details = houseDetailService.findAllByHouseIdIn(houseIds);
        if (details != null) {
            details.forEach(houseDetail -> {
                HouseDto houseDTO = idToHouseMap.get(houseDetail.getHouseId());
                HouseDetailDto detailDTO = modelMapper.map(houseDetail, HouseDetailDto.class);
                houseDTO.setHouseDetail(detailDTO);
            });
        }
        List<HouseTag> houseTags = houseTagService.findAllByHouseIdIn(houseIds);
        if (houseTags != null) {
            houseTags.forEach(houseTag -> {
                List<String> tags = new ArrayList<>();
                HouseDto house = idToHouseMap.get(houseTag.getHouseId());
                tags.add(houseTag.getName());
                house.setTags(tags);
            });
        }
    }

    /**
     * 房屋标签封装
     *
     * @param houseId
     * @param houseForm
     * @return
     */
    private List<String> addHouseTags(long houseId, HouseForm houseForm) {
        List<String> tags = houseForm.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(null, houseId, tag));
            }
            houseTagService.save(houseTags);
        }
        return tags;
    }

    /**
     * 根据es查询出来的houseIds查询House信息=detail+tags
     *
     * @param houseIds
     * @return
     */
    private List<HouseDto> wrapperHouseResults(List<Long> houseIds) {
        List<HouseDto> houseDtos = Lists.newArrayList();
        List<House> houses = findHouseByHouseIds(houseIds);
        Map<Long, HouseDto> idToHouseMap = new HashMap<>();
        houses.forEach(item -> {
            HouseDto houseDto = modelMapper.map(item, HouseDto.class);
            houseDto.setCover(cdnPath + item.getCover());
            idToHouseMap.put(item.getId(), houseDto);
        });
        //包装房屋detail和tag
        wrapperHouseDetailList(houseIds, idToHouseMap);
        for (Long houseId : houseIds) {
            houseDtos.add(idToHouseMap.get(houseId));
        }
        return houseDtos;
    }


    /**
     * 改变传参==传参与数据库字段不一致的情况
     *
     * @param rentSearch
     * @return
     */
    private RentSearch validateSearchParam(RentSearch rentSearch) {
        //用于排序的字段
        String orderBy = rentSearch.getOrderBy();
        //升序还是降序
        String orderDirection = rentSearch.getOrderDirection();

        //默认是last_update_time进行排序
        if (rentSearch.getOrderBy() == null) {
            rentSearch.setOrderBy("last_update_time");
        }

        if (rentSearch.getOrderBy() != null && rentSearch.getOrderBy().equals("lastUpdateTime")) {
            rentSearch.setOrderBy("last_update_time");
        } else if (rentSearch.getOrderBy() != null && rentSearch.getOrderBy().equals("distanceToSubway")) {
            rentSearch.setOrderBy("distance_to_subway");
        } else if (rentSearch.getOrderBy() != null && rentSearch.getOrderBy().equals("createTime")) {
            rentSearch.setOrderBy("create_time");
        }

        if (rentSearch.getRegionEnName() != null) {
            if (rentSearch.getRegionEnName().equals("*")) {
                rentSearch.setRegionEnName(null);
            }
        }
        if (rentSearch.getAreaBlock() != null) {
            if (rentSearch.getAreaBlock().equals("*")) {
                rentSearch.setAreaBlock(null);
            }
        }
        if (rentSearch.getPriceBlock() != null) {
            if (rentSearch.getPriceBlock().equals("*")) {
                rentSearch.setPriceBlock("null");
            }
        }
        if (rentSearch.getAreaBlock() != null) {
            if (rentSearch.getAreaBlock().equals("*-30")) {
                rentSearch.setAreaKey(1);
            } else if (rentSearch.getAreaBlock().equals("30-50")) {
                rentSearch.setAreaKey(2);
            } else if (rentSearch.getAreaBlock().equals("50-*")) {
                rentSearch.setAreaKey(3);
            }
        }

        if (rentSearch.getPriceBlock() != null) {
            if (rentSearch.getPriceBlock().equals("*-1000")) {
                rentSearch.setPriceKey(1);
            } else if (rentSearch.getPriceBlock().equals("1000-3000")) {
                rentSearch.setPriceKey(2);
            } else if (rentSearch.getPriceBlock().equals("3000-*")) {
                rentSearch.setPriceKey(3);
            }
        }
        return rentSearch;
    }

}
