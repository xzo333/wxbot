package vip.xzhao.wxbot.service;

import vip.xzhao.wxbot.data.WxMessage;

public interface DispatchService {
    /**
     * 派单接单
     */
    String order(WxMessage wxMessage);
}
