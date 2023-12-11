package vip.xzhao.wxbot.active;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import vip.xzhao.wxbot.data.ApiSend;

@Component
public class MsgApi {
    /**
     * 发送信息
     * @param chatId 微信id
     * @param Msg  信息
     * @param atIds  @wxid数组
     * @return
     */
    public String WebApiClient(String chatId, String Msg, String[] atIds) {
        WebClient webClient = WebClient.create("http://wxserver.6hu.cc/sendTextMessage");
        ApiSend send = new ApiSend();
        send.setBotId("wxid_n5vjejrw1uyt22");//机器人
        send.setChatId(chatId);
        send.setContent(Msg);
        send.setAtIds(atIds);


        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(send))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
