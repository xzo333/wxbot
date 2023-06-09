package vip.xzhao.wxbot.active;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;
import vip.xzhao.wxbot.data.AIdate;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MsgAi {
    private static final OkHttpClient CLIENT = createOkHttpClient();
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String URL = "https://api.binjie.fun/api/generateStream";
    private static final String REFERER = "https://chat7.aichatos.xyz/";
    private static final String ORIGIN = "https://chat7.aichatos.xyz";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36";
    private static final int TIMEOUT_SECONDS = 5;

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
    }

    public String ai(String msg, String userId) {
        AIdate info = new AIdate();
        info.setPrompt(msg);
        info.setUserId(userId);
        info.setNetwork(true); // 设置为 true
        info.setSystem("");
        info.setWithoutContext(false);
        info.setStream(false);

        String json = JSON.toJSONString(info);
        log.info("Ai请求内容转化为Json：" + json);

        RequestBody body = RequestBody.create(JSON_TYPE, json);
        Request request = new Request.Builder()
                .url(URL)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .header("Host", "api.binjie.fun")
                .header("Connection", "keep-alive")
                .header("Referer", REFERER)
                .header("Origin", ORIGIN) // 增加 Origin 请求头
                .header("User-Agent", USER_AGENT)
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Ai请求失败，响应码：" + response.code());
                return "Ai请求失败";
            }
            String responseBody = getResponseString(response);
            log.info("Ai收到响应：" + responseBody);
            return responseBody;
        } catch (SocketTimeoutException e) {
            log.error("Ai请求超时", e);
            return "Ai请求超时";
        } catch (IOException e) {
            log.error("Ai请求异常", e);
            return "Ai请求异常";
        }
    }

    private String getResponseString(Response response) throws IOException {
        String responseBody = response.body().string();
        response.close();
        return responseBody;
    }
}
