package vip.xzhao.wxbot.data;

import lombok.Data;

import java.util.List;

@Data
public class WxMessage {
    /**
     * 机器人 ID
     */
    private String botId;

    /**
     * 机器人名称
     */
    private String botName;

    /**
     * 服务器 ID
     */
    private String serverId;

    /**
     * 消息 ID
     */
    private long messageId;

    /**
     * 本地消息 ID
     */
    private String localId;

    /**
     * 消息时间戳，单位为毫秒
     */
    private long timestamp;

    /**
     * 消息，message，事件notice 测回 退群
     */
    private String type;

    /**
     * 是否是自己发送的消息
     */
    private boolean isSelf;

    /**
     * 聊天 ID
     */
    private String chatId;

    /**
     * 发送消息的用户 ID
     */
    private String userId;

    /**
     * 发送消息的用户名
     */
    private String userName;

    /**
     * 群聊 ID
     */
    private String groupId;

    /**
     * 群聊名称
     */
    private String groupName;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型，例如 text，recallMessage（撤回），image（图片），groupMemberDecrease（退群）
     */
    private String subtype;

    /**
     * @ 的用户列表
     */
    private List<String> ats;
    private boolean isGroup;
    /**
     * 退群进群组
     */
    private String groupMembers;

}
