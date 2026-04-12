package client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.ResponseEnvelope;

/*
 * ClientListenerThread - dedicated daemon thread that reads ALL inbound
 * messages from the server. Never touches Swing components 
 *
 * Two types of messages:
 *
 * 1. Normal response (action != "PUSH_UPDATE")
 *    The server replied to a sendRequest() call.
 *    -> Calls client.deliverResponse() to wake the waiting sendRequest() thread.
 *
 * 2. Push notification (action == "PUSH_UPDATE") 
 *    Server broadcasting a reservation status change to ALL clients.
 *    No client request triggered this.
 *    -> Calls pushListener.onPushReceived() so GUI can update.
 *    The GUI implementation uses SwingUtilities.invokeLater() to safely
 *    update Swing components from this non-EDT thread.
 *
 * setDaemon(true): dies automatically when the JVM exits - never blocks shutdown.
 */
public class ClientListenerThread extends Thread {

    private static final Logger logger = LogManager.getLogger(ClientListenerThread.class);

    private final ObjectInputStream objIs;   // reads ResponseEnvelopes from server
    private final Client            client;  // used to deliver responses

    public ClientListenerThread(ObjectInputStream objIs, Client client) {
        super("ClientListenerThread");
        this.objIs  = objIs;
        this.client = client;
        setDaemon(true); // dies with the JVM - never prevents shutdown
    }

    @Override
    public void run() {
        logger.info("CLIENT LISTENER THREAD STARTED");

        try {
            while (!isInterrupted()) {

                // BLOCKS here waiting for the next ResponseEnvelope from the server
                // This blocking is intentional - it's on its own thread 
                ResponseEnvelope response = (ResponseEnvelope) objIs.readObject();
                logger.info("RECEIVED: {} | success={}", response.getAction(), response.isSuccess());

                if ("PUSH_UPDATE".equals(response.getAction())) {
                    // ── Push notification 
                    // Server is broadcasting a reservation change to all clients
                    // Forward to the registered push listener (the GUI)
                    Client.PushListener listener = client.getPushListener();
                    if (listener != null) {
                        // Runs on this thread - GUI must use invokeLater() internally
                        listener.onPushReceived(response);
                    }
                } else {
                    // ── Normal response ───────────────────────────────────────
                    // Matches a sendRequest() call - wake the waiting thread
                    client.deliverResponse(response);
                }
            }
        } catch (EOFException e) {
            logger.info("SERVER CLOSED CONNECTION");
        } catch (SocketException e) {
            if (!isInterrupted()) {
                logger.warn("SOCKET CLOSED: {}", e.getMessage());
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!isInterrupted()) {
                e.printStackTrace();
            }
        }

        logger.info("CLIENT LISTENER THREAD STOPPED");
    }
}
