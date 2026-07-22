package com.bob.pay;

import com.bob.pay.config.WechatOfficialAccountProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WechatOfficialAccountProperties.class)
public class DaoHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaoHomeApplication.class, args);
    }
}
