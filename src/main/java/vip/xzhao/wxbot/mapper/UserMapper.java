package vip.xzhao.wxbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import vip.xzhao.wxbot.data.Userdate;

@Mapper
public interface UserMapper extends BaseMapper<Userdate> {
}
