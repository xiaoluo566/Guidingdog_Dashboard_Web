package com.javaweb_study.guidingdog_dashboard_web.ws;

import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * WebSocket handler for real-time image streaming
 * Clients connect to /ws/image to receive radar and camera images
 */
public class ImageStreamHandler implements WebSocketHandler {

    private final List<WebSocketSession> imageSessions = new CopyOnWriteArrayList<>();

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull WebSocketSession session) {
        // 添加客户端连接
        imageSessions.add(session);
        System.out.println("Image stream client connected. Total clients: " + imageSessions.size());

        // 处理客户端发送的消息
        return session.receive()
            .doOnNext(message -> {
                String payload = message.getPayloadAsText();
                System.out.println("Received image request: " + payload);
            })
            .doOnError(error -> System.err.println("Image stream error: " + error.getMessage()))
            .doFinally(signalType -> {
                imageSessions.remove(session);
                System.out.println("Image stream client disconnected. Total clients: " + imageSessions.size());
            })
            .then();
    }

    /**
     * 广播图像数据给所有连接的客户端
     * @param imageType 图像类型 (radar 或 camera)
     * @param base64ImageData Base64 编码的图像数据
     */
    public void broadcastImage(String imageType, String base64ImageData) {
        String message = "{\"type\":\"" + imageType + "\",\"data\":\"" + base64ImageData + "\",\"timestamp\":" + System.currentTimeMillis() + "}";
        for (WebSocketSession session : imageSessions) {
            if (session.isOpen()) {
                session.send(Mono.just(session.textMessage(message)))
                    .subscribe(
                        null,
                        error -> System.err.println("Failed to send image to client: " + error.getMessage())
                    );
            }
        }
    }

    /**
     * 广播雷达图像
     */
    public void broadcastRadarImage(String base64ImageData) {
        broadcastImage("radar", base64ImageData);
    }

    /**
     * 广播摄像头图像
     */
    public void broadcastCameraImage(String base64ImageData) {
        broadcastImage("camera", base64ImageData);
    }

    /**
     * 获取当前连接的客户端数量
     */
    public int getConnectedClientsCount() {
        return imageSessions.size();
    }

    /**
     * 获取所有活跃的会话
     */
    public List<WebSocketSession> getActiveSessions() {
        return new CopyOnWriteArrayList<>(imageSessions);
    }
}

