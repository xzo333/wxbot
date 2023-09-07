package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
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
        //String str = message.getMsg();
        String str = message.getMsg().replace("昵称:", "昵称：");
        //昵称
        String nickname = str.substring(str.indexOf("：") + 1, str.length() - 1);
        if (res == null) {
            userdate.setWxid(message.getFrom_wxid());
            userdate.setName(nickname);
            userdate.setBattery(10L);
            userdate.setHistoricalbattery(10L);
            userdate.setGrade("实习");
            userdate.setState(0L);
            userdate.setContinuation(1L);
            userdate.setNumberoforders(1L);
            userMapper.insert(userdate);
            msgACT.WebApiClient("", message.getFrom_group(), "昵称：" + nickname + "\n等级：见习\n注册成功\n新用户赠送10电池");
        } else {
            if (res.getName().equals("游离态")) {
                msgACT.WebApiClient("", message.getFrom_group(), "呜呜呜，为啥要改昵称，不改好嘛");
            } else {
                UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<Userdate>()
                        .eq("wxid", message.getFrom_wxid())
                        .set("name", nickname);
                userMapper.update(null, updateWrapper);
                msgACT.WebApiClient("", message.getFrom_group(), "原昵称：" + res.getName() + "\n修改成\n新昵称：" + nickname + "\n修改成功");
            }
        }
        return null;
    }

    @Override
    public ResponseEntity  ViewBattery(Message message) {
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getWxid, message.getFrom_wxid());
            Userdate res = userMapper.selectOne(queryWrapper);

            if (res != null) {
                BigDecimal t = new BigDecimal(Optional.ofNullable(res.getContinuation()).orElse(0L));
                BigDecimal tt = new BigDecimal(Optional.ofNullable(res.getNumberoforders()).orElse(0L));
                BigDecimal ttt = new BigDecimal(Optional.ofNullable(res.getExistingorder()).orElse(0L));
                BigDecimal ratio = BigDecimal.ZERO;

                if (tt.compareTo(BigDecimal.ZERO) != 0) {
                    //ratio = t.multiply(new BigDecimal("100")).divide(tt, 2, RoundingMode.HALF_UP);
                     ratio = t.divide(tt, 2, RoundingMode.HALF_UP);
                }

                msgACT.WebApiClient("", message.getFrom_group(),
                        res.getName() +
                                "\n等级：" + res.getGrade() +
                                "\n电池：" + res.getBattery() +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString());
/*
                                "\n进行中的订单：" + ttt);
*/
                return null;
            } else {
                msgACT.WebApiClient("", message.getFrom_group(), "查询失败，请先设置昵称");
                return null;
            }
        } catch (Exception e) {
            log.error("查询电池报错", e);
            msgACT.WebApiClient("", message.getFrom_group(), "查询失败，请重试");
            return null;
        }
    }

    @Override
    public ResponseEntity Ranking(Message message) {
        QueryWrapper<Userdate> wrapper = new QueryWrapper<>();
        wrapper.select("name", "battery", "grade","continuation","numberoforders")
                .orderByDesc("battery")
                .last("limit 50");
        List<Userdate> userList = userMapper.selectList(wrapper);
        // 构造要发送的消息
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("排名前50的接单员：\n");
        messageBuilder.append("姓名\t电池\t等级\t订单\t权值");  // 列名部分
        for (Userdate user : userList) {
            messageBuilder.append("\n").append(user.getName())
                    .append("\t").append(user.getBattery())
                    .append("\t").append(user.getGrade())
                    .append("\t").append(user.getContinuation())
                    .append("\t").append(user.getNumberoforders());
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
                "Ai问题文字\n" +
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