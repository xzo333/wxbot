package vip.xzhao.wxbot.data;

import lombok.Data;

import java.util.Map;

@Data
public class AIdate {
    /**
     * 信息
     */
    private String prompt;
    private String options;
    /**
     * #/chat/1685771238319\
     */
    private String userId;
    private String usingContext;
    /**
     * 网络
     */
    private String network;
    private String stream;
    private String system;
    private String withoutContext;
}
