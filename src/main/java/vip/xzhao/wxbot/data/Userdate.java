package vip.xzhao.wxbot.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("userdate")
public class Userdate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String wxid;
    private String name;
    private String grade;
    private Long battery;
    private Long historicalbattery;
    private Long state;
    /**
     * 续单数
     */
    private Long continuation;
    /**
     * 接单数
     */
    private Long numberoforders;
    /**
     * 已有订单
     */
    private Long existingorder;
    /**
     * 管理
     */
    private Long isadmin;
}
