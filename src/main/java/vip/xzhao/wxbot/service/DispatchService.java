package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;

public interface DispatchService {
    /**
     * 接单
     */
    ResponseEntity order(Message message);
}
