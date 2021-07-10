package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 属性类,从配置文件中获取阿里云文件系统的配置信息
 */
@Data
@ConfigurationProperties(prefix = "tanhua.oss")
public class OssProperties {
    private String endpoint;
    private String bucketName;
    private String url;
    private String accessKeyId;
    private String accessKeySecret;
}
