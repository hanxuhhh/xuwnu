package com.hww.house.config.redisconfig;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/11/29
 * @Time: 15:21
 * Description: redis配置
 * @EnableConfigurationProperties 相当于把使用 @ConfigurationProperties 的类进行了一次注入
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfigurer {


    @Autowired
    private RedisProperties properties;

    /**
     * redis池
     *
     * @return
     */
    @Bean
    public JedisPoolConfig configureJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(properties.getPool().getMaxIdle());
        jedisPoolConfig.setMinIdle(properties.getPool().getMinIdle());
        return jedisPoolConfig;
    }

    /**
     * 。
     * 初始化redis工厂 注入池
     *
     * @return
     */
    @Bean
    public JedisConnectionFactory getConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setUsePool(true);
        JedisPoolConfig config = new JedisPoolConfig();
        factory.setPoolConfig(config);
        factory.setHostName(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setTimeout(properties.getTimeout());
        factory.setDatabase(0);
        log.info("[--------init the redis factory  it's info as follows :" + factory+"-------]");
        log.info("[--------redis's host is :"+properties.getHost()+"-----------]");
        log.info("[--------redis's port is :"+properties.getPort()+"-----------]");
        return factory;
    }

    /**
     * redisTemplate 注入ioc容器中
     *
     * @return
     */
    @Bean
    public RedisTemplate<?, ?> getRedisTemplate() {
        JedisConnectionFactory factory = getConnectionFactory();
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        initDomainRedisTemplate(redisTemplate, factory);
        return redisTemplate;
    }

    /**
     * 设置数据存入 redis 的序列化方式,并开启事务
     *
     * @param factory
     */
    private void initDomainRedisTemplate(RedisTemplate<String, Object> redisTemplate, RedisConnectionFactory factory) {

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        /**
         * 事务支持
         * 项目在非事务管理下用到readis，造成连接不关闭，导致pool资源耗尽
         * 当前业务并未涉及到需要批量指令提交，估取消事务支持
         */
        redisTemplate.setEnableTransactionSupport(false);
        redisTemplate.setConnectionFactory(factory);
    }
}
