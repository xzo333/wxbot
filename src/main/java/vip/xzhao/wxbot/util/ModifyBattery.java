package vip.xzhao.wxbot.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.mapper.OrderdateMapper;
import vip.xzhao.wxbot.mapper.UserMapper;
import vip.xzhao.wxbot.service.AdminService;

@Slf4j
@Service
public class ModifyBattery {
    public final UserMapper userMapper;
    public final OrderdateMapper orderdateMapper;
    public final MsgACT msgACT;

    public ModifyBattery(UserMapper userMapper, OrderdateMapper orderdateMapper, MsgACT msgACT) {
        this.userMapper = userMapper;
        this.orderdateMapper = orderdateMapper;
        this.msgACT = msgACT;
    }

    //权值修改电池
    public String ModifyBattery(String group, String name, String op,Long number) {
        log.info("修改电池: jname=" + name + ", op=" + op + ", num=" + number);
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
                    msgACT.WebApiClient("", group, res.getName() +
                            "\n加电池" + number +
                            "\n现电池:" + t +
                            "\n原等级:" + res.getGrade() +
                            "\n卷卷再接再厉");
                    updateGradeByBattery(group);
                } catch (Exception e) {
                    log.error(String.valueOf(e));
                    msgACT.WebApiClient("", group, res.getName() +
                            "\n加电池失败");
                }
            } else if (op.equals("-")) {
                // 处理电池减去数字的情况
                try {
                    long t = res.getBattery() - number;
                    updateWrapper.eq("name", name).set("battery", t);
                    userMapper.update(null, updateWrapper);
                    msgACT.WebApiClient("", group, res.getName() +
                            "\n减电池" + number +
                            "\n现电池:" + t +
                            "\n原等级:" + res.getGrade() +
                            "\n卷卷再接再厉");
                    updateGradeByBattery(group);
                } catch (Exception e) {
                    log.error(String.valueOf(e));
                    msgACT.WebApiClient("", group, res.getName() +
                            "\n减电池失败");
                }
            } else if (op.equals("=")) {
                // 处理电池=数字
                try {
                    updateWrapper.eq("name", name).set("battery", number).set("historicalbattery", number);
                    userMapper.update(null, updateWrapper);
                    msgACT.WebApiClient("", group, res.getName() +
                            "\n现电池:" + number +
                            "\n原等级:" + res.getGrade() +
                            "\n卷卷再接再厉");
                    updateGradeByBattery(group);
                } catch (Exception e) {
                    log.error(String.valueOf(e));
                    msgACT.WebApiClient("", group, res.getName() +
                            "\n修改电池失败");
                }
            }
        } catch (Exception e) {
            msgACT.WebApiClient("", group, name +
                    "\n修改电池失败\n没有这个昵称");
        }
        return null;
    }

    //刷新等级
    public String updateGradeByBattery(String GroupId) {
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
}
