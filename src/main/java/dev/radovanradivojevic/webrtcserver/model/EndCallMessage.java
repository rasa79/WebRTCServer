package dev.radovanradivojevic.webrtcserver.model;

/**
 * End call message sent when a user wants to terminate an ongoing call
 *
 * This message notifies the other peer that the call is being ended,
 * allowing them to clean up their WebRTC connection and UI.
 *
 * Example JSON:
 * {"type": "end-call", "to": "son"}
 */
public class EndCallMessage extends SignalingMessage {

    /**
     * Sender's userId
     * Who is ending the call (populated by server)
     */
    private String from;

    /**
     * Recipient's userId
     * Who should be notified that the call is ending
     */
    private String to;

    // Constructors
    public EndCallMessage() {
        super("end-call");
    }

    public EndCallMessage(String to) {
        super("end-call");
        this.to = to;
    }

    public EndCallMessage(String from, String to) {
        super("end-call");
        this.from = from;
        this.to = to;
    }

    // Getters and Setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
