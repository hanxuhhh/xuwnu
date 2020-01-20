package com.hww.house.config.iocconfig;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.gson.Gson;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 16:10
 * Description:
 */
@Configuration
public class SpringIocConfig {


    /**
     * ModelMapper 对象间属性值拷贝
     *
     * @return
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }

    /*******************七牛云**********************************************************/

    @Value("${qiniu.AccessKey}")
    private String accessKey;

    @Value("${qiniu.SecretKey}")
    private String secretKey;


    /**
     * 配置华南机房
     *
     * @return
     */
    @Bean
    public com.qiniu.storage.Configuration configuration() {
        com.qiniu.storage.Configuration cfg = new com.qiniu.storage.Configuration(Region.region2());
        return cfg;
    }

    /**
     * 上传实例
     *
     * @return
     */
    @Bean
    public UploadManager uploadManager() {
        UploadManager uploadManager = new UploadManager(configuration());
        return uploadManager;
    }


    /**
     * 认证信息实例
     *
     * @return
     */
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }


    /**
     * 构建七牛资源空间管理实例
     */
    @Bean
    public BucketManager bucketManager() {
        return new BucketManager(auth(), configuration());
    }

    /**
     * gson
     *
     * @return
     */
    @Bean
    public Gson gson() {
        return new Gson();
    }

}
