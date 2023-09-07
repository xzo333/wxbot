package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;

public interface AdminService {
    /**
     * 修改电池
     */
    ResponseEntity ModifyBattery(Message message);

    /**
     * 刷新等级
     */
    ResponseEntity updateGradeByBattery(String message);

    /**
     * 刷新停止晋级等级
     */
    ResponseEntity RefreshStopPromotionLevel(String message);

    /**
     * 停止晋级
     */
    ResponseEntity FreezeLevel(Message message);

    /**
     * 关闭停止晋级
     */
    ResponseEntity ThawLevel(Message message);

    /**
     * 下载数据文字
     */
    ResponseEntity DownloadRealTimeData(Message message);

    /**
     * 取消订单
     */
    ResponseEntity CancelOrder(Message message);

    /**
     * 修改续单
     */
    ResponseEntity modifyContinuedOrderQuantity(Message message);
    /**
     * 修改接单
     */
    ResponseEntity modifyReceipt(Message message);
    /**
     * 修改已有订单
     */
    ResponseEntity modifyAnExistingOrder(Message message);

    /**
     * 删监督员
     */
    ResponseEntity deleteSupervisor(String name,String group);

}
