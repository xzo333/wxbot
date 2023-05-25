package vip.xzhao.wxbot.service;

import org.springframework.http.ResponseEntity;
import vip.xzhao.wxbot.data.Message;

public interface MessageService {
    ResponseEntity handleGroupMsg(Message message);
}
