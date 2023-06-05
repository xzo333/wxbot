package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.active.MsgAi;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.mapper.UserMapper;
import vip.xzhao.wxbot.service.AiService;

@Service
public class AiServicempl implements AiService {
    public final MsgACT msgACT;
    public final MsgAi msgAi;
    public final UserMapper userMapper;

    public AiServicempl(MsgACT msgACT, MsgAi msgAi, UserMapper userMapper) {
        this.msgACT = msgACT;
        this.msgAi = msgAi;
        this.userMapper = userMapper;
    }

    @Override
    public String Ai(Message message) {
        String Msg = message.getMsg();
        Msg = Msg.substring(2);  // 删除开头的 "ai"

        //根据wiid上下文进行互动
        String wxid = message.getFrom_wxid();
        long num = Long.parseLong(wxid.substring(0, wxid.indexOf("@")));  // 直接获取数字部分并转换为long类型
        String UserId = "#/chat/" + num;
        String aimsg = msgAi.ai(Msg,UserId);
        String newStr = aimsg.replace("binjie09", "小高");
        //获取昵称
        try {
            Userdate res = userMapper.selectOne(new QueryWrapper<Userdate>().lambda().eq(Userdate::getWxid, wxid));
            String name = res.getName();
            msgACT.WebApiClient("", message.getFrom_group(), name + "\n" + newStr);
           } catch (Exception e) {
            msgACT.WebApiClient("", message.getFrom_group(), "使用Ai，需要先设置昵称");
        }

       /* //去广告
        if (Aimsg.contains("网站已经基本恢复正常")){
            String MM = "Ai正在思考中。。。\n\n" +
                    "支持根据微信号上下文互动\n" +
                    "如果你觉得做的好，可以给我买一包辣条嘛\n";
            msgACT.WebApiClient("", message.getFrom_group(), MM);
            String AimsgA = msgAi.ai(Msg,UserId);
            msgACT.WebApiClient("", message.getFrom_group(), AimsgA);
        }else {
            msgACT.WebApiClient("", message.getFrom_group(), Aimsg);
        }*/
        return null;
    }
}
