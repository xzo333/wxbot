package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.data.Userdate;
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
    private final Map<String, Map<String, LocalDateTime>> dateMap = new HashMap<>();//记录抢单群数据
    private final Map<String, Timer> timers = new HashMap<>();//定时记录
    @Value("${robot.remindWeChat}")
    private String remindWeChat;
    private Map<String, LocalDateTime> wxidDateMap;//记录抢单wxid和日期

    public DispatchServiceImpl(MsgACT msgACT, UserMapper userMapper) {
        this.msgACT = msgACT;
        this.userMapper = userMapper;
    }

    @Override
    public ResponseEntity<?> order(Message message) {
        log.error(String.valueOf(message));
        String content = message.getMsg();
        String groupId = message.getFrom_group();
        String wxid = message.getFrom_wxid();
        Userdate res;
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getWxid, message.getFrom_wxid());
            res = userMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (res == null) {
            msgACT.WebApiClient("", message.getFrom_group(), "请先设置昵称\n格式[昵称：自定义昵称]\n发闲聊群,包含[]\n示范：[昵称：小高]");
            return null;
        }

        if (content.equals("派单")) {
            if (timers.containsKey(groupId)) {
                // 如果已存在倒计时，给出提示
                msgACT.WebApiClient(message.getFrom_wxid(), groupId, "\n3分钟抢单倒计时还在进行中，请等待倒计时结束后派新单");
            } else {
                log.error(message.getFrom_group_name() + "群开始派单");
                startCountdown(groupId); // 启动倒计时
            }
        } else if (timers.containsKey(groupId) && content.equals("1")) {
            Set<String> wxidSet = wxidMap.getOrDefault(groupId, new HashSet<>());  // 获取该群的已抢单wxid集合
            if (wxidSet.contains(wxid)) {
                // 如果收到了重复的"1"消息并且是相同的wxid，给出提示
                msgACT.WebApiClient(message.getFrom_wxid(), groupId, res.getName() + "\n已经抢单过啦，无需重复抢单");
            } else {
                log.error(res.getName() + "接单");
                recordMessage(groupId, wxid);
            }
        }
        return null;
    }

    private void startCountdown(String groupId) {
        // 启动一个计时器，3分钟后执行
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handleOrderData(groupId);
                timers.remove(groupId);  // 清除计时器
                wxidMap.remove(groupId);  // 清空对应群的已抢单wxid集合,重复接单
            }
        }, 180000);  // 3分钟

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

                    renewalRate = continuation.multiply(new BigDecimal(100)).divide(totalOrders, 2, RoundingMode.HALF_UP);
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
                // 输出续单率最高且最早时间的wxid
                String formattedRenewalRate = maxRenewalRate.setScale(2, RoundingMode.HALF_UP) + "%";
                log.error("最终结果：selectedWxid: " + selectedWxid + ", 最高续单率: " + formattedRenewalRate + ", 最早时间: " + earliestDate);

                // 接单成功
                try {
                    QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(Userdate::getWxid, selectedWxid);
                    Userdate res = userMapper.selectOne(queryWrapper);

                    // 更新订单数量
                    UpdateWrapper<Userdate> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("wxid", selectedWxid).setSql("numberoforders = IFNULL(numberoforders, 0) + 1");
                    userMapper.update(null, updateWrapper);

                    if (res != null) {
                        log.info("续单率最高或最早抢单的是: " + res.getName() + "续单率为：" + formattedRenewalRate);
                        msgACT.WebApiClient(selectedWxid, groupId, res.getName() + "\n抢单成功，您续单率最高或最早抢单的，续单率为：" + formattedRenewalRate);
                    }
                } catch (Exception e) {
                    log.error("抢单出现报错" + e);
                    msgACT.WebApiClient("", groupId, "抢单出现报错");
                    throw new RuntimeException(e);
                }

                // 处理完成后清空指定的群的全部数据
                dateMap.remove(groupId);
                log.error("倒计时结束");
            }
        } else {
            log.error("wxidDateMap为空");
            msgACT.WebApiClient(remindWeChat, groupId, "\n3分钟抢单倒计时结束了，但无人接单");
        }
    }
}