package com.tanhua.commons;

import com.tanhua.commons.properties.FaceProperties;
import com.tanhua.commons.properties.HuanXinProperties;
import com.tanhua.commons.properties.OssProperties;
import com.tanhua.commons.properties.SmsProperties;
import com.tanhua.commons.templates.FaceTemplate;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.commons.templates.SmsTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 配置类,自动装配
 */
@Configuration
@EnableConfigurationProperties({
        SmsProperties.class,
        OssProperties.class,
        FaceProperties.class,
        HuanXinProperties.class
})
public class CommonsAutoConfiguration {

    /**
     * 阿里云短信验证码
     * 当工程启动后,创建smsTemplate对象,放到spring容器中
     * @param smsProperties
     * @return
     */
    @Bean
    public SmsTemplate smsTemplate(SmsProperties smsProperties){
        SmsTemplate smsTemplate = new SmsTemplate(smsProperties);
        smsTemplate.init();
        return smsTemplate;
    }

    /**
     * 阿里云文件上传
     * @param ossProperties
     * @return
     */
    @Bean
    public OssTemplate ossTemplate(OssProperties ossProperties){
        OssTemplate ossTemplate = new OssTemplate(ossProperties);
        return ossTemplate;
    }

    /**
     * 百度AI
     * @param faceProperties
     * @return
     */
    @Bean
    public FaceTemplate faceTemplate(FaceProperties faceProperties){
        FaceTemplate faceTemplate = new FaceTemplate(faceProperties);
        return faceTemplate;
    }

    /**
     * 环信即时通讯
     * @param huanXinProperties
     * @return
     */
    @Bean
    public HuanXinTemplate huanXinTemplate(HuanXinProperties huanXinProperties){
        return new HuanXinTemplate(huanXinProperties);
    }

    /**
     * 环信即时通讯
     * @param builder
     * @return
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }
}
