package com.hww.house.test;

import com.hww.house.HouseElkApplicationTests;
import com.hww.house.service.QiNiuService;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/9
 * @Time: 15:49
 * Description:
 */
public class QinNiuServiceTest extends HouseElkApplicationTests {

    @Autowired
    private QiNiuService qiNiuService;
    @Test
    @Override
    public void test() {
        File file = new File("G:/images/1.jpg");
        try {
            Response response = qiNiuService.uploadFile(file);
            String info = response.getInfo();
            System.out.println(info);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
    }
}
