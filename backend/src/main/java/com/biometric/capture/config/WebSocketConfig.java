package com.biometric.capture.config;

import com.biometric.capture.websocket.CaptureWebSocketHandler;
import com.biometric.capture.websocket.DashboardWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    // A single JPEG capture frame (base64) comfortably fits under 1MB, but Tomcat's default
    // WebSocket text buffer is only 8KB — real webcam frames (unlike tiny test images) blew
    // past that and the container silently closed the connection with code 1009, so the
    // client never saw another analysis result after the first real frame.
    private static final int MAX_MESSAGE_BUFFER_SIZE = 1024 * 1024;

    private final CaptureWebSocketHandler captureWebSocketHandler;
    private final DashboardWebSocketHandler dashboardWebSocketHandler;

    public WebSocketConfig(CaptureWebSocketHandler captureWebSocketHandler,
                            DashboardWebSocketHandler dashboardWebSocketHandler) {
        this.captureWebSocketHandler = captureWebSocketHandler;
        this.dashboardWebSocketHandler = dashboardWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(captureWebSocketHandler, "/ws/capture").setAllowedOriginPatterns("*");
        registry.addHandler(dashboardWebSocketHandler, "/ws/dashboard").setAllowedOriginPatterns("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(MAX_MESSAGE_BUFFER_SIZE);
        container.setMaxBinaryMessageBufferSize(MAX_MESSAGE_BUFFER_SIZE);
        return container;
    }
}
