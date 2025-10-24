package com.javaweb_study.guidingdog_dashboard_web.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping wsMapping(RosbridgeProxyHandler rosbridgeProxyHandler, ImageStreamHandler imageStreamHandler) {
        Map<String, Object> map = new HashMap<>();
        map.put("/ws/ros", rosbridgeProxyHandler);
        map.put("/ws/image", imageStreamHandler);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(-1); // 高优先级
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService();
    }

    @Bean
    public ImageStreamHandler imageStreamHandler() {
        return new ImageStreamHandler();
    }
}