package com.javaweb_study.guidingdog_dashboard_web.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * WebSocket reverse proxy handler for ROS rosbridge.
 * Client connects to /ws/ros, this handler bridges to upstream ws://host:9090
 */
@Component
public class RosbridgeProxyHandler implements WebSocketHandler {

    private final ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Value("${guidingdog.upstream.rosbridge}")
    private String rosbridgeUrl;

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull WebSocketSession inbound) {
        // 添加会话到列表
        sessions.add(inbound);

        // Build upstream target URI
        URI inboundUri = inbound.getHandshakeInfo().getUri();
        URI base = URI.create(rosbridgeUrl);
        String upstream = base.toString();
        if (inboundUri.getQuery() != null && !inboundUri.getQuery().isEmpty()) {
            upstream = upstream + "?" + inboundUri.getQuery();
        }
        URI target = URI.create(upstream);

        return client.execute(target, outbound ->
                Mono.when(
                        // Client -> Upstream
                        outbound.send(
                            inbound.receive()
                                .doOnError(error -> System.err.println("Inbound receive error: " + error.getMessage()))
                                .onErrorContinue((throwable, obj) -> {})
                                .map(msg -> convert(msg, outbound))
                        ),
                        // Upstream -> Client
                        inbound.send(
                            outbound.receive()
                                .doOnError(error -> System.err.println("Outbound receive error: " + error.getMessage()))
                                .onErrorContinue((throwable, obj) -> {})
                                .map(msg -> convert(msg, inbound))
                        )
                )
        ).doOnError(error -> System.err.println("WebSocket proxy error: " + error.getMessage()))
         .doFinally(signalType -> sessions.remove(inbound))
         .onErrorReturn(null);
    }

    private WebSocketMessage convert(WebSocketMessage src, WebSocketSession target) {
        return switch (src.getType()) {
            case TEXT -> target.textMessage(src.getPayloadAsText());
            case BINARY -> {
                DataBuffer payload = src.getPayload();
                try {
                    byte[] bytes = new byte[payload.readableByteCount()];
                    payload.read(bytes);
                    yield target.binaryMessage(factory -> factory.wrap(bytes));
                } finally {
                    DataBufferUtils.release(payload);
                }
            }
            case PING -> target.pingMessage(factory -> {
                DataBuffer payload = src.getPayload();
                try {
                    byte[] bytes = new byte[payload.readableByteCount()];
                    payload.read(bytes);
                    return factory.wrap(bytes);
                } finally {
                    DataBufferUtils.release(payload);
                }
            });
            case PONG -> target.pongMessage(factory -> {
                DataBuffer payload = src.getPayload();
                try {
                    byte[] bytes = new byte[payload.readableByteCount()];
                    payload.read(bytes);
                    return factory.wrap(bytes);
                } finally {
                    DataBufferUtils.release(payload);
                }
            });
            default -> target.textMessage("");
        };
    }

    /**
     * 广播图像数据给所有连接的客户端
     */
    public void broadcastImageData(String imageType, String base64ImageData) {
        String message = "{\"type\":\"" + imageType + "\",\"data\":\"" + base64ImageData + "\"}";
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.send(Mono.just(session.textMessage(message)))
                    .subscribe(
                        null,
                        error -> System.err.println("Failed to broadcast image: " + error.getMessage())
                    );
            }
        }
    }

    /**
     * 获取当前活跃的会话列表
     */
    public List<WebSocketSession> getActiveSessions() {
        return new CopyOnWriteArrayList<>(sessions);
    }
}