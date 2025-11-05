package dev.radovanradivojevic.webrtcserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base class for all WebSocket signaling messages
 *
 * This abstract class uses Jackson annotations to handle polymorphic JSON deserialization.
 * When JSON arrives with a "type" field, Jackson automatically creates the correct subclass.
 *
 * Example JSON -> Java mapping:
 * {"type": "register", ...} -> RegisterMessage
 * {"type": "offer", ...} -> CallMessage
 * {"type": "answer", ...} -> CallMessage
 * {"type": "ice-candidate", ...} -> IceCandidateMessage
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegisterMessage.class, name = "register"),
        @JsonSubTypes.Type(value = CallMessage.class, name = "offer"),
        @JsonSubTypes.Type(value = CallMessage.class, name = "answer"),
        @JsonSubTypes.Type(value = IceCandidateMessage.class, name = "ice-candidate"),
        @JsonSubTypes.Type(value = EndCallMessage.class, name = "end-call")
})
public abstract class SignalingMessage {

    /**
     * Message type - handled by Jackson's @JsonTypeInfo annotation
     * WRITE_ONLY means Jackson will set this field when deserializing but won't serialize it,
     * avoiding duplicate "type" fields in JSON output
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String type;

    // Constructors
    public SignalingMessage() {
    }

    public SignalingMessage(String type) {
        this.type = type;
    }

    // Getter - allows code to read the type
    public String getType() {
        return type;
    }

    // Setter - allows Jackson and code to set the type
    public void setType(String type) {
        this.type = type;
    }
}