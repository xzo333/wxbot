package vip.xzhao.wxbot.web;

import com.alibaba.fastjson2.JSON;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.service.MessageService;

@RestController
public class juanwu {
    private final MessageService messageService;

    public juanwu(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/juanwu/msg")
    public ResponseEntity<String> receivePost(@RequestBody String requestBody) {
        //System.out.println(requestBody); // 输出
        Message message = JSON.parseObject(requestBody, Message.class);
        try {
            messageService.handleGroupMsg(message);
        } catch (Exception e) {
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
