package com.hww.house.web.house;

import com.hww.house.base.service.response.AppResponse;
import com.hww.house.base.bmap.MapSearch;
import com.hww.house.base.es.RentSearch;
import com.hww.house.base.es.RentValueBlock;
import com.hww.house.base.enums.Status;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseBucketDto;
import com.hww.house.dto.HouseDto;
import com.hww.house.dto.SupportAddressDto;
import com.hww.house.dto.UserDto;
import com.hww.house.entity.Subway;
import com.hww.house.entity.SubwayStation;
import com.hww.house.entity.SupportAddress;
import com.hww.house.service.EsSearchService;
import com.hww.house.service.HouseService;
import com.hww.house.service.SupportAddressService;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 14:46
 * Description:
 */
@Controller
public class HouseController {

    @Autowired
    private SupportAddressService addressService;


    @Autowired
    private HouseService houseService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private EsSearchService searchService;


    /**
     * 自动补全接口
     */
    @GetMapping("rent/house/autocomplete")
    @ResponseBody
    public AppResponse autocomplete(@RequestParam(value = "prefix") String prefix) {

        if (prefix.isEmpty()) {
            return AppResponse.requestError(Status.BAD_REQUEST.getStandardMessage());
        }
        ServiceResponse<List<String>> result = searchService.suggest(prefix);
        return AppResponse.requestSuccess(result.getResult());
    }


    /**
     * 获取支持城市列表
     *
     * @return
     */
    @GetMapping("address/support/cities")
    @ResponseBody
    public AppResponse getSupportCities() {
        BaseServiceResponse<SupportAddressDto> result = addressService.findAll();
        if (result.getResultSize() == 0) {
            return AppResponse.requestError("暂无城市列表");
        }
        return AppResponse.requestSuccess(result.getResult());
    }


    /**
     * 获取对应城市支持区域列表
     *
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public AppResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        BaseServiceResponse<SupportAddressDto> areaLists = addressService.findAllRegionsByCityName(cityEnName);
        if (areaLists.getResult() == null || areaLists.getTotal() < 1) {
            return AppResponse.requestError("当前城市暂无区域");
        }
        return AppResponse.requestSuccess(areaLists.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     *
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public AppResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        BaseServiceResponse<Subway> result = addressService.findAllSubwayByCity(cityEnName);
        return AppResponse.requestSuccess(result.getResult());
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     *
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public AppResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        List<SubwayStation> stationList = addressService.findAllStationBySubway(subwayId);
        if (stationList.isEmpty()) {
            return AppResponse.requestError("该铁路线暂无站点");
        }
        return AppResponse.requestSuccess(stationList);
    }


    /**
     * 跳转租房页面
     *
     * @param rentSearch
     * @param model
     * @param session
     * @param redirectAttributes 跳转
     * @return
     */
    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (rentSearch.getCityEnName() == null) {
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }
        SupportAddress city = addressService.getByEnNameAndLevel(rentSearch.getCityEnName());
        model.addAttribute("currentCity", city);
        BaseServiceResponse<SupportAddressDto> addressResult = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        } else {
            model.addAttribute("regions", addressResult.getResult());
        }
        BaseServiceResponse<HouseDto> serviceMultiResult = houseService.userQuery(rentSearch);
        model.addAttribute("total", serviceMultiResult.getTotal());
        model.addAttribute("houses", serviceMultiResult.getResult());

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }
        model.addAttribute("searchBody", rentSearch);
        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);
        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock()));
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));

        return "rent-list";
    }

    /**
     * 房屋详情页面
     *
     * @param houseId
     * @param model
     * @return
     */
    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId, Model model) {
        if (houseId <= 0) {
            return "404";
        }
        ServiceResponse<HouseDto> serviceResult = houseService.findCompleteOne(houseId);
        if (!serviceResult.isSuccess()) {
            return "404";
        }

        HouseDto houseDto = serviceResult.getResult();

        String cityEnName = houseDto.getCityEnName();
        String regionEnName = houseDto.getRegionEnName();
        String district = houseDto.getDistrict();

        SupportAddress city = addressService.getByEnNameAndLevel(cityEnName);
        SupportAddressDto cityDto = modelMapper.map(city, SupportAddressDto.class);
        SupportAddress region = addressService.getByEnNameAndBelongTo(regionEnName, city.getEnName());
        SupportAddressDto regionDto = modelMapper.map(region, SupportAddressDto.class);

        model.addAttribute("city", cityDto);
        model.addAttribute("region", regionDto);
        model.addAttribute("house", houseDto);


        ServiceResponse<UserDto> userDtoServiceResponse = userService.getUserById(houseDto.getAdminId());
        model.addAttribute("agent", userDtoServiceResponse.getResult());
        //设计es的聚合
        ServiceResponse<Long> countResponse = searchService.aggregateDistrictHouse(cityEnName, regionEnName, district);
        model.addAttribute("houseCountInDistrict", countResponse.getResult());
        return "house-detail";
    }

    /**
     * 地图找房
     *
     * @param cityEnName
     * @param model
     * @param session
     * @param redirectAttributes
     * @return
     */
    @GetMapping("rent/house/map")
    public String rentMapPage(@RequestParam(value = "cityEnName") String cityEnName, Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        SupportAddress address = addressService.getByEnNameAndLevel(cityEnName);
        if (address == null) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        } else {
            session.setAttribute("cityName", cityEnName);
            model.addAttribute("city", address);
        }
        BaseServiceResponse<SupportAddressDto> regions = addressService.findAllRegionsByCityName(cityEnName);

        BaseServiceResponse<HouseBucketDto> serviceResult = searchService.aggregateHouseCountByCityEnName(cityEnName);

        model.addAttribute("aggData", serviceResult.getResult());
        model.addAttribute("total", serviceResult.getTotal());
        model.addAttribute("regions", regions.getResult());
        return "rent-map";
    }

    @GetMapping("rent/house/map/houses")
    @ResponseBody
    public AppResponse rentMapHouses(@ModelAttribute MapSearch mapSearch) {
        if (mapSearch.getCityEnName() == null) {
            return AppResponse.requestError( "必须选择城市");
        }
        BaseServiceResponse<HouseDto> serviceResponse;
        if (mapSearch.getLevel() < 13) {
            serviceResponse = houseService.wholeMapQuery(mapSearch);
        } else {
            // 小地图查询必须要传递地图边界参数
            serviceResponse = houseService.boundMapQuery(mapSearch);
        }

        AppResponse response = AppResponse.requestSuccess(serviceResponse.getResult());
        response.setMore(serviceResponse.getTotal() > (mapSearch.getStart() + mapSearch.getSize()));
        return response;
    }


}
