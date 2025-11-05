package dev.radovanradivojevic.webrtcserver.config;

import dev.radovanradivojevic.webrtcserver.handler.SignalingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


/**
 * WebSocket Configuration for WebRTC Signaling Server
 *
 * Configures the WebSocket endpoint where Android clients connect to exchange signaling messages.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;

    public WebSocketConfig(SignalingHandler signalingHandler) {
        this.signalingHandler = signalingHandler;
    }

    /**
     * Register WebSocket endpoints and handlers
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this.signalingHandler, "/signal")
                .setAllowedOrigins("*");
    }
}