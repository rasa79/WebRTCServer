package dev.radovanradivojevic.webrtcserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.radovanradivojevic.webrtcserver.config.WebSocketConfig;
import dev.radovanradivojevic.webrtcserver.model.CallMessage;
import dev.radovanradivojevic.webrtcserver.model.EndCallMessage;
import dev.radovanradivojevic.webrtcserver.model.IceCandidateMessage;
import dev.radovanradivojevic.webrtcserver.model.RegisterMessage;
import dev.radovanradivojevic.webrtcserver.model.SignalingMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SignalingHandler - Handles WebSocket connections for WebRTC signaling
 *
 * Purpose: This handler manages WebSocket connections from Android clients
 * and routes signaling messages between them to establish WebRTC peer connections.
 *
 * Architecture:
 * - Extends TextWebSocketHandler (handles text-based WebSocket messages)
 * - Maintains an in-memory map of connected sessions
 * - Routes messages from one client to another
 *
 * Key Concept: This is NOT handling video/audio data! This only handles
 * the "signaling" - exchanging connection information so devices can
 * establish a direct peer-to-peer connection for the actual media.
 */
@Component
public class SignalingHandler extends TextWebSocketHandler {

    /**
     * Store active WebSocket sessions mapped by userId
     *
     * QUESTION: Why ConcurrentHashMap instead of regular HashMap?
     * HINT: Think about multiple devices connecting/disconnecting simultaneously
     *
     * Structure: Map<userId, WebSocketSession>
     * Example: {"dad" -> session1, "son" -> session2}
     */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public SignalingHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Called when a new WebSocket connection is established
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New WebSocket connection established: " + session.getId());
    }

    /**
     * Called when a WebSocket message is received from a client
     * Handles registration and message routing between peers
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received message: " + payload);

        SignalingMessage signalingMessage = objectMapper.readValue(payload, SignalingMessage.class);

        // Use instanceof to determine message type instead of checking the type field
        if (signalingMessage instanceof RegisterMessage) {
            handleRegister(session, (RegisterMessage) signalingMessage);
        } else if (signalingMessage instanceof CallMessage) {
            handleCallMessage(session, (CallMessage) signalingMessage);
        } else if (signalingMessage instanceof IceCandidateMessage) {
            handleIceCandidate(session, (IceCandidateMessage) signalingMessage);
        } else if (signalingMessage instanceof EndCallMessage) {
            handleEndCall(session, (EndCallMessage) signalingMessage);
        } else {
            System.err.println("Unknown message type: " + signalingMessage.getClass().getName());
        }
    }

    private void handleRegister(WebSocketSession session, RegisterMessage registerMessage) throws IOException {
        String userId = registerMessage.getUserId();
        sessions.put(userId, session);
        sessionToUser.put(session.getId(), userId);
        System.out.println("User registered: " + userId);

        // Send acknowledgment
        String ackMessage = "{\"type\":\"registered\",\"userId\":\"" + userId + "\"}";
        session.sendMessage(new TextMessage(ackMessage));
    }

    private void handleCallMessage(WebSocketSession session, CallMessage callMessage) throws IOException {
        // Get sender's userId
        String senderId = sessionToUser.get(session.getId());
        if (senderId == null) {
            System.err.println("Sender not registered: " + session.getId());
            return;
        }

        // Set the "from" field
        callMessage.setFrom(senderId);

        // Get recipient's session
        WebSocketSession recipientSession = sessions.get(callMessage.getTo());
        if (recipientSession != null && recipientSession.isOpen()) {
            // Convert message to JSON and send
            String forwardedMessage = objectMapper.writeValueAsString(callMessage);
            recipientSession.sendMessage(new TextMessage(forwardedMessage));
            System.out.println("Forwarded call message from " + senderId + " to " + callMessage.getTo());
        } else {
            System.err.println("Recipient not found or offline: " + callMessage.getTo());
        }
    }

    private void handleIceCandidate(WebSocketSession session, IceCandidateMessage iceCandidateMessage) throws IOException {
        // Get sender's userId
        String senderId = sessionToUser.get(session.getId());
        if (senderId == null) {
            System.err.println("Sender not registered: " + session.getId());
            return;
        }

        // Set the "from" field
        iceCandidateMessage.setFrom(senderId);

        // Get recipient's session
        WebSocketSession recipientSession = sessions.get(iceCandidateMessage.getTo());
        if (recipientSession != null && recipientSession.isOpen()) {
            // Convert message to JSON and send
            String forwardedMessage = objectMapper.writeValueAsString(iceCandidateMessage);
            recipientSession.sendMessage(new TextMessage(forwardedMessage));
            System.out.println("Forwarded ICE candidate from " + senderId + " to " + iceCandidateMessage.getTo());
        } else {
            System.err.println("Recipient not found or offline: " + iceCandidateMessage.getTo());
        }
    }

    private void handleEndCall(WebSocketSession session, EndCallMessage endCallMessage) throws IOException {
        // Get sender's userId
        String senderId = sessionToUser.get(session.getId());
        if (senderId == null) {
            System.err.println("Sender not registered: " + session.getId());
            return;
        }

        // Set the "from" field
        endCallMessage.setFrom(senderId);

        // Get recipient's session
        WebSocketSession recipientSession = sessions.get(endCallMessage.getTo());
        if (recipientSession != null && recipientSession.isOpen()) {
            // Convert message to JSON and send
            String forwardedMessage = objectMapper.writeValueAsString(endCallMessage);
            recipientSession.sendMessage(new TextMessage(forwardedMessage));
            System.out.println("Forwarded end-call from " + senderId + " to " + endCallMessage.getTo());
        } else {
            System.err.println("Recipient not found or offline: " + endCallMessage.getTo());
        }
    }

    /**
     * Called when a WebSocket connection is closed
     * Cleans up session mappings
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket connection closed: " + session.getId());

        String userId = sessionToUser.remove(session.getId());
        if (userId != null) {
            sessions.remove(userId);
            System.out.println("User disconnected: " + userId);
        }
    }

    /**
     * Called when an error occurs in the WebSocket connection
     * Logs the error and closes the session
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket error for session " + session.getId());
        exception.printStackTrace();
        session.close();
    }

    /**
     * Get session by userId (for testing)
     */
    public WebSocketSession getSession(String userId) {
        return sessions.get(userId);
    }

    /**
     * Get userId by sessionId (for testing)
     */
    public String getUserId(String sessionId) {
        return sessionToUser.get(sessionId);
    }
}