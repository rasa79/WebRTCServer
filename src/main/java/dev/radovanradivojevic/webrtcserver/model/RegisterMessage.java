package dev.radovanradivojevic.webrtcserver.model;

/**
 * Registration message sent when a device first connects
 *
 * This message tells the server who is connecting so it can route
 * future messages to the correct device.
 *
 * Example JSON:
 * {"type": "register", "userId": "dad"}
 * {"type": "register", "userId": "son"}
 */
public class RegisterMessage extends SignalingMessage {

    /**
     * Unique identifier for the user/device
     * Examples: "dad", "son"
     */
    private String userId;

    // Constructors
    public RegisterMessage() {
        super("register");
    }

    public RegisterMessage(String userId) {
        super("register");
        this.userId = userId;
    }

    // Getter and Setter
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}