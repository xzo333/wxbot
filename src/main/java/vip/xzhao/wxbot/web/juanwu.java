/*
package vip.xzhao.wxbot.web;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import vip.xzhao.wxbot.active.MsgACT;
import vip.xzhao.wxbot.data.Message;
import vip.xzhao.wxbot.service.MessageService;
import java.util.Map;

@Slf4j
@RestController
public class Juanwu {
    private final MessageService messageService;
    private final AsyncService asyncService;

    public Juanwu(MessageService messageService, AsyncService asyncService, MsgACT msgACT) {
        this.messageService = messageService;
        this.asyncService = asyncService;
    }
    //测试
    @PostMapping("/msg/test")
    public ResponseEntity<String> TestReceivePost(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String requestBody) {
        //log.error("接收到请求头: {}", headers);
        try {
            JSONObject txt = JSONObject.parseObject(requestBody);
            Message message = JSONObject.parseObject(requestBody, Message.class);
            messageService.handleGroupMsg(message);
            log.error("接收到数据: {}", txt);
        } catch (Exception e) {
            log.error("使用阿里巴巴JSON报错: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @PostMapping("/juanwu/msg")
    public ResponseEntity<String> receivePost(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String requestBody) {
        //log.debug("接收到请求头: {}", headers);

        try {
            Message message = JSONObject.parseObject(requestBody, Message.class);
            asyncService.sendToAnotherApi(message); // 异步发送数据到另一个接口
            messageService.handleGroupMsg(message);
        } catch (Exception e) {
            JSONObject txt = JSONObject.parseObject(requestBody);
            log.debug("接收报错的数据是: {}", txt);
            log.debug("处理JSON报错: {}", e.getMessage(), e);
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
*/
