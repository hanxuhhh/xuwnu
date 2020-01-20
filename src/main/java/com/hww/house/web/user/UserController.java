package com.hww.house.web.user;

import com.hww.house.base.enums.HouseSubscribeStatus;
import com.hww.house.base.service.response.AppResponse;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseDto;
import com.hww.house.dto.HouseSubscribeDto;
import com.hww.house.service.HouseService;
import com.hww.house.service.UserService;
import com.hww.house.util.LoginUserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 10:02
 * Description:
 */
@Controller
public class UserController {


    @Autowired
    private UserService userService;

    @Autowired

    private HouseService houseService;

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/center")
    public String centerPage() {
        return "user/center";
    }

    /**
     * 修改个人信息
     *
     * @param profile
     * @param value
     * @return
     */
    @PostMapping(value = "api/user/info")
    @ResponseBody
    public AppResponse updateUserInfo(@RequestParam(value = "profile") String profile,
                                      @RequestParam(value = "value") String value) {
        if (StringUtils.isEmpty(profile)) {
            return AppResponse.requestError("修改类型为空");
        }

        if ("email".equals(profile) && !LoginUserUtil.checkEmail(value)) {
            return AppResponse.requestError("不支持的邮箱格式");
        }

        ServiceResponse result = userService.updateUser(profile, value);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess("");
        } else {
            return AppResponse.requestError("修改出错");
        }

    }

    /**
     * 加入待看清单
     *
     * @param houseId
     * @return
     */
    @PostMapping(value = "api/user/house/subscribe")
    @ResponseBody
    public AppResponse subscribeHouse(@RequestParam(value = "house_id") Long houseId) {
        ServiceResponse result = houseService.addSubscribeOrder(houseId);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess(null);
        } else {
            return AppResponse.requestError("预约失败");
        }
    }


    /**
     * 用户待看清单
     * 配对(Pair)。配对提供了一种方便方式来处理简单的键值关联，当我们想从方法返回两个值时特别有用。
     *
     * @param start
     * @param size
     * @param status
     * @return
     */
    @GetMapping(value = "api/user/house/subscribe/list")
    @ResponseBody
    public AppResponse subscribeList(
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "size", defaultValue = "3") int size,
            @RequestParam(value = "status") int status) {

        BaseServiceResponse<Pair<HouseDto, HouseSubscribeDto>> result = houseService.querySubscribeList(HouseSubscribeStatus.getStatus(status), start, size);

        if (result.getResultSize() == 0) {
            return AppResponse.requestSuccess(result.getResult());
        }

        AppResponse response = AppResponse.requestSuccess(result.getResult());
        response.setMore(result.getTotal() > (start + size));
        return response;
    }

    /**
     * 预约看房
     *
     * @param houseId
     * @param orderTime
     * @param desc
     * @param telephone
     * @return
     */
    @PostMapping(value = "api/user/house/subscribe/date")
    @ResponseBody
    public AppResponse subscribeDate(
            @RequestParam(value = "houseId") Long houseId,
            @RequestParam(value = "orderTime") @DateTimeFormat(pattern = "yyyy-MM-dd") Date orderTime,
            @RequestParam(value = "desc", required = false) String desc,
            @RequestParam(value = "telephone") String telephone
    ) {
        if (orderTime == null) {
            return AppResponse.requestError("请选择预约时间");
        }

        if (!LoginUserUtil.checkTelephone(telephone)) {
            return AppResponse.requestError("手机格式不正确");
        }

        ServiceResponse serviceResult = houseService.subscribe(houseId, orderTime, telephone, desc);
        if (serviceResult.isSuccess()) {
            return AppResponse.requestSuccess("预约成功");
        } else {
            return AppResponse.requestError(serviceResult.getMessage());
        }
    }

    @DeleteMapping(value = "api/user/house/subscribe")
    @ResponseBody
    public AppResponse cancelSubscribe(@RequestParam(value = "houseId") Long houseId) {
        ServiceResponse serviceResult = houseService.cancelSubscribe(houseId);
        if (serviceResult.isSuccess()) {
            return AppResponse.requestSuccess("取消预约成功");
        } else {
            return AppResponse.requestError("取消预约失败");
        }
    }

}
