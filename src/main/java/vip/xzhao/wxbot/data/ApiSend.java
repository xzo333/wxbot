package vip.xzhao.wxbot.data;

import lombok.Data;

@Data
public class ApiSend extends WxMessage {
    /**
     * 机器人id
     */
    private String botId;
    /**
     * wxid
     */
    private String chatId;
    /**
     * 信息
     */
    private String content;
    private String[] atIds;
}
