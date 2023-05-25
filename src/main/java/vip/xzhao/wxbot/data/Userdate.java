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
}
