package vip.xzhao.wxbot.active;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import vip.xzhao.wxbot.data.Send;

import java.time.Duration;

@Slf4j
@Component
public class MsgACT {

    /**
     * 发送数据
     *
     * @param Atuser
     * @param Togroup wxid
     * @param Msg     信息
     * @return
     */
    public String WebApiClient(String Atuser, String Togroup, String Msg) {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://ts.jingsai.win:19000/we/post/rev")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(10))))
                .build();

        Send send = new Send();
        send.setAtuser(Atuser);
        send.setTogroup(Togroup);
        send.setMsg(Msg);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(send))
                .exchangeToMono(response -> {
                    HttpStatus status = response.statusCode();
                    if (status.is2xxSuccessful()) {
                        return response.bodyToMono(String.class);
                    } else {
                        // 记录错误状态码和发送的内容
                        String errorLog = "状态码: " + status.value() + ", 内容: " + send.toString();
                        // 处理错误逻辑，例如写入日志文件或发送通知
                        log.error(errorLog);
                        // 返回自定义的错误提示信息
                        return Mono.error(new RuntimeException("接口发送失败."));
                    }
                })
                .block();
    }
}
