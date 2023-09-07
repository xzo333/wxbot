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
import vip.xzhao.wxbot.util.ModifyBattery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AdminServiceimpl implements AdminService {
    public final UserMapper userMapper;
    public final OrderdateMapper orderdateMapper;
    public final MsgACT msgACT;
    public final ModifyBattery modifyBattery;

    public AdminServiceimpl(UserMapper userMapper, OrderdateMapper orderdateMapper, MsgACT msgACT, ModifyBattery modifyBattery) {
        this.userMapper = userMapper;
        this.orderdateMapper = orderdateMapper;
        this.msgACT = msgACT;
        this.modifyBattery = modifyBattery;
    }

    /**
     * 修改电池
     *
     * @param message
     * @return
     */
    @Override
    public ResponseEntity ModifyBattery(Message message) {
        //信息
        String text = message.getMsg();
        text = text.replace("@", "");
        log.info("修改电池收到消息：" + text);
        Pattern pattern = Pattern.compile("(?<=^|，)([^，]+)(电池)([\\+\\-=])(\\d+)");

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            String op = matcher.group(3);
            long number = Long.parseLong(matcher.group(4));
            log.info("Match: 昵称=" + name + ", 符号=" + op + ", 数量=" + number);
            //数据库

        /*//信息
        String text = message.getMsg();
        log.info("加减电池收到消息：" + text);
        Pattern pattern = Pattern.compile("(?<=^|，)([^，]+)([\\+\\-=])(\\d+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            String op = matcher.group(2);
            int number = Integer.parseInt(matcher.group(3));
            log.info("Match: jname=" + name + ", op=" + op + ", num=" + number);*/
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
                    "case when battery <= 29 then '实习' " +
                    "when battery >= 30 and battery <= 90 then '正式' " +
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
                    .set(Userdate::getGrade, "实习"); //更新为“见习”
            userMapper.update(null, updateWrapper);
            //金牌
            UpdateWrapper<Userdate> updateWrapperA = new UpdateWrapper<>();
            updateWrapperA.lambda().eq(Userdate::getState, 1)
                    .eq(Userdate::getGrade, "金牌")
                    .between(Userdate::getBattery, 30, 179) //电量介于30到149之间
                    .set(Userdate::getGrade, "正式"); //更新为“正式”
            userMapper.update(null, updateWrapperA);

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
/*
                "订单表：http://wxbot.6hu.cc/export/order\n" +
*/
                "监督员表：http://wxbot.6hu.cc/export/user");
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

    /**
     * 修改权值
     */
    @Override
    public ResponseEntity modifyContinuedOrderQuantity(Message message) {
        //信息
        String text = message.getMsg();
        text = text.replace("@", "");
        log.info("修改权值数收到消息：" + text);
        Pattern pattern = Pattern.compile("(?<=^|，)([^，]+)(权值)([\\+\\-=])(\\d+)");

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            String op = matcher.group(3);
            long number = Long.parseLong(matcher.group(4));
            log.info("Match: 昵称=" + name + ", 符号=" + op + ", 数量=" + number);
            //数据库
            try {
                QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(Userdate::getName, name);
                Userdate res = userMapper.selectOne(queryWrapper);
                UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                if (op.equals("+")) {
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L) + Optional.ofNullable(number).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L) + Optional.ofNullable(number).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L);

                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("continuation", t).set("numberoforders", tt);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n权值+" + number +
                                "\n订单+" + number +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString()
/*
                                "\n进行中的订单：" + ttt
*/
                        );
                        //修改电池
                        Long battery = number * 5;
                        modifyBattery.ModifyBattery(message.getFrom_group(),name,op,battery);
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n加权值失败");
                    }
                } else if (op.equals("-")) {
                    try {
                        // 权值
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L) - Optional.ofNullable(number).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L) - Optional.ofNullable(number).orElse(0L);

                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("continuation", t).set("numberoforders", tt);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n权值-" + number +
                                "\n订单-" + number +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString()
/*
                                "\n进行中的订单：" + ttt
*/
                        );
                        //修改电池
                        Long battery = number * 5;
                        try {
                            modifyBattery.ModifyBattery(message.getFrom_group(),name,op,battery);
                        } catch (Exception e) {
                            msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                    "\n 减电池出现bug");
                            throw new RuntimeException(e);
                        }
                    } catch (Exception e) {
                        log.debug(String.valueOf(e));
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n减权值数失败");
                    }
                } else if (op.equals("=")) {
                    // 处理续单=
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L);
                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(number);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("continuation", number);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n权值=" + number +
                                "\n权值：" + number +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString()
/*
                                "\n进行中的订单：" + ttt
*/
                        );
                        //修改电池
                        Long battery = number * 5;
                        modifyBattery.ModifyBattery(message.getFrom_group(),name,op,battery);
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n修改权值数失败");
                    }
                }
            } catch (Exception e) {
                msgACT.WebApiClient("", message.getFrom_group(), name +
                        "\n修改权值数失败\n没有这个昵称");
            }
        }
        return null;
    }

    /*
    修改接单
     */
    @Override
    public ResponseEntity modifyReceipt(Message message) {
        //信息
        String text = message.getMsg();
        log.info("修改订单数收到消息：" + text);
        Pattern pattern = Pattern.compile("(?<=^|，)([^，]+)(订单)([+\\-=])(\\d+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            String op = matcher.group(3);
            long number = Long.parseLong(matcher.group(4));
            log.info("Match: 昵称=" + name + ", 符号=" + op + ", 数量=" + number);
            //数据库
            try {
                QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(Userdate::getName, name);
                Userdate res = userMapper.selectOne(queryWrapper);
                UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                if (op.equals("+")) {
                    // 处理电池加上数字的情况
                    /*String level = res.getGrade(); // 从数据库获取用户等级
                    int maxOrders = 0; // 最大接单数量
                    if (level.equals("金牌")) {
                        maxOrders = 7;
                    } else if (level.equals("正式")) {
                        maxOrders = 5;
                    } else if (level.equals("见习")) {
                        maxOrders = 3;
                    }*/
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L) + Optional.ofNullable(number).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L) + Optional.ofNullable(number).orElse(0L);
                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        // 判断是否超过最大接单数量
                        /*if (ttt > maxOrders) {
                            msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                    "\n等级：" + res.getGrade() +
                                    "\n进行中的订单数量：" + res.getExistingorder() + " + " + number +
                                    "\n超过限制，请米粒重新派单");
                            return null;
                        }*/

                        updateWrapper.eq("name", name).set("numberoforders", tt).set("existingorder", ttt);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n订单+" + number +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString()
/*
                                "\n进行中的订单：" + ttt
*/
                        );
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n加订单失败");
                    }
                } else if (op.equals("-")) {
                    // 处理电池减去数字的情况
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L) - Optional.ofNullable(number).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L) - Optional.ofNullable(number).orElse(0L);
                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("numberoforders", tt).set("existingorder", ttt);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n订单-" + number +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString()
/*
                                "\n进行中的订单：" + ttt
*/
                        );
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n减订单失败");
                    }
                } else if (op.equals("=")) {
                    // 处理电池=数字
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L);
                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(number);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("numberoforders", number);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n订单=" + number +
                                "\n权值：" + t +
                                "\n总订单：" + number +
                                "\n接单指数：" + ratio.toPlainString()
/*
                                "\n进行中的订单：" + ttt
*/
                        );
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n修改订单数失败");
                    }
                }
            } catch (Exception e) {
                msgACT.WebApiClient("", message.getFrom_group(), name +
                        "\n修改订单数失败\n没有这个昵称");
            }
        }
        return null;
    }

    /**
     * 修改进行中的订单
     * @param message
     * @return
     */
    @Override
    public ResponseEntity modifyAnExistingOrder(Message message) {
        //信息
        String text = message.getMsg();
        log.info("修改进行中的订单收到消息：" + text);
        Pattern pattern = Pattern.compile("(?<=^|，)([^，]+)(进行中的订单)([+\\-=])(\\d+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1);
            String op = matcher.group(3);
            long number = Long.parseLong(matcher.group(4));
            log.info("Match: 昵称=" + name + ", 符号=" + op + ", 数量=" + number);
            //数据库
            try {
                QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(Userdate::getName, name);
                Userdate res = userMapper.selectOne(queryWrapper);
                UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                if (op.equals("+")) {
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L) + Optional.ofNullable(number).orElse(0L);
                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("existingorder", ttt);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n进行中的订单+" + number +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString() +
                                "\n进行中的订单：" + ttt
                        );
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n加进行中的订单失败");
                    }
                } else if (op.equals("-")) {
                    // 处理电池减去数字的情况
                    try {
                        // 续单数
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        //接单数
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L);
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L) - Optional.ofNullable(number).orElse(0L);
                        // 计算百分比
                        BigDecimal tBigDecimal = BigDecimal.valueOf(t);
                        BigDecimal ttBigDecimal = BigDecimal.valueOf(tt);
                        //BigDecimal ratio = tBigDecimal.multiply(BigDecimal.valueOf(100)).divide(ttBigDecimal, 2, RoundingMode.HALF_UP);
                        BigDecimal ratio = tBigDecimal.divide(ttBigDecimal, 2, RoundingMode.HALF_UP);

                        updateWrapper.eq("name", name).set("existingorder", ttt);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n进行中的订单-" + number +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n接单指数：" + ratio.toPlainString() +
                                "\n进行中的订单：" + ttt
                        );
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n减进行中的订单失败");
                    }
                } else if (op.equals("=")) {
                    // 处理电池=数字
                    try {
                        //已有订单
                        long ttt = Optional.ofNullable(res.getExistingorder()).orElse(0L);
                        updateWrapper.eq("name", name).set("existingorder", number);
                        userMapper.update(null, updateWrapper);
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n进行中的订单=" + number +
                                "\n进行中的订单：" + number
                        );
                        updateGradeByBattery(message.getFrom_group());
                    } catch (Exception e) {
                        msgACT.WebApiClient("", message.getFrom_group(), res.getName() +
                                "\n修改进行中的订单数失败");
                    }
                }
            } catch (Exception e) {
                msgACT.WebApiClient("", message.getFrom_group(), name +
                        "\n修改进行中的订单失败\n没有这个昵称");
            }
        }
        return null;
    }

    @Override
    public ResponseEntity deleteSupervisor(String name, String group) {
        try {
            LambdaQueryWrapper<Userdate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Userdate::getName, name);
            int reg = userMapper.delete(wrapper);
            if (reg == 0) {
                msgACT.WebApiClient("", group, "监督员：" + name + "\n删除失败，没有这个昵称");
            } else {
                msgACT.WebApiClient("", group, "监督员：" + name + "\n删除成功");
            }
        } catch (Exception e) {
            msgACT.WebApiClient("", group, "监督员：" + name + "\n删除失败");
            throw new RuntimeException(e);
        }
        return null;
    }
}
