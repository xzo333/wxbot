package vip.xzhao.wxbot;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.active.MsgAi;

import java.io.IOException;

@SpringBootTest
class WxbotApplicationTests {

    @Autowired
    private MsgACT msgACT;
    @Autowired
    private MsgAi msgAi;

    @Test
    void contextLoads() {
    }

    @Test
    void post() {
       msgACT.WebApiClient("", "25984983585997042@openim", "测试");
    }

    @Test
    void Ai(){
        /*String Msg = "ai你好";
        Msg = Msg.substring(2);  // 删除开头的 "ai"
        String Aimsg = msgAi.Ai(Msg,);
        msgACT.WebApiClient("", "25984983585997042@openim", Aimsg);*/
}
}
