package com.hww.house.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.service.MessageService;
import com.hww.house.util.RedisService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/26
 * @Time: 15:59
 * Description:
 */
@Service
public class MessageServiceImpl implements MessageService, InitializingBean {


    private static final String[] NUMS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private final static String SMS_CODE_CONTENT_PREFIX = "SMS::CODE::CONTENT::";

    private static final Random random = new Random();

    @Value("${aliyun.sms.accessKey}")
    private String accessKey;

    @Value("${aliyun.sms.accessKeySecret}")
    private String secertKey;

    @Value("${aliyun.sms.template.code}")
    private String templateCode;

    @Value("${aliyun.sms.signname}")
    private String signName;

    @Autowired
    private RedisService redisService;

    //阿里云短信服务客户端
    private IAcsClient client;

    /**
     * 发送校验码
     *
     * @param telephone
     * @return
     */
    @Override
    public ServiceResponse<String> sendMessage(String telephone) {
        String gapKey = "SMS::CODE::INTERVAL::" + telephone;

        Object codeObject = redisService.get(gapKey);
        if (codeObject != null) {
                return new ServiceResponse<>(false, "请求次数太频繁");
        }
        //校验码
        String code = generateRandomSmsCode();
        String templateParam = String.format("{\"code\": \"%s\"}", code);
        // 组装请求对象
        CommonRequest request = new CommonRequest();
        // 使用post提交
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", telephone);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", templateParam);
        boolean success = false;
        try {
            CommonResponse response = client.getCommonResponse(request);
            if (200==(response.getHttpStatus())) {
                success = true;
            } else {
                // TODO log this question
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }
        if (success) {
            redisService.set(gapKey, code, 60, TimeUnit.SECONDS);
            redisService.set(SMS_CODE_CONTENT_PREFIX + telephone, code, 10, TimeUnit.MINUTES);
            return new ServiceResponse<String>(true,code);
        } else {
            return new ServiceResponse<String>(false, "服务忙，请稍后重试");
        }
    }


    /**
     * 取出key
     *
     * @param telehone
     * @return
     */
    @Override
    public String getMessage(String telehone) {
        return redisService.get(SMS_CODE_CONTENT_PREFIX + telehone).toString();
    }


    /**
     * 移除key
     *
     * @param telephone
     */
    @Override
    public void remove(String telephone) {
        redisService.del(SMS_CODE_CONTENT_PREFIX + telephone);
    }

    /**
     * 初始化短信发送客户端
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secertKey);
        this.client = new DefaultAcsClient(profile);
    }

    /**
     * 6位验证码生成器
     *
     * @return
     */
    private static String generateRandomSmsCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(10);
            sb.append(NUMS[index]);
        }
        return sb.toString();
    }
}
