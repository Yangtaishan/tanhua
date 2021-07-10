package com.tanhua.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 服务消费者启动类
 */
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@EnableCaching  //开启缓存注解
public class TanhuaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TanhuaServerApplication.class,args);
    }
}
