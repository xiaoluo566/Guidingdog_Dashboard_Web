package com.javaweb_study.guidingdog_dashboard_web.http;

import com.javaweb_study.guidingdog_dashboard_web.service.ImageSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing image streaming
 */
@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")
public class ImageController {

    @Autowired
    private ImageSubscriptionService imageSubscriptionService;

    /**
     * 启动图像推送
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startImageStreaming() {
        try {
            imageSubscriptionService.subscribeToRadarImage();
            imageSubscriptionService.subscribeToCameraImage();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Image streaming started");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 停止图像推送
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopImageStreaming() {
        try {
            imageSubscriptionService.unsubscribeFromImages();

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Image streaming stopped");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取图像推送状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getImageStreamStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "active");
        response.put("message", "Image streaming service is running");
        return ResponseEntity.ok(response);
    }
}

