package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 属性类,用于从配置文件中获取数据,以便java代码调用
 */
@Data
@ConfigurationProperties(prefix = "tanhua.sms")
public class SmsProperties {

    /**
     * 签名
     */
    private String signName;

    /**
     * 模板中的参数名
     */
    private String parameterName;

    /**
     * 验证码 短信模板code
     */
    private String validateCodeTemplateCode;

    private String accessKeyId;
    private String accessKeySecret;
}
