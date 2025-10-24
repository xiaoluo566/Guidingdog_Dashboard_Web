package com.guidingdog.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEventHttpMessageWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Controller
public class VideoProxyController {

    private final WebClient webClient;
    private final String upstreamBase;

    public VideoProxyController(@Value("${guidingdog.upstream.video}") String base) {
        this.upstreamBase = base;
        this.webClient = WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024)) // 防止缓冲过大
                .build();
    }

    @GetMapping("/video/stream")
    public Mono<ResponseEntity<Flux<DataBuffer>>> stream(
            @RequestParam("topic") String topic,
            @RequestParam(value = "quality", required = false) Integer quality,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height
    ) {
        // 透传到 web_video_server，支持其原生参数
        UriBuilder b = new UriBuilder(upstreamBase + "/stream")
                .add("topic", topic);
        if (quality != null) b.add("quality", String.valueOf(quality));
        if (width != null) b.add("width", String.valueOf(width));
        if (height != null) b.add("height", String.valueOf(height));

        return webClient.get()
                .uri(b.build())
                .retrieve()
                .toEntityFlux(DataBuffer.class)
                .map(e -> {
                    HttpHeaders h = new HttpHeaders();
                    // web_video_server 返回 multipart/x-mixed-replace;boundary=...
                    MediaType ct = e.getHeaders().getContentType();
                    if (ct != null) h.setContentType(ct);
                    return ResponseEntity
                            .status(e.getStatusCode())
                            .headers(h)
                            .body(e.getBody());
                });
    }

    // 简单 URI 构建器
    static class UriBuilder {
        private final StringBuilder sb;
        private boolean first = true;
        UriBuilder(String base) { this.sb = new StringBuilder(base); }
        UriBuilder add(String k, String v) {
            sb.append(first ? "?" : "&");
            first = false;
            sb.append(k).append("=").append(encode(v));
            return this;
        }
        URI build() { return URI.create(sb.toString()); }
        private static String encode(String v) {
            return v.replace(" ", "%20");
        }
    }
}