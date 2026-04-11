package server;

import java.io.Serializable;
import java.util.UUID;

/*
 * ResponseEnvelope - wraps every message sent from Server back to Client.
 *
 * All Client/Server communication must use serialized
 * request/response envelopes with correlation IDs (UUID).
 *
 * The correlationId mirrors the UUID from the RequestEnvelope so the
 * client can match this response to the request that triggered it.
 *
 * PUSH RESPONSES  real-time updates
 *   When a reservation is approved, rejected, or cancelled the server
 *   sends a ResponseEnvelope with action="PUSH_UPDATE" to ALL connected
 *   clients simultaneously. These push envelopes get a fresh UUID because
 *   no client request triggered them.
 */
public class ResponseEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID    correlationId; // mirrors the request UUID so client can match it
    private String  action;        // mirrors the request action, or "PUSH_UPDATE"
    private boolean success;       // true = operation worked
    private String  message;       // human-readable result message shown in the GUI
    private Object  payload;       // result data (User, Reservation list, Boolean, etc.)

    // Normal response - mirrors the request UUID
    public ResponseEnvelope(UUID correlationId, String action,
                             boolean success, String message, Object payload) {
        this.correlationId = correlationId;
        this.action  = action;
        this.success = success;
        this.message = message;
        this.payload = payload;
    }

    // Push notification - generates a fresh UUID (no matching request)
    public ResponseEnvelope(String action, String message, Object payload) {
        this.correlationId = UUID.randomUUID();
        this.action  = action;
        this.success = true;
        this.message = message;
        this.payload = payload;
    }

    public UUID    getCorrelationId() { return correlationId; }
    public String  getAction()        { return action; }
    public boolean isSuccess()        { return success; }
    public String  getMessage()       { return message; }
    public Object  getPayload()       { return payload; }

    @Override
    public String toString() {
        return "Response[id=" + correlationId + ", action=" + action
             + ", success=" + success + ", msg=" + message + "]";
    }
}
