package com.hww.house.web.admin;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.hww.house.base.enums.Status;
import com.hww.house.base.datatables.ApiDataTableResponse;
import com.hww.house.base.service.response.AppResponse;
import com.hww.house.base.QinNiuResult;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.base.datatables.DatatableSearch;
import com.hww.house.dto.*;
import com.hww.house.entity.Subway;
import com.hww.house.entity.SubwayStation;
import com.hww.house.entity.SupportAddress;
import com.hww.house.exception.HouseException;
import com.hww.house.form.HouseForm;
import com.hww.house.service.*;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.util.RedisService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/5
 * @Time: 19:41
 * Description:
 */
@Controller
public class AdminController {

    @Autowired
    private SupportAddressService supportAddressService;
    @Autowired
    private HouseService houseService;

    @Autowired
    private QiNiuService qiNiuService;

    @Autowired
    private Gson gson;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SupportAddressService addressService;

    @Autowired
    private SubwayService subwayService;

    @Autowired
    private SubwayStationService subwayStationService;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private HousePictureService housePictureService;

    @Autowired
    private HouseTagService houseTagService;

    @Autowired
    private UserService userService;


    /**
     * 后台管理中心
     *
     * @return
     */
    @GetMapping("/admin/center")
    public String adminCenterPage() {
        return "admin/center";
    }

    /**
     * 欢迎页
     *
     * @return
     */
    @GetMapping("/admin/welcome")
    public String welcomePage() {
        return "admin/welcome";
    }

    /**
     * 管理员登录页
     *
     * @return
     */
    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }


    /**
     * 新增房源功能页
     *
     * @return
     */
    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }

    /**
     * 上传图片接口
     * produces：它的作用是指定返回值类型，不但可以设置返回值类型还可以设定返回值的字符编码；
     * consumes： 指定处理请求的提交内容类型（Content-Type），例如application/json, text/html;
     */
    @PostMapping(value = "admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public AppResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        redisService.set("hello", "ok");
        if (file.isEmpty()) {
            return AppResponse.requestError("请上传文件");
        }
        try {
            //以流的上传图片至七牛云服务器
            Response response = qiNiuService.uploadFile(file.getInputStream());
            if (response.isOK()) {
                QinNiuResult result = gson.fromJson(response.bodyString(), QinNiuResult.class);
                return AppResponse.requestSuccess(result);
            } else {
                return AppResponse.requestError(response.getInfo());
            }
        } catch (QiniuException e) {
            Response response = e.response;
            try {
                return AppResponse.requestError(response.bodyString());
            } catch (QiniuException e1) {
                return AppResponse.requestError("上传服务器出错");
            }
        } catch (IOException e) {
            return AppResponse.requestError("上传服务器出错");
        }
    }

    /**
     * 添加房屋
     *
     * @param
     * @return
     * @Valid 验证
     */
    @PostMapping("admin/add/house")
    @ResponseBody
    public AppResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm) {
        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            return AppResponse.requestError("必须上传图片");
        }
        ServiceResponse<HouseDto> response = houseService.save(houseForm);
        if (response.isSuccess()) {
            return AppResponse.requestSuccess(response.getResult());
        }
        return AppResponse.requestError("添加出错");
    }


    /**
     * 房源列表页
     *
     * @return
     */
    @GetMapping("admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }

    /**
     * 查询所有房屋，因为前端使用的是datatables控件，具有规定的数据格式。
     * 所以定制返回的格式
     *
     * @param searchBody
     * @return
     */
    @PostMapping("admin/houses")
    @ResponseBody
    public ApiDataTableResponse houses(@ModelAttribute DatatableSearch searchBody) {

        if (searchBody.getOrderBy().equals("createTime")) {
            searchBody.setOrderBy("create_time");
        } else if (searchBody.getOrderBy().equals("watchTimes")) {
            searchBody.setOrderBy("watch_times");
        }
        BaseServiceResponse<HouseDto> result = houseService.adminQuery(searchBody);
        ApiDataTableResponse response = new ApiDataTableResponse(Status.SUCCESS);
        response.setData(result.getResult());
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        response.setDraw(searchBody.getDraw());
        return response;
    }

    /**
     * 房源信息编辑页
     *
     * @return
     */
    @GetMapping("admin/house/edit")
    public String houseEditPage(@RequestParam(value = "id") Long id, Model model) {
        if (id == null || id < 1) {
            return "404";
        }
        ServiceResponse<HouseDto> serviceResult = houseService.findCompleteOne(id);
        if (!serviceResult.isSuccess()) {
            return "404";
        }
        HouseDto houseDto = serviceResult.getResult();

        String cityEnName = houseDto.getCityEnName();
        String regionEnName = houseDto.getRegionEnName();

        SupportAddress city = supportAddressService.getByEnNameAndLevel(cityEnName);
        SupportAddressDto cityDto = modelMapper.map(city, SupportAddressDto.class);
        SupportAddress region = supportAddressService.getByEnNameAndBelongTo(regionEnName, city.getEnName());
        SupportAddressDto regionDto = modelMapper.map(region, SupportAddressDto.class);

        HouseDetailDto houseDetailDto = houseDto.getHouseDetail();
        /*铁路线*/

        Subway subway = subwayService.getSubwayById(houseDetailDto.getSubwayLineId());

        /*铁路站*/
        SubwayStation subwayStation = subwayStationService.getSubwayStationById(houseDetailDto.getSubwayStationId());


        model.addAttribute("house", houseDto);
        model.addAttribute("city", cityDto);
        model.addAttribute("region", regionDto);
        model.addAttribute("subway", subway);
        model.addAttribute("station", subwayStation);
        return "admin/house-edit";
    }

    /**
     * 编辑接口
     */
    @PostMapping("admin/house/edit")
    @ResponseBody
    public AppResponse saveHouse(@ModelAttribute("form-house-edit") HouseForm houseForm) {
        try {
            ServiceResponse result = houseService.update(houseForm);
            if (result.isSuccess()) {
                return AppResponse.requestSuccess(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new HouseException("更新失败");
    }

    /**
     * 移除图片接口
     *
     * @param id
     * @return
     */
    @DeleteMapping("admin/house/photo")
    @ResponseBody
    public AppResponse removeHousePhoto(@RequestParam(value = "id") Long id) {
        ServiceResponse result = housePictureService.deletePictures(id);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess(null);
        } else {
            return AppResponse.requestError(null);
        }
    }

    /**
     * 修改封面接口
     *
     * @param coverId
     * @param targetId
     * @return
     */
    @PostMapping("admin/house/cover")
    @ResponseBody
    public AppResponse updateCover(@RequestParam(value = "cover_id") Long coverId,
                                   @RequestParam(value = "target_id") Long targetId) {
        ServiceResponse result = houseService.updateCover(coverId, targetId);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess(null);
        } else {
            return AppResponse.requestError(null);
        }
    }

    /**
     * 移除标签接口
     *
     * @param houseId
     * @param tag
     * @return
     */
    @DeleteMapping("admin/house/tag")
    @ResponseBody
    public AppResponse removeHouseTag(@RequestParam(value = "house_id") Long houseId,
                                      @RequestParam(value = "tag") String tag) {
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            throw new HouseException("house_id==null || tag==null");
        }
        ServiceResponse result = houseTagService.deleteTagsByHouseIdAndTag(houseId, tag);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess(null);
        } else {
            return AppResponse.requestError(null);
        }
    }

    /**
     * 增加标签接口
     *
     * @param houseId
     * @param tag
     * @return
     */
    @PostMapping("admin/house/tag")
    @ResponseBody
    public AppResponse addHouseTag(@RequestParam(value = "house_id") Long houseId,
                                   @RequestParam(value = "tag") String tag) {
        if (houseId < 1 || Strings.isNullOrEmpty(tag)) {
            throw new HouseException("house_id==null || tag==null");
        }
        ServiceResponse result = houseTagService.addTag(houseId, tag);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess(null);
        } else {
            return AppResponse.requestError(null);
        }
    }


    /**
     * 审核接口
     *
     * @param id
     * @param operation
     * @return
     */
    @PutMapping("admin/house/operate/{id}/{operation}")
    @ResponseBody
    public AppResponse operateHouse(@PathVariable(value = "id") Long id,
                                    @PathVariable(value = "operation") int operation) {
        if (id <= 0) {
            throw new HouseException("house_id==null ");
        }
        ServiceResponse result;

        switch (operation) {
            case 1:
                result = houseService.updateStatus(id, 1);
                break;
            case 2:
                result = houseService.updateStatus(id, 2);
                break;
            case 3:
                result = houseService.updateStatus(id, 3);
                break;
            case 4:
                result = houseService.updateStatus(id, 4);
                break;
            default:
                return AppResponse.requestError("操作失败");
        }

        if (result.isSuccess()) {
            return AppResponse.requestSuccess(null);
        }
        throw new HouseException("操作失败");
    }

    @GetMapping("admin/house/subscribe")
    public String houseSubscribe() {
        return "admin/subscribe";
    }

    /**
     * @param draw  datatable回显参数
     * @param start
     * @param size
     * @return
     */
    @GetMapping("admin/house/subscribe/list")
    @ResponseBody
    public AppResponse subscribeList(@RequestParam(value = "draw") int draw,
                                     @RequestParam(value = "start") int start,
                                     @RequestParam(value = "length") int size) {
        BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> result = houseService.adminFindSubscribeList(start, size);
        ApiDataTableResponse response = new ApiDataTableResponse(Status.SUCCESS);
        response.setData(result.getResult());
        response.setDraw(draw);
        response.setRecordsFiltered(result.getTotal());
        response.setRecordsTotal(result.getTotal());
        return response;
    }


    @GetMapping("admin/user/{userId}")
    @ResponseBody
    public AppResponse getUserInfo(@PathVariable(value = "userId") Long userId) {
        if (userId == null || userId < 1) {
            throw new HouseException("userId==null");
        }
        ServiceResponse<UserDto> serviceResult = userService.getUserById(userId);
        if (serviceResult.isSuccess()) {
            return AppResponse.requestSuccess(serviceResult.getResult());
        } else {
            return AppResponse.requestError(serviceResult.getMessage());
        }
    }

    @PostMapping("admin/finish/subscribe")
    @ResponseBody
    public AppResponse finishSubscribe(@RequestParam(value = "house_id") Long houseId) {
        if (houseId < 1) {
            return AppResponse.requestError("操作失败");
        }

        ServiceResponse serviceResult = houseService.finishSubscribe(houseId);
        if (serviceResult.isSuccess()) {
            return AppResponse.requestSuccess("");
        } else {
            return AppResponse.requestError(serviceResult.getMessage());
        }
    }



    /**
     * 上传文件到本地
     *
     * @param file
     * @return
     */
    @PostMapping(value = "admin/upload/photo/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public AppResponse uploadPhotoToLocal(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AppResponse.requestError("请上传文件");
        }
        String fileName = file.getOriginalFilename();
        File targetFile = new File(fileName);
        try {
            file.transferTo(targetFile);
            return AppResponse.requestSuccess(null);
        } catch (IOException e) {
            e.printStackTrace();
            return AppResponse.requestError(e.getMessage());
        }
    }
}
