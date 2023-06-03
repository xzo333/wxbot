package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.data.Orderdate;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.mapper.OrderdateMapper;
import vip.xzhao.wxbot.mapper.UserMapper;
import vip.xzhao.wxbot.service.TellerService;
import vip.xzhao.wxbot.util.Order;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TellerServiceimpl implements TellerService {
    public final MsgACT msgACT;
    public final UserMapper userMapper;
    public final OrderdateMapper orderdateMapper;

    public TellerServiceimpl(MsgACT msgACT, UserMapper userMapper, OrderdateMapper orderdateMapper) {
        this.msgACT = msgACT;
        this.userMapper = userMapper;
        this.orderdateMapper = orderdateMapper;
    }

    @Override
    public ResponseEntity Order(Message message) {
        QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Userdate::getWxid, message.getFrom_wxid());
        Userdate res = userMapper.selectOne(queryWrapper);

        //判断是否设置了名称
        if (res == null) {
            msgACT.WebApiClient("", message.getFrom_group(), "请先设置昵称\n格式[昵称：自定义昵称]\n发群里,包含[]\n示范：[昵称：小高]");
        } else {
            //订单号
            String orderid = Order.extractDigit(message.getMsg());
            // 获取当前时间
            LocalDateTime currentDateTime = LocalDateTime.now();

            QueryWrapper<Orderdate> or = new QueryWrapper<>();
            or.lambda().eq(Orderdate::getOrderid, orderid);
            Orderdate resor = orderdateMapper.selectOne(or);
            Orderdate orderdate = new Orderdate();
            //判断重复接单
            if (resor == null) {
                //等级
                if (message.getMsg().contains("金牌") & res.getGrade().equals("金牌")) {
                    orderdate.setOrderid(orderid);
                    orderdate.setName(res.getName());
                    orderdate.setDate(currentDateTime);
                    orderdate.setWxid(message.getFrom_wxid());
                    orderdate.setGrade(res.getGrade());
                    orderdateMapper.insert(orderdate);
                    msgACT.WebApiClient("", message.getFrom_group(), "订单:" + orderid + "\n" +
                            res.getName() + "接单成功");
                } else if (message.getMsg().contains("正式") & res.getGrade().equals("正式")) {
                    orderdate.setOrderid(orderid);
                    orderdate.setName(res.getName());
                    orderdate.setDate(currentDateTime);
                    orderdate.setGrade(res.getGrade());
                    orderdate.setWxid(message.getFrom_wxid());
                    orderdateMapper.insert(orderdate);
                    msgACT.WebApiClient("", message.getFrom_group(), "订单:" + orderid + "\n" +
                            res.getName() + "接单成功");
                } else if (message.getMsg().contains("见习") & res.getGrade().equals("见习")) {
                    orderdate.setOrderid(orderid);
                    orderdate.setName(res.getName());
                    orderdate.setDate(currentDateTime);
                    orderdate.setGrade(res.getGrade());
                    orderdate.setWxid(message.getFrom_wxid());
                    orderdateMapper.insert(orderdate);
                    msgACT.WebApiClient("", message.getFrom_group(), "订单:" + orderid + "\n" +
                            res.getName() + "接单成功");
                } else {
                    msgACT.WebApiClient("", message.getFrom_group(), "订单:" +
                            orderid + "\n" +
                            res.getName() +
                            "接单失败" +
                            "\n你的等级是：" + res.getGrade() +
                            "\n不符合接单等级");
                }
            }
            if (message.getMsg().contains("转单")) {
                if (resor.getGrade().equals(res.getGrade())) {
                    UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("orderid", orderid).set("name", res.getName());
                    userMapper.update(null, updateWrapper);
                    msgACT.WebApiClient("", message.getFrom_group(), "订单:" + orderid + "\n" +
                            res.getName() + "接转单成功");
                } else {
                    msgACT.WebApiClient("", message.getFrom_group(), "订单:" +
                            orderid + "\n" +
                            res.getName() +
                            "接转单失败" +
                            "\n你的等级是：" + res.getGrade() +
                            "\n不符合接单等级");
                }
            } else {
                msgACT.WebApiClient("", message.getFrom_group(), "订单:" +
                        orderid + "\n" +
                        res.getName() +
                        "接单失败\n此订单接单员是：" + resor.getName());
            }
        }
        return null;
    }

    @Override
    public ResponseEntity Name(Message message) {
        QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Userdate::getWxid, message.getFrom_wxid());
        Userdate res = userMapper.selectOne(queryWrapper);
        Userdate userdate = new Userdate();
        String str = message.getMsg();
        //昵称
        String nickname = str.substring(str.indexOf("：") + 1, str.length() - 1);
        //System.out.println(nickname);
        if (res == null) {
            userdate.setWxid(message.getFrom_wxid());
            userdate.setName(nickname);
            userdate.setBattery(10L);
            userdate.setHistoricalbattery(10L);
            userdate.setGrade("见习");
            userdate.setState(0L);
            userMapper.insert(userdate);
            msgACT.WebApiClient("", message.getFrom_group(), "昵称：" + nickname + "\n等级：见习\n注册成功\n新用户赠送10电池");
        } else {
            UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<Userdate>()
                    .eq("wxid", message.getFrom_wxid())
                    .set("name", nickname);
            userMapper.update(null, updateWrapper);
            msgACT.WebApiClient("", message.getFrom_group(), "原昵称：" + res.getName() + "\n修改成\n新昵称：" + nickname + "\n修改成功");
        }

        return null;
    }

    @Override
    public ResponseEntity ViewBattery(Message message) {
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getWxid, message.getFrom_wxid());
            Userdate res = userMapper.selectOne(queryWrapper);
            msgACT.WebApiClient("", message.getFrom_group(), res.getName() + "\n电池：" + res.getBattery() + "\n总电池：" + res.getHistoricalbattery() + "\n等级：" + res.getGrade());
        } catch (Exception e) {
            msgACT.WebApiClient("", message.getFrom_group(), "查询失败，请先设置昵称");
        }
        return null;
    }

    @Override
    public ResponseEntity Ranking(Message message) {
        QueryWrapper<Userdate> wrapper = new QueryWrapper<>();
        wrapper.select("name", "battery", "grade")
                .orderByDesc("battery")
                .last("limit 50");
        List<Userdate> userList = userMapper.selectList(wrapper);
        // 构造要发送的消息
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("排名前50的接单员：\n");
        messageBuilder.append("姓名\t电池\t等级\n");  // 列名部分
        for (Userdate user : userList) {
            messageBuilder.append(user.getName())
                    .append("\t").append(user.getBattery())
                    .append("\t").append(user.getGrade())
                    .append("\n");
        }

        String messageToSend = messageBuilder.toString();

        // 使用机器人发送消息到指定的QQ群
        String groupIdToReceiveMsg = message.getFrom_group();  // 接收消息的QQ群号
        msgACT.WebApiClient("", groupIdToReceiveMsg, messageToSend);
        return null;
    }

    @Override
    public ResponseEntity CommandDescription(Message message) {
        msgACT.WebApiClient("", message.getFrom_group(), "【接单员】\n" +
                "注册/修改昵称：\n" +
                "[昵称：小高]\n" +
                "chatGPT：\n" +
                "问*问题文字\n" +
                "\n" +
                "查看电池\n" +
                "查看排名\n" +
                "\n" +
                "【管理员】\n" +
                "修改电池：\n" +
                "，小高+100，落花-100\n" +
                "停止晋级：\n" +
                "[小高]停止晋级\n" +
                "关闭停止晋级：\n" +
                "[小高]关闭停止晋级\n" +
                "取消订单：\n" +
                "取消订单：12346。\n" +
                "\n" +
                "下载实时数据\n" +
                "刷新停止晋级等级\n" +
                "刷新等级");
        return null;
    }
}