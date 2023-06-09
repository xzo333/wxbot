package vip.xzhao.wxbot.data;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class AIdate {
    /**
     * 信息
     */
    @JSONField(ordinal = 1)
    private String prompt;
    /**
     * ID
     */
    @JSONField(ordinal = 2)
    private String userId;
    /**
     * 网络
     */
    @JSONField(ordinal = 3)
    private boolean network = false; // 将属性的默认值设为 false

    @JSONField(ordinal = 4)
    private String system;
    @JSONField(ordinal = 5)
    private boolean withoutContext = false; // 将属性的默认值设为 false
    @JSONField(ordinal = 6)
    private boolean stream = false; // 将属性的默认值设为 false
/*    private String options;

    private String usingContext;*/

}
