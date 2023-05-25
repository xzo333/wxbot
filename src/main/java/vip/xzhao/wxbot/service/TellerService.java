package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;

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
     * @param message
     * @return
     */
    ResponseEntity Name(Message message);

    /**
     * 查看电池
     *
     * @param message
     * @return
     */
    ResponseEntity ViewBattery(Message message);

    /**
     * 查看排名
     *
     * @param message
     * @return
     */
    ResponseEntity Ranking(Message message);

    /**
     * 命令说明
     *
     * @param message
     * @return
     */
    ResponseEntity CommandDescription(Message message);

}
