package vip.xzhao.wxbot.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.mapper.UserMapper;
import vip.xzhao.wxbot.service.AdminService;

import java.util.List;

@Component
public class BuckleBattery {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MsgACT msgACT;
    @Autowired
    private AdminService adminService;
    @Value("${robot.groupidB}")
    private String GroupId;

    //@Scheduled(cron = "0 */1 * * * ?") // 测试
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void reduceBattery() {
        try {
            List<Userdate> userdates = userMapper.selectList(null);
            for (Userdate userdate : userdates) {
                if (userdate.getState().equals(0) && userdate.getBattery() > 0) {
                    userdate.setBattery(userdate.getBattery() - 1);
                    userMapper.updateById(userdate);
                }
            }
            msgACT.WebApiClient("", GroupId, "每日减1电池执行成功");
            adminService.updateGradeByBattery(GroupId);
        } catch (Exception e) {
            msgACT.WebApiClient("", "25984983585997042@openim", "定时出现报错");
        }
    }
}
