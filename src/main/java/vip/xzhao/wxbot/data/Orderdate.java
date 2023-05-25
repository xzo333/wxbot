package vip.xzhao.wxbot.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orderdate")
public class Orderdate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderid;
    private LocalDateTime date;
    private String name;
    private String grade;
    private String wxid;
}
