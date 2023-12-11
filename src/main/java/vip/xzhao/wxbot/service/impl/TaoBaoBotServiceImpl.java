package vip.xzhao.wxbot.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.data.WxMessage;
import vip.xzhao.wxbot.service.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TaoBaoBotServiceImpl implements TaoBaoBotService {
    @Value("${robot.dispatchGroup}")
    private String dispatchGroup;
    @Resource
    private DispatchService dispatchService;
    @Resource
    private TellerService tellerService;
    @Resource
    private AdminService adminService;
    @Resource
    private UserdateService userdateService;

    @Override
    public void handleGroupMsg(WxMessage wxMessage) {
        //获取群
        String groupid = wxMessage.getGroupId();
        //获取发信息微信id
        String wid = wxMessage.getUserId();
        //获取配置里的派单群
        String[] dispatchgroup = dispatchGroup.split(",");
        List<String> dispatchList = Arrays.asList(dispatchgroup);

        //指定群才执行
        if (dispatchList.contains(groupid)) {
            //消息
            String msg = wxMessage.getContent();

            //派单 倒计时内 回复1接单
            if (msg.equals("派单") | msg.equals("1")) {
                dispatchService.order(wxMessage);
            }
            //查看电池
            if (msg.equals("查看电池")) {
                tellerService.ViewBattery(wxMessage);
            }
            //查看排名
            if (msg.equals("查看排名")) {
                tellerService.Ranking(wxMessage);
            }
            //设置昵称
            String msgname = msg.replace("昵称:", "昵称：");
            Pattern pattern = Pattern.compile("^\\[昵称：(.+?)\\]$");
            Matcher matcher = pattern.matcher(msgname);
            if (matcher.matches()) {
                String nickname = matcher.group(1).trim();
                if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                    // 昵称校验通过，执行其他操作
                    tellerService.Name(wxMessage);
                }
            }
            /*//命令说明
            if (message.getMsg().equals("命令说明")) {
                tellerService.CommandDescription(message);
            }*/
            //刷新等级
            if (msg.equals("刷新等级")) {
                adminService.updateGradeByBattery(groupid);
            }
            //刷新停止晋级等级
            if (msg.equals("刷新停止晋级等级")) {
                adminService.RefreshStopPromotionLevel(groupid);
            }
            //导出下载
            if (msg.equals("下载实时数据")) {
                adminService.DownloadRealTimeData(groupid);
            }

            //判断是否是管理员
            boolean isadmin = userdateService.isadmin(wid);
            if (isadmin){
                //修改权值
                if (Pattern.matches("^，.*权值[=\\-\\+]\\d+", msg)) {
                    adminService.modifyContinuedOrderQuantity(wid,groupid,msg);
                }
                //修改电池
                if (Pattern.matches("^，.*电池[=\\-\\+]\\d+", msg)) {
                    adminService.ModifyBattery(wid,groupid,msg);
                }
                //修改接单
                if (Pattern.matches("^，.*订单[=\\-\\+]\\d+", msg)) {
                    adminService.modifyReceipt(wid,groupid,msg);
                }
               /* //修改已有订单数量
                if (Pattern.matches("^，.*进行中的订单[=\\-\\+]\\d+", msg)) {
                    adminService.modifyAnExistingOrder(wid,groupid,msg);
                }*/

                //删监督员
                Pattern deletename = Pattern.compile("^\\[删监督员：(.+?)\\]$");
                Matcher deletemsg = deletename.matcher(msgname);
                if (deletemsg.matches()) {
                    String nickname = deletemsg.group(1).trim();
                    if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                        // 昵称校验通过，执行其他操作
                        adminService.deleteSupervisor(nickname,groupid);
                    }
                }
                //设管理员
                Pattern adminname = Pattern.compile("^\\[设置管理员：(.+?)\\]$");
                Matcher adminmsg = adminname.matcher(msgname);
                if (adminmsg.matches()) {
                    String nickname = adminmsg.group(1).trim();
                    if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                        // 昵称校验通过，执行其他操作
                        adminService.setupAdministrator(nickname,groupid);
                    }
                }
                //查看管理员
                if (Pattern.matches("查看管理员", msg)) {
                    adminService.viewAdministrator(groupid);
                }
                /*//停止晋级
                Pattern patternA = Pattern.compile("^\\[(.+?)\\]停止晋级$");
                Matcher matcherA = patternA.matcher(msg);
                if (matcherA.matches()) {
                    String nickname = matcherA.group(1).trim();
                    if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                        // 昵称校验通过，执行其他操作
                        adminService.FreezeLevel(message);
                    }
                }
                //关闭停止晋级
                Pattern patternB = Pattern.compile("^\\[(.+?)\\]关闭停止晋级$");
                Matcher matcherB = patternB.matcher(msg);
                if (matcherB.matches()) {
                    String nickname = matcherB.group(1).trim();
                    if (nickname.matches("^[\\u4e00-\\u9fa5_a-zA-Z0-9]+$")) {
                        // 昵称校验通过，执行其他操作
                        adminService.ThawLevel(message);
                    }
                }*/
            }
        }
    }
}
