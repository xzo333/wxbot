package vip.xzhao.wxbot.web;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.service.MessageService;
import java.util.Map;

@Slf4j
@RestController
public class Juanwu {
    private final MessageService messageService;

    public Juanwu(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/juanwu/msg")
    public ResponseEntity<String> receivePost(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String requestBody) {
        log.error("接收到请求头: {}", headers);
        try {
            JSONObject txt = JSONObject.parseObject(requestBody);
            log.error("接收到数据: {}", txt);
        } catch (Exception e) {
            log.error("使用阿里巴巴JSON报错: {}", e.getMessage(), e);
        }


        try {
            Message message = JSONObject.parseObject(requestBody, Message.class);
            messageService.handleGroupMsg(message);
        } catch (Exception e) {
            log.error("处理JSON报错: {}", e.getMessage(), e);
            return new ResponseEntity<>("OK", HttpStatus.OK);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @GetMapping("/juanwu/msg")
    public ResponseEntity<String> receiveGet(@RequestParam Map<String, String> params) {
        if (params != null){
            log.error("接收到Get数据: {}", params);
        }
        // 处理请求
        return new ResponseEntity<>("Get", HttpStatus.OK);
    }
}
