package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.data.Userdate;
import vip.xzhao.wxbot.mapper.UserMapper;
import vip.xzhao.wxbot.service.UserdateService;

import java.util.List;

@Service
public class UserdateServiceImpl extends ServiceImpl<UserMapper, Userdate> implements UserdateService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Userdate> getUserdateList() {
        return userMapper.selectList(null);
    }

    @Override
    public boolean isadmin(String wxid) {
        try {
            QueryWrapper<Userdate> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(Userdate::getWxid, wxid);
            Userdate res = userMapper.selectOne(queryWrapper);
            if (res.getIsadmin() != null && res.getIsadmin() == 1) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
