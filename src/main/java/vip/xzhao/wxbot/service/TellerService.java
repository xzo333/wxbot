package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.data.WxMessage;

/**
 * 接单员
 */
public interface TellerService {
    /**
     * 接单
     *
     * @param message
     * @return
     */
    ResponseEntity Order(Message message);

    /**
     * 昵称
     *
     */
    String Name(WxMessage message);

    /**
     * 查看电池
     *
     */
    String ViewBattery(WxMessage message);

    /**
     * 查看排名
     *
     * @param message
     * @return
     */
    String Ranking(WxMessage message);

    /**
     * 命令说明
     *
     * @param message
     * @return
     */
    ResponseEntity CommandDescription(Message message);

}
