package dev.radovanradivojevic.webrtcserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.radovanradivojevic.webrtcserver.model.CallMessage;
import dev.radovanradivojevic.webrtcserver.model.RegisterMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignalingHandlerTest {

    private SignalingHandler handler;
    private ObjectMapper objectMapper;
    private WebSocketSession dadSession;

    @BeforeEach
    void setUp() {
        // Create real ObjectMapper (we need real JSON processing)
        objectMapper = new ObjectMapper();

        // Create the handler we're testing
        handler = new SignalingHandler(objectMapper);

        // Create a mock WebSocketSession
        dadSession = mock(WebSocketSession.class);

        // Configure the mock: when getId() is called, return "session123"
        when(dadSession.getId()).thenReturn("session123");
        when(dadSession.isOpen()).thenReturn(true);


    }

    @Test
    void testRegistrationStoresSessionCorrectly() throws Exception {
        // ARRANGE: Prepare the registration message
        RegisterMessage registerMessage = new RegisterMessage("dad");
        String json = objectMapper.writeValueAsString(registerMessage);
        TextMessage textMessage = new TextMessage(json);

        // ACT: Process the registration
        handler.handleTextMessage(dadSession, textMessage);

        // ASSERT: Verify both maps are updated correctly

        // 1. Can we retrieve the session by userId?
        assertNotNull(handler.getSession("dad"),
                "Session should be retrievable by userId 'dad'");
        assertEquals(dadSession, handler.getSession("dad"),
                "The stored session should be the same mock session we passed in");

        // 2. Can we retrieve userId by sessionId?
        assertEquals("dad", handler.getUserId("session123"),
                "UserId 'dad' should be retrievable by sessionId 'session123'");

        assertEquals(dadSession, handler.getSession("dad"), "The stored session should be the same mock session we passed in");
    }

    @Test
    void testMessageForwardingToRecipient() throws Exception {
        // ARRANGE: Set up TWO users (dad and son)

        // Create a second mock session for "son"
        WebSocketSession sonSession = mock(WebSocketSession.class);

        // Configure sonSession.getId() to return "sonSession123"
        when(sonSession.getId()).thenReturn("sonSession123");
        // Configure sonSession.isOpen() to return true
        when(sonSession.isOpen()).thenReturn(true);

        // Register dad (reuse the dadSession from setUp - that's dad's session)
        RegisterMessage registerMessageDad = new RegisterMessage("dad");
        String jsonDad = objectMapper.writeValueAsString(registerMessageDad);
        TextMessage textMessageDad = new TextMessage(jsonDad);

        // Register son with sonSession
        RegisterMessage registerMessageSon = new RegisterMessage("son");
        String jsonSon = objectMapper.writeValueAsString(registerMessageSon);
        TextMessage textMessageSon = new TextMessage(jsonSon);

        handler.handleTextMessage(dadSession, textMessageDad);
        handler.handleTextMessage(sonSession, textMessageSon);

        // ACT: Dad sends an offer to son

        // Create a CallMessage with type="offer", to="son", sdp="fake-sdp-data"
        CallMessage callMessage = new CallMessage("offer", "son", "fake-sdp-data");

        // Convert CallMessage to JSON string
        String jsonCallMessage = objectMapper.writeValueAsString(callMessage);

        // Wrap JSON in TextMessage
        TextMessage textMessageCallMessage = new TextMessage(jsonCallMessage);

        // Call handler.handleTextMessage with dad's session and the message
        handler.handleTextMessage(dadSession, textMessageCallMessage);
        // ASSERT: Verify son received the message with the "from" field added

        // Create expected message with "from" field populated by server
        CallMessage expectedMessage = new CallMessage("offer", "dad", "son", "fake-sdp-data");
        String expectedJson = objectMapper.writeValueAsString(expectedMessage);

        // Verify that sonSession.sendMessage() was called with the modified message
        verify(sonSession).sendMessage(new TextMessage(expectedJson));
    }

    @Test
    void testDisconnectionCleansUpMaps() throws Exception {
        // ARRANGE: Register dad
        RegisterMessage registerMessageDad = new RegisterMessage("dad");
        String jsonDad = objectMapper.writeValueAsString(registerMessageDad);
        TextMessage textMessageDad = new TextMessage(jsonDad);
        handler.handleTextMessage(dadSession, textMessageDad);

        // Verify dad is registered BEFORE disconnecting
        assertNotNull(handler.getSession("dad"), "Dad should be registered before disconnecting");

        // ACT: Dad disconnects
        handler.afterConnectionClosed(dadSession, CloseStatus.NORMAL);

        // ASSERT: Verify both maps are cleaned up
        assertNull(handler.getSession("dad"), "Session should be removed after disconnection");
        assertNull(handler.getUserId("session123"), "UserId mapping should be removed after disconnection");
    }
}