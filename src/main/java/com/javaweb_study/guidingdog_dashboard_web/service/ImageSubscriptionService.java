package com.javaweb_study.guidingdog_dashboard_web.service;

import com.javaweb_study.guidingdog_dashboard_web.ws.ImageStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service for subscribing to ROS image topics and broadcasting to clients
 */
@Service
public class ImageSubscriptionService {

    @Autowired
    private ImageStreamHandler imageStreamHandler;

    private Timer radarTimer;
    private Timer cameraTimer;

    /**
     * 订阅雷达图像话题
     * 通过 ROS Bridge WebSocket 订阅 /radar/image 话题
     */
    public void subscribeToRadarImage() {
        System.out.println("Subscribing to radar image topic: /radar/image");

        // 启动定时任务模拟接收雷达图像
        if (radarTimer != null) {
            radarTimer.cancel();
        }
        radarTimer = new Timer("RadarImageTimer", true);
        radarTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 这里可以从 ROS 接收实际的图像数据
                // 目前为演示，发送占位符数据
                String radarImageData = generateDummyRadarImage();
                imageStreamHandler.broadcastRadarImage(radarImageData);
            }
        }, 0, 100); // 每 100ms 发送一次（10 FPS）
    }

    /**
     * 订阅摄像头图像话题
     * 通过 ROS Bridge WebSocket 订阅 /camera/image 话题
     */
    public void subscribeToCameraImage() {
        System.out.println("Subscribing to camera image topic: /camera/image");

        // 启动定时任务模拟接收摄像头图像
        if (cameraTimer != null) {
            cameraTimer.cancel();
        }
        cameraTimer = new Timer("CameraImageTimer", true);
        cameraTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // 这里可以从 ROS 接收实际的图像数据
                // 目前为演示，发送占位符数据
                String cameraImageData = generateDummyCameraImage();
                imageStreamHandler.broadcastCameraImage(cameraImageData);
            }
        }, 0, 33); // 每 33ms 发送一次（约 30 FPS）
    }

    /**
     * 取消订阅所有图像话题
     */
    public void unsubscribeFromImages() {
        if (radarTimer != null) {
            radarTimer.cancel();
            radarTimer = null;
        }
        if (cameraTimer != null) {
            cameraTimer.cancel();
            cameraTimer = null;
        }
        System.out.println("Unsubscribed from image topics");
    }

    /**
     * 生成演示用的雷达图像数据（Base64 编码）
     * 实际应用中应该从 ROS 接收真实的图像数据
     */
    private String generateDummyRadarImage() {
        // 这里可以替换为实际从 ROS 接收的图像数据
        byte[] dummyData = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}; // JPEG 头部
        return Base64.getEncoder().encodeToString(dummyData);
    }

    /**
     * 生成演示用的摄像头图像数据（Base64 编码）
     * 实际应用中应该从 ROS 接收真实的图像数据
     */
    private String generateDummyCameraImage() {
        // 这里可以替换为实际从 ROS 接收的图像数据
        byte[] dummyData = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}; // JPEG 头部
        return Base64.getEncoder().encodeToString(dummyData);
    }

    /**
     * 手动推送图像数据
     */
    public void publishRadarImage(byte[] imageData) {
        String base64Data = Base64.getEncoder().encodeToString(imageData);
        imageStreamHandler.broadcastRadarImage(base64Data);
    }

    public void publishCameraImage(byte[] imageData) {
        String base64Data = Base64.getEncoder().encodeToString(imageData);
        imageStreamHandler.broadcastCameraImage(base64Data);
    }
}

