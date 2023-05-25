package vip.xzhao.wxbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WxbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxbotApplication.class, args);
    }

}
