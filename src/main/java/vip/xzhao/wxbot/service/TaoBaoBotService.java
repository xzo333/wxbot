package vip.xzhao.wxbot.service;

import vip.xzhao.wxbot.data.WxMessage;

public interface TaoBaoBotService {
    void handleGroupMsg(WxMessage wxMessage);
}
