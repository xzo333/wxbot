package vip.xzhao.wxbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import vip.xzhao.wxbot.active.MsgACT;

@SpringBootTest
class WxbotApplicationTests {

    public final MsgACT msgACT;
    WxbotApplicationTests(MsgACT msgACT) {
        this.msgACT = msgACT;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void post() {
       /* msgACT.WebApiClient("", "48408656825@chatroom", "测试");*/
    }
}
