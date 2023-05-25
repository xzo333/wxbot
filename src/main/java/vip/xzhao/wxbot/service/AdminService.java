package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;

public interface AdminService {
    /**
     * 修改电池
     *
     * @param message
     * @return
     */
    ResponseEntity ModifyBattery(Message message);

    /**
     * 刷新等级
     *
     * @param message
     * @return
     */
    ResponseEntity updateGradeByBattery(String message);
    /**
     * 刷新停止晋级等级
     *
     * @param message
     * @return
     */
    ResponseEntity RefreshStopPromotionLevel(String message);

    /**
     * 停止晋级
     *
     * @param message
     * @return
     */
    ResponseEntity FreezeLevel(Message message);

    /**
     * 关闭停止晋级
     *
     * @param message
     * @return
     */
    ResponseEntity ThawLevel(Message message);

    /**
     * 下载数据文字
     *
     * @param message
     * @return
     */
    ResponseEntity DownloadRealTimeData(Message message);
    /**
     * 取消订单
     *
     * @param message
     * @return
     */
    ResponseEntity CancelOrder(Message message);

}
