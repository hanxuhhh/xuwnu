package com.hww.house.web;

import com.hww.house.base.service.response.AppResponse;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.service.MessageService;
import com.hww.house.util.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/11/29
 * @Time: 15:21
 * Description:
 */
@Controller
public class HomeController {

    @Autowired
    private MessageService messageService;


    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/404")
    public String notFoundPage() {
        return "404";
    }

    @GetMapping("/403")
    public String accessError() {
        return "403";
    }

    @GetMapping("/500")
    public String internalError() {
        return "500";
    }

    @GetMapping("/logout/page")
    public String logoutPage() {
        return "logout";
    }


    @GetMapping(value = "sms/code")
    @ResponseBody
    public AppResponse smsCode(@RequestParam("telephone") String telephone) {
        //正则校验手机号码
        if (!LoginUserUtil.checkTelephone(telephone)) {
            return AppResponse.requestError("请输入正确的手机号");
        }
        ServiceResponse<String> result = messageService.sendMessage(telephone);
        if (result.isSuccess()) {
            return AppResponse.requestSuccess("");
        } else {
            return AppResponse.requestError(result.getMessage());
        }

    }


}
