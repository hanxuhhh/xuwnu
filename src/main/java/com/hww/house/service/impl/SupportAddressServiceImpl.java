package com.hww.house.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.hww.house.base.bmap.BaiDuMapLocation;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.base.enums.Level;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.SupportAddressDto;
import com.hww.house.entity.Subway;
import com.hww.house.entity.SubwayStation;
import com.hww.house.entity.SupportAddress;
import com.hww.house.mapper.SupportAddressMapper;
import com.hww.house.service.SubwayService;
import com.hww.house.service.SubwayStationService;
import com.hww.house.service.SupportAddressService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 14:55
 * Description:
 */
@SuppressWarnings("ALL")
@Service
@Slf4j
public class SupportAddressServiceImpl implements SupportAddressService {

    @Autowired
    private SupportAddressMapper supportAddressMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SubwayService subwayService;

    @Autowired
    private SubwayStationService subwayStationService;

    @Value("${baidu_map_key}")
    private String BAIDU_MAP_KEY;

    //根据城市查询坐标
    private static final String BAIDU_MAP_GEOCONV_API = "http://api.map.baidu.com/geocoding/v3/?";
    /**
     * POI数据管理接口
     */
    //创建数据（create poi）接口
    private static final String CREATEPOI = "http://api.map.baidu.com/geodata/v3/poi/create";
    //查询指定条件的数据（poi）列表接口
    private static final String LISTPOI = "http://api.map.baidu.com/geodata/v3/poi/list?";
    //查询指定id的数据（poi）详情接口
    private static final String DETAILPOI = "http://api.map.baidu.com/geodata/v3/poi/detail?";
    //修改数据（poi）接口
    private static final String UPDATEPOI = "http://api.map.baidu.com/geodata/v3/poi/update";
    //删除数据（poi）接口（支持批量）
    private static final String DELETEPOI = "http://api.map.baidu.com/geodata/v3/poi/delete";

    //记录关联的geotable的标识
    private static final String GEOTABLE_ID = "208063";


    /**
     * 城市列表
     *
     * @return
     */
    @Override
    public BaseServiceResponse<SupportAddressDto> findAll() {
        List<SupportAddress> supportAddresss = supportAddressMapper.findAllByLevel(Level.CITY.getValue());
        List<SupportAddressDto> supportAddressDtos = new ArrayList<>();
        supportAddresss.forEach(item -> {
            SupportAddressDto target = modelMapper.map(item, SupportAddressDto.class);
            supportAddressDtos.add(target);
        });
        return new BaseServiceResponse<>(supportAddressDtos.size(), supportAddressDtos);
    }

    @Override
    public SupportAddress getByEnNameAndLevel(String cityName) {
        return supportAddressMapper.getByEnNameAndLevel(cityName, Level.CITY.getValue());
    }

    /**
     * 根据ename和level查询一条记录
     *
     * @param levelName
     * @param level
     * @return
     */
    @Override
    public SupportAddress getSupportAddress(String levelName, String level) {
        return supportAddressMapper.getSupportAddress(levelName, level);
    }

    @Override
    public SupportAddress getByEnNameAndBelongTo(String regionName, String belongTo) {
        return supportAddressMapper.getByEnNameAndBelongTo(regionName, belongTo);
    }


    /**
     * 区域列表
     *
     * @param cityEnName
     * @return
     */
    @Override
    public BaseServiceResponse<SupportAddressDto> findAllRegionsByCityName(String cityEnName) {

        if (cityEnName == null) {
            return new BaseServiceResponse<>(0, null);
        }
        List<SupportAddress> regions = supportAddressMapper.findAllByCity(Level.REGION.getValue(), cityEnName);
        List<SupportAddressDto> supportAddressDtos = Lists.newArrayList();
        regions.forEach(item -> {
            SupportAddressDto target = modelMapper.map(item, SupportAddressDto.class);
            supportAddressDtos.add(target);
        });
        return new BaseServiceResponse<>(supportAddressDtos.size(), supportAddressDtos);
    }

    @Override
    public BaseServiceResponse<Subway> findAllSubwayByCity(String cityEnName) {
        if (cityEnName == null) {
            return new BaseServiceResponse<>(0, null);
        }
        List<Subway> subwayList = subwayService.findAllSubwayByCity(cityEnName);
        if (subwayList == null || subwayList.size() < 1) {
            return new BaseServiceResponse<>(0, null);
        }
        return new BaseServiceResponse<>(subwayList.size(), subwayList);
    }

    @Override
    public List<SubwayStation> findAllStationBySubway(Long subwayId) {
        return subwayStationService.findAllStationBySubway(subwayId);
    }

    /**
     * 根据城市以及具体地址查询经纬度
     *
     * @param city
     * @param detailAddress
     * @return
     */
    @Override
    public ServiceResponse<BaiDuMapLocation> getBaiDuMapLocationByCity(String city, String detailAddress) {
        String addressEncode = null;
        String cityEncode = null;
        try {
            addressEncode = URLEncoder.encode(detailAddress, "UTF-8");
            cityEncode = URLEncoder.encode(city, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ServiceResponse<>(false, "解码错误", null);
        }
        HttpClient httpClient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(BAIDU_MAP_GEOCONV_API);
        sb.append("address=").append(addressEncode).append("&")
                .append("city=").append(cityEncode).append("&")
                .append("output=json&")
                .append("ak=").append(BAIDU_MAP_KEY);
        HttpGet get = new HttpGet(sb.toString());
        try {
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return new ServiceResponse<BaiDuMapLocation>(false, "Can not get baidu map location", null);
            }
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject baiDuResponse = (JSONObject) JSONObject.parse(result);
            int status = Integer.parseInt(baiDuResponse.get("status").toString());
            if (status != 0) {
                return new ServiceResponse<>(false, "解码错误", null);
            }

            BaiDuMapLocation location = new BaiDuMapLocation();
            JSONObject resultObject = (JSONObject) JSONObject.parse(baiDuResponse.get("result").toString());
            JSONObject locationObject = (JSONObject) resultObject.get("location");
            location.setLon(Double.parseDouble(locationObject.get("lng").toString()));
            location.setLat(Double.parseDouble(locationObject.get("lat").toString()));
            return new ServiceResponse(true, null, location);
        } catch (IOException e) {
            e.printStackTrace();
            return new ServiceResponse<>(false, "Error to fetch baidumap api", null);
        }
    }

    /**
     * 上传至lbs
     *
     * @param location
     * @param area
     * @param title
     * @param price
     * @param address
     * @param houseId
     * @return
     */
    @Override
    public ServiceResponse lbsUpload(BaiDuMapLocation location, int area, String title, int price, String address, Long houseId,String tags,String image) {
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("latitude", String.valueOf(location.getLat())));
        params.add(new BasicNameValuePair("longitude", String.valueOf(location.getLon())));
        //用户上传的坐标的类型
        params.add(new BasicNameValuePair("coord_type", "3")); // 百度坐标系
        params.add(new BasicNameValuePair("geotable_id", GEOTABLE_ID));
        params.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));
        params.add(new BasicNameValuePair("houseId", String.valueOf(houseId)));
        params.add(new BasicNameValuePair("price", String.valueOf(price)));
        params.add(new BasicNameValuePair("area", String.valueOf(area)));
        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("address", address));
        params.add(new BasicNameValuePair("tags", tags));
        params.add(new BasicNameValuePair("image", image));
        HttpPost post;
        if (isLbsDataExists(houseId)) {
            post = new HttpPost(UPDATEPOI);
        } else {
            post = new HttpPost(CREATEPOI);
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("Can not upload lbs data for response: " + result);
                return new ServiceResponse(false, "Can not upload baidu lbs data", null);
            } else {
                JSONObject responseObject = (JSONObject) JSONObject.parse(result);
                int status = Ints.tryParse(responseObject.get("status").toString());
                if (status != 0) {
                    String message = responseObject.get("message").toString();
                    log.error("Error to upload lbs data for status: {}, and message: {}", status, message);
                    return new ServiceResponse(false, "Error to upload lbs data", null);
                } else {
                    return new ServiceResponse(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServiceResponse(false);
    }

    /**
     * 判断poi数据是否已经上传
     *
     * @param houseId
     * @return
     */
    private boolean isLbsDataExists(Long houseId) {
        HttpClient httpClient = HttpClients.createDefault();
        StringBuilder sb = new StringBuilder(LISTPOI);
        sb.append("geotable_id=").append(GEOTABLE_ID).append("&")
                .append("ak=").append(BAIDU_MAP_KEY).append("&")
                .append("houseId=").append(houseId).append(",").append(houseId);
        HttpGet get = new HttpGet(sb.toString());
        try {

            HttpResponse response = httpClient.execute(get);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("Can not get lbs data for response: " + result);
                return false;
            }
            JSONObject responseObject = (JSONObject) JSONObject.parse(result);
            int status = Ints.tryParse(responseObject.get("status").toString());
            if (status != 0) {
                log.error("Error to get lbs data for status: " + status);
                return false;
            } else {
                long size = Longs.tryParse(responseObject.get("size").toString());
                if (size > 0) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从lbs移除数据
     *
     * @param houseId
     * @return
     */
    @Override
    public ServiceResponse removeLbs(Long houseId) {
        HttpClient httpClient = HttpClients.createDefault();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("geotable_id", GEOTABLE_ID));
        params.add(new BasicNameValuePair("ak", BAIDU_MAP_KEY));
        params.add(new BasicNameValuePair("houseId", String.valueOf(houseId)));

        HttpPost delete = new HttpPost(DELETEPOI);
        try {
            delete.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse response = httpClient.execute(delete);
            String result = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("Error to delete lbs data for response: " + result);
                return new ServiceResponse(false);
            }

            JSONObject responseObject = (JSONObject) JSONObject.parse(result);
            int status = Ints.tryParse(responseObject.get("status").toString());
            if (status != 0) {
                String message = responseObject.get("message").toString();
                log.error("Error to delete lbs data for message: " + message);
                return new ServiceResponse(false, "Error to delete lbs data for: " + message);
            }
            return new ServiceResponse(true);
        } catch (IOException e) {
            log.error("Error to delete lbs data.", e);
            return new ServiceResponse(false);
        }
    }
}
