package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import vip.xzhao.wxbot.service.AdminService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AdminServiceimpl implements AdminService {
    public final UserMapper userMapper;
    public final OrderdateMapper orderdateMapper;
    public final MsgACT msgACT;

    public AdminServiceimpl(UserMapper userMapper, OrderdateMapper orderdateMapper, MsgACT msgACT) {
        this.userMapper = userMapper;
        this.orderdateMapper = orderdateMapper;
        this.msgACT = msgACT;
    }

    @Override
    public ResponseEntity ModifyBattery(Message message) {
        //信息
        String text = message.getMsg();
        log.info("加减电池收到消息：" + text);
        /*//加电池昵称
        String name = text.substring(text.indexOf("[") + 1, text.indexOf("]")); // 获取[]中的名字部分，即"小高"
        //电池数
        Long number = Long.valueOf(text.replaceAll("[^0-9]", "")); // 替换除了数字以外的所有字符，即"100"
        //System.out.println("昵称：" + name + "，电池：" + number);*/
        //Pattern pattern = Pattern.compile("，(\\S+)([\\+\\-=])(\\d+)");
        Pattern pattern = Pattern.compile("(?<=^|，)([^，]+)([\\+\\-=])(\\d+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            String op = matcher.group(2);
            int number = Integer.parseInt(matcher.group(3));
            log.info("Match: jname=" + name + ", op=" + op + ", num=" + number);
        //数据库
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getName, name);
            Userdate res = userMapper.selectOne(queryWrapper);
            UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
            if (op.equals("+")) {
                // 处理电池加上数字的情况
                try {
                    long t = res.getBattery() + number;
                    long tt = res.getHistoricalbattery() + number;
                    updateWrapper.eq("name", name).set("battery", t).set("historicalbattery", tt);
                    userMapper.update(null, updateWrapper);
                    msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                            "\n加电池" + number +
                            "\n现电池:" + t +
                            "\n原等级:" + res.getGrade() +
                            "\n卷卷再接再厉");
                    updateGradeByBattery(message.getFrom_group());
                } catch (Exception e) {
                    msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                            "\n加电池失败");
                }
            } else if (op.equals("-")) {
                // 处理电池减去数字的情况
                try {
                    long t = res.getBattery() - number;
                    updateWrapper.eq("name", name).set("battery", t);
                    userMapper.update(null, updateWrapper);
                    msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                            "\n减电池" + number +
                            "\n现电池:" + t +
                            "\n原等级:" + res.getGrade() +
                            "\n卷卷再接再厉");
                    updateGradeByBattery(message.getFrom_group());
                } catch (Exception e) {
                    msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                            "\n减电池失败");
                }
            } else if (op.equals("=")) {
                // 处理电池=数字
                try {
                    updateWrapper.eq("name", name).set("battery", number).set("historicalbattery", number);
                    userMapper.update(null, updateWrapper);
                    msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                            "\n现电池:" + number +
                            "\n原等级:" + res.getGrade() +
                            "\n卷卷再接再厉");
                    updateGradeByBattery(message.getFrom_group());
                } catch (Exception e) {
                    msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                            "\n修改电池失败");
                }
            }
        } catch (Exception e) {
            msgACT.WebApiClient("", message.getFrom_group(), name +
                    "\n修改电池失败\n没有这个昵称");
        }
        }
        return null;
    }

    @Override
    public ResponseEntity updateGradeByBattery(String GroupId) {
        try {
            // 构造查询条件
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getState, 0);
            // 构造更新值和更新条件
            UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(Userdate::getState, 0).setSql("grade = " +
                    "case when battery <= 29 then '见习' " +
                    "when battery >= 30 and battery <= 179 then '正式' " +
                    "else '金牌' end");
            // 更新数据库
            userMapper.update(null, updateWrapper);
            //msgACT.WebApiClient("", GroupId, "全部接单员等级刷新成功");
        } catch (Exception e) {
            msgACT.WebApiClient("", GroupId, "等级刷新失败，呜呜");
        }
        return null;
    }

    @Override
    public ResponseEntity RefreshStopPromotionLevel(String GroupId) {
        try {
            //正式
            UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().eq(Userdate::getState, 1)
                    .eq(Userdate::getGrade, "正式")
                    .le(Userdate::getBattery, 29) //电量小于等于29
                    .set(Userdate::getGrade, "见习"); //更新为“见习”
            int rowsA = userMapper.update(null, updateWrapper);
            //金牌
            UpdateWrapper<Userdate> updateWrapperA = new UpdateWrapper<>();
            updateWrapperA.lambda().eq(Userdate::getState, 1)
                    .eq(Userdate::getGrade, "金牌")
                    .between(Userdate::getBattery, 30, 179) //电量介于30到149之间
                    .set(Userdate::getGrade, "正式"); //更新为“正式”
            int rowsB = userMapper.update(null, updateWrapperA);

            //msgACT.WebApiClient("", GroupId, "全部停止晋级接单员等级刷新成功\n" + rowsB + "个金牌降到了正式\n" + rowsA + "个正式降到了见习");
        } catch (Exception e) {
            msgACT.WebApiClient("", GroupId, "全部停止晋级接单员等级刷新失败，呜呜");
        }
        return null;
    }

    @Override
    public ResponseEntity FreezeLevel(Message message) {
        String str = message.getMsg();
        int start = str.indexOf("[") + 1; // 获取 "[" 后面的位置
        int end = str.indexOf("]");       // 获取 "]" 的位置
        String matcher = str.substring(start, end); // 截取子串
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getName, matcher);
            Userdate res = userMapper.selectOne(queryWrapper);
            if (res.getState() == 0) {
                UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("name", matcher).set("State", "1");
                userMapper.update(null, updateWrapper);
                msgACT.WebApiClient("", message.getFrom_group(), matcher + "\n停止晋级成功");
            } else {
                msgACT.WebApiClient("", message.getFrom_group(), matcher + "\n还在停止晋级中，无需重复申请");
            }
        } catch (Exception e) {
            msgACT.WebApiClient("", message.getFrom_group(), matcher + "\n出错了，停止晋级失败");
        }
        return null;
    }

    @Override
    public ResponseEntity ThawLevel(Message message) {
        String str = message.getMsg();
        int start = str.indexOf("[") + 1; // 获取 "[" 后面的位置
        int end = str.indexOf("]");       // 获取 "]" 的位置
        String matcher = str.substring(start, end); // 截取子串
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getName, matcher);
            Userdate res = userMapper.selectOne(queryWrapper);
            if (res.getState() == 1) {
                UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("name", matcher).set("state", "0");
                userMapper.update(null, updateWrapper);
                msgACT.WebApiClient("", message.getFrom_group(), matcher + "\n关闭停止晋级成功");
            } else {
                msgACT.WebApiClient("", message.getFrom_group(), matcher + "\n无需关闭停止晋级");
            }
        } catch (Exception e) {
            msgACT.WebApiClient("", message.getFrom_group(), matcher + "\n出错了，关闭停止晋级失败");
        }
        return null;
    }

    @Override
    public ResponseEntity DownloadRealTimeData(Message message) {
        msgACT.WebApiClient("", message.getFrom_group(), "点击链接下载实时Excel文件\n" +
                "订单表：http://wxbot.6hu.cc/export/order\n" +
                "接单员表：http://wxbot.6hu.cc/export/user");
        return null;
    }

    @Override
    public ResponseEntity CancelOrder(Message message) {
        String regex = "^取消订单：(\\d+)。$"; // 匹配以"取消订单："开头，以"."结尾，中间为数字的字符串
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message.getMsg());
        if (matcher.find()) {
            String result = matcher.group(1); // 正则表达式中用()括号捕获的内容，使用group(1)来获取
            try {
                LambdaQueryWrapper<Orderdate> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Orderdate::getOrderid, result);
                int reg = orderdateMapper.delete(wrapper);
                if (reg == 0) {
                    msgACT.WebApiClient("", message.getFrom_group(), "订单：" + result + "\n取消失败");
                } else {
                    msgACT.WebApiClient("", message.getFrom_group(), "订单：" + result + "\n取消成功");
                }
            } catch (Exception e) {
                msgACT.WebApiClient("", message.getFrom_group(), "订单：" + result + "\n取消出现报错");
            }
        }
        return null;
    }
}
