package vip.xzhao.wxbot.data;

import lombok.Data;

import java.util.Map;

@Data
public class Message {
    /**
     * 机器人微信号
     */
    private String robot_wxid;
    /**
     * 消息类型，1文本
     */
    private Long type;
    /**
     * 消息所在群的群id
     */
    private String from_group;
    /**
     * 消息所在群的群名
     */
    private String from_group_name;
    /**
     * 消息所在群的群名 2
     */
    private String to_name;
    /**
     * 发送消息人的wxid
     */
    private String from_wxid;
    /**
     * 发送消息人的wxid 2
     */
    private String to_wxid;
    // 发送消息的联系人昵称
    private String from_name;
    /**
     * 消息内容
     */
    private String msg;
    /**
     * 消息来源相关信息
     */
    private Map<String, Object> msg_source;
    /**
     * 客户端ID
     */
    private Long clientid;
    /**
     * 机器人类型
     */
    private Long robot_type;
    /**
     * 消息ID
     */
    private String msg_id;
}
