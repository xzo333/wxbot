package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.active.MsgApi;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.data.WxMessage;
import vip.xzhao.wxbot.mapper.UserMapper;
import vip.xzhao.wxbot.service.DispatchService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
public class DispatchServiceImpl implements DispatchService {

    private static final Map<String, Set<String>> wxidMap = new HashMap<>();  // 记录每个群的已抢单wxid集合
    public final MsgACT msgACT;
    public final UserMapper userMapper;
    private final MsgApi msgApi;
    private final Map<String, Map<String, LocalDateTime>> dateMap = new HashMap<>();//记录抢单群数据
    private final Map<String, Timer> timers = new HashMap<>();//定时记录
    @Value("${robot.remindWeChat}")
    private String remindWeChat;
    private Map<String, LocalDateTime> wxidDateMap;//记录抢单wxid和日期

    public DispatchServiceImpl(MsgACT msgACT, MsgApi msgApi, UserMapper userMapper) {
        this.msgACT = msgACT;
        this.msgApi = msgApi;
        this.userMapper = userMapper;
    }

    @Override
    public String order(WxMessage message) {
        //信息
        String content = message.getContent();
        //群Id
        String groupId = message.getGroupId();
        //发信息微信id
        String wid = message.getUserId();

        QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Userdate::getWxid, wid);
        Userdate res = userMapper.selectOne(queryWrapper);

        if (res == null) {
            String[] atwx = {wid};
            msgApi.WebApiClient(groupId, "\n请先发注册命令到指令群\n才能抢单\n格式[昵称：自定义昵称]\n示范：[昵称：小高]", atwx);
            //msgACT.WebApiClient(message.getFrom_wxid(), message.getFrom_group(), "\n请先发注册命令到指令群\n才能抢单\n格式[昵称：自定义昵称]\n示范：[昵称：小高]");
            return null;
        }

        if (content.equals("派单")) {
            if (timers.containsKey(groupId)) {
                // 如果已存在倒计时，给出提示
                //msgACT.WebApiClient(message.getFrom_wxid(), groupId, "可夏\n请等待上一单结束后派新单");
                String[] atwx = {wid};
                msgApi.WebApiClient(groupId, "\n请等待上一单结束后派新单", atwx);

            } else {
                log.error(message.getGroupName() + "群开始派单");
                startCountdown(groupId); // 启动倒计时
            }
        } else if (timers.containsKey(groupId) && content.equals("1")) {
            String level = res.getGrade(); // 从数据库获取用户等级

            Set<String> wxidSet = wxidMap.getOrDefault(groupId, new HashSet<>());  // 获取该群的已抢单wxid集合
            if (wxidSet.contains(wid)) {
                // 如果收到了重复的"1"消息并且是相同的wxid，给出提示
                String[] atwx = {wid};
                msgApi.WebApiClient(groupId, "\n已经抢单过啦，无需重复抢单", atwx);
            } else {
                log.error(res.getName() + "接单");
                recordMessage(groupId, wid);
            }
        }
        return null;
    }

    private void startCountdown(String groupId) {
        // 启动一个计时器，1分钟后执行
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleOrderData(groupId);
                timers.remove(groupId);  // 清除计时器
                wxidMap.remove(groupId);  // 清空对应群的已抢单wxid集合,重复接单
            }
        }, 50000);  // 1分钟

        timers.put(groupId, timer);  // 记录计时器
    }

    private void recordMessage(String groupId, String wxid) {
        Set<String> wxidSet = wxidMap.getOrDefault(groupId, new HashSet<>());  // 获取该群的已抢单wxid集合
        wxidSet.add(wxid);  // 将wxid添加到已抢单wxid集合中
        wxidMap.put(groupId, wxidSet);  // 判断重复接单

        // 时间
        LocalTime currentTime = LocalTime.now();
        LocalDate currentDate = LocalDate.now();
        LocalDateTime dateTime = LocalDateTime.of(currentDate, currentTime);
        // 记录群的wxid和日期的映射关系
        wxidDateMap = dateMap.computeIfAbsent(groupId, k -> new HashMap<>());
        wxidDateMap.put(wxid, dateTime);  // 记录wxid和当前日期的映射关系
    }

    private void handleOrderData(String groupId) {
        // 获取指定群的wxid和时间
        Map<String, LocalDateTime> wxidDateMap = dateMap.get(groupId);

        if (wxidDateMap != null) {
            LocalDateTime earliestDate = null;
            String selectedWxid = null;
            BigDecimal maxRenewalRate = null;
            boolean isFirst = true;

            for (Map.Entry<String, LocalDateTime> entry : wxidDateMap.entrySet()) {
                String wxid = entry.getKey();
                LocalDateTime wxdate = entry.getValue();

                QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(Userdate::getWxid, wxid);
                Userdate res = userMapper.selectOne(queryWrapper);

                log.error("wxidDateMap数据是" + wxidDateMap);
                log.error(res.getName() + ", 抢单时间: " + wxdate);

                BigDecimal renewalRate;
                if (res.getContinuation() != null && res.getNumberoforders() != null && res.getNumberoforders() != 0) {
                    BigDecimal continuation = new BigDecimal(res.getContinuation());
                    BigDecimal totalOrders = new BigDecimal(res.getNumberoforders());

                    //renewalRate = continuation.multiply(new BigDecimal(100)).divide(totalOrders, 2, RoundingMode.HALF_UP);
                    renewalRate = continuation.divide(totalOrders, 2, RoundingMode.HALF_UP);

                } else {
                    renewalRate = BigDecimal.ZERO;
                }

                if (isFirst || renewalRate.compareTo(maxRenewalRate) > 0 || (renewalRate.compareTo(maxRenewalRate) == 0 && wxdate.isBefore(earliestDate))) {
                    maxRenewalRate = renewalRate;
                    earliestDate = wxdate;
                    selectedWxid = wxid;
                    isFirst = false;
                    log.debug("对比续单率，earliestDate: " + earliestDate + ", selectedWxid: " + selectedWxid);
                }
            }

            if (selectedWxid != null) {
                //订单指数
                String formattedRenewalRate = String.valueOf(maxRenewalRate);
                // 输出续单率最高且最早时间的wxid
               /* String formattedRenewalRate = maxRenewalRate.setScale(2, RoundingMode.HALF_UP) + "%";
                ratio = t.multiply(new BigDecimal("100")).divide(tt, 2, RoundingMode.HALF_UP);*/

                log.error("最终结果：selectedWxid: " + selectedWxid + ", 最高续单率: " + formattedRenewalRate + ", 最早时间: " + earliestDate);

                // 接单成功
                try {
                    QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(Userdate::getWxid, selectedWxid);
                    Userdate res = userMapper.selectOne(queryWrapper);

                    // 更新订单数量
                    UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("wxid", selectedWxid).setSql("numberoforders = IFNULL(numberoforders, 0) + 1").setSql("existingorder = IFNULL(existingorder, 0) + 1");
                    userMapper.update(null, updateWrapper);

                    if (res != null) {
                        long t = Optional.ofNullable(res.getContinuation()).orElse(0L);
                        long tt = Optional.ofNullable(res.getNumberoforders()).orElse(0L) + 1;

                        log.info("续单率最高或最早抢单的是: " + res.getName() + "续单率为：" + formattedRenewalRate);
                        /*msgACT.WebApiClient(selectedWxid, groupId, res.getName() +
                                "\n订单+1" +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n原接单指数：" + formattedRenewalRate);*/
                        //使用自己机器人
                        String[] atwx = {res.getWxid()};
                        msgApi.WebApiClient(groupId, "\n" + res.getName() +
                                "\n订单+1" +
                                "\n权值：" + t +
                                "\n总订单：" + tt +
                                "\n原接单指数：" + formattedRenewalRate, atwx);
                    }
                } catch (Exception e) {
                    log.error("抢单出现报错" + e);
                    /*msgACT.WebApiClient("", groupId, "抢单出现报错");*/
                    msgApi.WebApiClient(groupId, "抢单出现报错", null);
                    throw new RuntimeException(e);
                }

                // 处理完成后清空指定的群的全部数据
                dateMap.remove(groupId);
                log.error("倒计时结束");
            }
        } else {
            log.error("wxidDateMap为空");
            //msgACT.WebApiClient(remindWeChat, groupId, "可夏\n无人接单，请及时处理");
            String[] atwx = {remindWeChat};
            msgApi.WebApiClient(groupId, "\n无人接单，请及时处理", atwx);
        }
    }
}