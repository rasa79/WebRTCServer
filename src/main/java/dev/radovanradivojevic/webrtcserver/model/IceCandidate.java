package dev.radovanradivojevic.webrtcserver.model;

/**
 * Represents an ICE (Interactive Connectivity Establishment) candidate
 *
 * ICE candidates contain network connection information that WebRTC peers
 * exchange to establish a connection. Each candidate represents a possible
 * network path between the two devices.
 *
 * This class matches the structure of RTCIceCandidate from the WebRTC JavaScript API.
 */
public class IceCandidate {

    /**
     * The media stream identification tag
     * Identifies which media stream this candidate is associated with
     */
    private String sdpMid;

    /**
     * The index of the media line in the SDP this candidate is associated with
     */
    private Integer sdpMLineIndex;

    /**
     * The candidate attribute as defined in RFC 5245
     * Contains the actual connection information (IP address, port, protocol, etc.)
     * Example: "candidate:1 1 udp 2130706431 192.168.100.11 52341 typ host"
     */
    private String sdp;

    // Constructors
    public IceCandidate() {
    }

    public IceCandidate(String sdpMid, Integer sdpMLineIndex, String sdp) {
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.sdp = sdp;
    }

    // Getters and Setters
    public String getSdpMid() {
        return sdpMid;
    }

    public void setSdpMid(String sdpMid) {
        this.sdpMid = sdpMid;
    }

    public Integer getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public void setSdpMLineIndex(Integer sdpMLineIndex) {
        this.sdpMLineIndex = sdpMLineIndex;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }
}
