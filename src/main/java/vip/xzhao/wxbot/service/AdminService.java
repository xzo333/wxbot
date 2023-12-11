package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.data.WxMessage;
import vip.xzhao.wxbot.data.dto.MsgGroupDto;

public interface AdminService {
    /**
     * 修改电池
     */
    String ModifyBattery(String wid,String groupid,String msg);

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
    String DownloadRealTimeData(String groupid);

    /**
     * 取消订单
     */
    ResponseEntity CancelOrder(Message message);

    /**
     * 修改权值
     */
    String modifyContinuedOrderQuantity(String wid,String groupid,String msg);
    /**
     * 修改接单
     */
    String modifyReceipt(String wid,String groupid,String msg);
    /**
     * 修改已有订单
     */
    ResponseEntity modifyAnExistingOrder(Message message);

    /**
     * 删监督员
     */
    String deleteSupervisor(String name,String group);
    /**
     * 设置管理员
     */
    void setupAdministrator(String name, String group);
    /**
     * 查看管理员
     */
    void viewAdministrator(String groupid);
}
