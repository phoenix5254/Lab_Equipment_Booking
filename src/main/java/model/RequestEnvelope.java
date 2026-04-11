package server;

import java.io.Serializable;
import java.util.UUID;

/*
 * RequestEnvelope - wraps every message sent from Client to Server.
 *
 *All Client/Server communication must use serialized
 * request/response envelopes with correlation IDs (UUID).
 *
 * Every request gets a unique UUID as its correlationId.
 * The server mirrors this UUID back in its ResponseEnvelope so the
 * client can match the response to the request that triggered it.
 *
 * action  : what the client wants (e.g. "Login", "Add Reservation")
 * payload : the object being sent (User, Reservation, etc.) or null
 */
public class RequestEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID   correlationId; // unique ID - mirrors back in ResponseEnvelope
    private String action;        // what to do
    private Object payload;       // data object (User, Reservation, String, etc.)

    // UUID is generated automatically - every request gets a fresh one
    public RequestEnvelope(String action, Object payload) {
        this.correlationId = UUID.randomUUID();
        this.action  = action;
        this.payload = payload;
    }

    public UUID   getCorrelationId() { return correlationId; }
    public String getAction()        { return action; }
    public Object getPayload()       { return payload; }

    @Override
    public String toString() {
        return "Request[id=" + correlationId + ", action=" + action + "]";
    }
}
