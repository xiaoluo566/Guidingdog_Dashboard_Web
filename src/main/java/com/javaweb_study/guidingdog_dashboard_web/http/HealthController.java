package com.guidingdog.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@RestController
public class HealthController {

    @Value("${guidingdog.upstream.video}")
    private String videoBase;

    @GetMapping("/api/health")
    public ResponseEntity<?> health() {
        // 轻量探测 video 上游是否可达（rosbridge 在前端自报 ws 状态）
        try {
            URL url = URI.create(videoBase).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestMethod("HEAD");
            int code = conn.getResponseCode();
            return ResponseEntity.ok().body("{\"video_upstream\":" + (code > 0) + "}");
        } catch (Exception e) {
            return ResponseEntity.ok().body("{\"video_upstream\":false}");
        }
    }
}