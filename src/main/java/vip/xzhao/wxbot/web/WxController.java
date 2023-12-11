package vip.xzhao.wxbot.web;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vip.xzhao.wxbot.data.WxMessage;
import vip.xzhao.wxbot.service.TaoBaoBotService;

import javax.annotation.Resource;

@Slf4j
@Controller
public class WxController {
    @Resource
    private TaoBaoBotService taoBaoBotService;

    @PostMapping("/wx")
    public ResponseEntity<String> receivePost(@RequestBody String requestBody) {
        try {
            JSONObject txt = JSONObject.parseObject(requestBody);
            log.error("接收到数据: {}", txt);
        } catch (Exception e) {
            log.error("使用阿里巴巴JSON报错: {}", e.getMessage(), e);
        }

        try {
            WxMessage wxMessage = JSONObject.parseObject(requestBody, WxMessage.class);
            taoBaoBotService.handleGroupMsg(wxMessage);
        } catch (Exception e) {
            log.error("处理JSON报错: {}", e.getMessage(), e);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}

