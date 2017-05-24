package distribution.client;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class RotationPolicyEndpoint {
	private Logger logger = Logger.getLogger(this.getClass().getName());
    private static CountDownLatch latch;
    int lastRegisteredServerId;
	private static String registeredQuery = null;
	
    @OnOpen
    public void onOpen(Session session) {
        logger.info("Connected ... " + session.getId());
        try {
            session.getBasicRemote().sendText(registeredQuery);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }      
        System.out.println("Session id from client:" + session.getId());
		
    }
 
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
    		logger.info("Received ...." + message);
    		session.close();
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
        latch.countDown();
    }
    
    public static void setQuery(String query) {
    	registeredQuery = query;
    }
    
}
