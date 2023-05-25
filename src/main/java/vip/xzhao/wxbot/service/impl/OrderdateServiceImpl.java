package vip.xzhao.wxbot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xzhao.wxbot.data.Orderdate;
import vip.xzhao.wxbot.mapper.OrderdateMapper;
import vip.xzhao.wxbot.service.OrderdateService;

import java.util.List;

@Service
public class OrderdateServiceImpl extends ServiceImpl<OrderdateMapper, Orderdate> implements OrderdateService {

    @Autowired
    private OrderdateMapper orderdateMapper;

    @Override
    public List<Orderdate> getOrderdateList() {
        return orderdateMapper.selectList(null);
    }
}
