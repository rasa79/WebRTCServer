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

        // TODO 1: Create a second mock session for "son"
        WebSocketSession sonSession = mock(WebSocketSession.class);
      //  WebSocketSession dadSession = mock(WebSocketSession.class);// already have this in setup

        // TODO 2: Configure sonSession.getId() to return "sonSession123"
        when(sonSession.getId()).thenReturn("sonSession123");
        // TODO 3: Configure sonSession.isOpen() to return true
        when(sonSession.isOpen()).thenReturn(true);

        // TODO 4: Register dad (reuse the mockSession from setUp - that's dad's session)
        // Hint: Create RegisterMessage for "dad", convert to JSON, call handleTextMessage
        RegisterMessage registerMessageDad = new RegisterMessage("dad");
        String jsonDad = objectMapper.writeValueAsString(registerMessageDad);
        TextMessage textMessageDad = new TextMessage(jsonDad);

        // TODO 5: Register son with sonSession
        // Hint: Same as TODO 4, but for "son"
        RegisterMessage registerMessageSon = new RegisterMessage("son");
        String jsonSon = objectMapper.writeValueAsString(registerMessageSon);
        TextMessage textMessageSon = new TextMessage(jsonSon);

        handler.handleTextMessage(dadSession, textMessageDad);
        handler.handleTextMessage(sonSession, textMessageSon);

        // ACT: Dad sends an offer to son

        // TODO 6: Create a CallMessage
        // Parameters: type="offer", to="son", sdp="fake-sdp-data"
        CallMessage callMessage = new CallMessage("offer", "son", "fake-sdp-data");

        // TODO 7: Convert CallMessage to JSON string
        String jsonCallMessage = objectMapper.writeValueAsString(callMessage);

        // TODO 8: Wrap JSON in TextMessage
        TextMessage textMessageCallMessage = new TextMessage(jsonCallMessage);

        // TODO 9: Call handler.handleTextMessage with dad's session and the message
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
        // TODO: Create RegisterMessage, convert to JSON, register dad
        RegisterMessage registerMessageDad = new RegisterMessage("dad");
        String jsonDad = objectMapper.writeValueAsString(registerMessageDad);
        TextMessage textMessageDad = new TextMessage(jsonDad);
        handler.handleTextMessage(dadSession, textMessageDad);

        // Verify dad is registered BEFORE disconnecting
        assertNotNull(handler.getSession("dad"), "Dad should be registered before disconnecting");

        // ACT: Dad disconnects
        // TODO: Call handler.afterConnectionClosed(dadSession, CloseStatus.NORMAL)
        handler.afterConnectionClosed(dadSession, CloseStatus.NORMAL);
        // Hint: You'll need to import CloseStatus from Spring

        // ASSERT: Verify both maps are cleaned up
        // TODO: Check that handler.getSession("dad") returns null
        //assertEquals(null, handler.getSession("dad"));
        assertNull(handler.getSession("dad")); // -> this is the better option then this above
        // TODO: Check that handler.getUserId("session123") returns null
        //assertEquals(null, handler.getUserId("session123"));
        assertNull(handler.getUserId("session123"));
    }
}