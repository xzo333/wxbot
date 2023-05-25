package vip.xzhao.wxbot.data;

import lombok.Data;

@Data
public class Send {
    private String atuser;
    /**
     * wxid
     */
    private String togroup;
    /**
     * 信息
     */
    private String msg;
}
