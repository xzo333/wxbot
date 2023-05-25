package vip.xzhao.wxbot.active;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import vip.xzhao.wxbot.data.Send;

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
        WebClient webClient = WebClient.create("http://43.153.186.146:9018/we/post/rev");
        Send send = new Send();
        send.setAtuser(Atuser);
        send.setTogroup(Togroup);
        send.setMsg(Msg);

        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(send))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
