package vip.xzhao.wxbot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.service.*;
import vip.xzhao.wxbot.util.WxRobot;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    public final MsgACT msgACT;
    private final TellerService tellerService;
    private final AdminService adminService;
    private final AiService aiService;
    private final DispatchService dispatchService;

    private final WxRobot wxRobot;
    @Value("${robot.groupidTest}")
    private String groupidTest;
    @Value("${robot.groupidA}")
    private String GroupIdA;
    @Value("${robot.groupidB}")
    private String GroupIdB;

    @Value("${robot.dispatchGroup}")
    private String dispatchGroup;

    public MessageServiceImpl(MsgACT msgACT, TellerService tellerService, AdminService adminService, AiService aiService, DispatchService dispatchService, WxRobot wxRobot) {
        this.msgACT = msgACT;
        this.tellerService = tellerService;
        this.adminService = adminService;
        this.aiService = aiService;
        this.dispatchService = dispatchService;
        this.wxRobot = wxRobot;
    }


    @Override
    public ResponseEntity handleGroupMsg(Message message) {
        String groupid = message.getFrom_group();
        //获取派单群
        String[] dispatchgroup = dispatchGroup.split(",");
        List<String> dispatchList = Arrays.asList(dispatchgroup);

        /*System.out.println("信息：" + message.getMsg());
        System.out.println("群id" + message.getFrom_group());*/
        //私发测试
        if (groupid == null && "25984983585997042@openim".equals(message.getFrom_wxid())
                && message.getMsg().toLowerCase().startsWith("ai")) {
            log.error("私Ai接收到数据: {}", message);
            aiService.Ai(message);
        }
        //只有指定群才回复
        if (groupid != null) {
            String Msg = message.getMsg();
            if (Msg.equals("查看负电池")) {
                wxRobot.sendToAnotherApi(message);//数据发送到接口
            } else if (groupid.equals(GroupIdA) | groupid.equals(GroupIdB)) {
                //System.out.println("收到：" + message);
                //接单,识别是接单
                /*if (message.getMsg().contains("@") & message.getMsg().contains("接单") &
                        message.getMsg().contains("------") & message.getMsg().contains("订单号：")) {
                    tellerService.Order(message);
                }*/
                //查看电池
                if (message.getMsg().equals("查看电池")) {
                    tellerService.ViewBattery(message);
                }
                //查看排名
                if (message.getMsg().equals("查看排名")) {
                    tellerService.Ranking(message);
                }
                //设置昵称
                Pattern pattern = Pattern.compile("^\\[昵称：(.+?)\\]$");
                Matcher matcher = pattern.matcher(message.getMsg());
                if (matcher.matches()) {
                    String nickname = matcher.group(1).trim();
                    if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                        // 昵称校验通过，执行其他操作
                        tellerService.Name(message);
                    }
                }
                //命令说明
                if (message.getMsg().equals("命令说明")) {
                    tellerService.CommandDescription(message);
                }
                //刷新等级
                if (message.getMsg().equals("刷新等级")) {
                    adminService.updateGradeByBattery(message.getFrom_group());
                }
                //刷新停止晋级等级
                if (message.getMsg().equals("刷新停止晋级等级")) {
                    adminService.RefreshStopPromotionLevel(message.getFrom_group());
                }
                //导出下载
                if (message.getMsg().equals("下载实时数据")) {
                    adminService.DownloadRealTimeData(message);
                }

                //Ai
                if (Msg.toLowerCase().startsWith("ai")) {
                    log.error("Ai接收到数据: {}", message);
                    aiService.Ai(message);
                }

                //管理员
                //小助手 25984984534779134@openim
                //米粒 25984982360415668@openim
                //江月 25984981886553582@openim
                //小高 25984983585997042@openim
                //阿凉 25984985225681406@openim
                if (message.getFrom_wxid().equals("25984984534779134@openim") |
                        message.getFrom_wxid().equals("25984982360415668@openim") |
                        message.getFrom_wxid().equals("25984981886553582@openim") |
                        message.getFrom_wxid().equals("25984983585997042@openim") |
                        message.getFrom_wxid().equals("25984985225681406@openim")) {
                    //=-电池,指定管理员
                    if (message.getMsg().matches("^，.*[+\\-=]\\d+") && !message.getMsg().contains("续单")) {
                        adminService.ModifyBattery(message);
                    }
                    //加续单
                    if (Pattern.matches("^，.*续单\\+\\d+", message.getMsg())) {
                        adminService.modifyContinuedOrderQuantity(message);
                    }
                    //取消订单
                    if (Pattern.matches("^取消订单：(\\d+)。$", message.getMsg())) {
                        adminService.CancelOrder(message);
                    }
                    //停止晋级
                    Pattern patternA = Pattern.compile("^\\[(.+?)\\]停止晋级$");
                    Matcher matcherA = patternA.matcher(message.getMsg());
                    if (matcherA.matches()) {
                        String nickname = matcherA.group(1).trim();
                        if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                            // 昵称校验通过，执行其他操作
                            adminService.FreezeLevel(message);
                        }
                    }
                    //关闭停止晋级
                    Pattern patternB = Pattern.compile("^\\[(.+?)\\]关闭停止晋级$");
                    Matcher matcherB = patternB.matcher(message.getMsg());
                    if (matcherB.matches()) {
                        String nickname = matcherB.group(1).trim();
                        if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                            // 昵称校验通过，执行其他操作
                            adminService.ThawLevel(message);
                        }
                    }
                }
            } else if (dispatchList.contains(message.getFrom_group())) {//指定派单群才执行
                if (message.getMsg().equals("派单") | message.getMsg().equals("1")) {
                    dispatchService.order(message);
                }
            }
        }
        return null;
    }
}
