package com.hww.house.config.esconfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/12
 * @Time: 14:57
 * Description:
 */
@Slf4j
@Configuration
public class EsConfig {


    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    /**
     * 初始化RestHighLevelClient
     *
     * @return
     */
    @Bean
    public RestHighLevelClient client() {

        log.info("[-------------------init the es's client..............]");
        //初始化es客户端
        RestHighLevelClient client = new RestHighLevelClient(
                //这里如果要用client去访问其他节点，就添加进去
                RestClient.builder(new HttpHost(host, port, "http"))
        );
        log.info("[----------------es's host is : " + host + ",port is ： " + port+"------------------]");
        return client;

    }
}
