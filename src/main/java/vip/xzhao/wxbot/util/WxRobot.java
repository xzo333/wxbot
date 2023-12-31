package vip.xzhao.wxbot.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import vip.xzhao.wxbot.data.Message;

@Slf4j
@Service
public class WxRobot {
    private static final String API_URL = "http://v2.wxbot.6hu.cc/msg/viewNegativeBattery";

    /**
     * 发送数据到另一个接口
     *
     * @param message 消息体
     */
    @Async
    public void sendToAnotherApi(Message message) {
        WebClient webClient = WebClient.create(API_URL);
        try {
            webClient.post()
                    .uri(API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(response -> log.error("查看负电池发送数据到另一个接口成功，返回结果: {}", response), error -> log.debug("查看负电池发送数据到另一个接口出错: {}", error.getMessage(), error));
        } catch (Exception e) {
            log.error("查看负电池发送数据到另一个接口出错: {}", e.getMessage(), e);
        }
    }
}
