package distribution.client;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class MemoryQuerier {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	MemoryPolicy bp;
	static CountDownLatch latch= new CountDownLatch(1);
	String query;
	public MemoryQuerier(MemoryPolicy bp, URI serverURI) {
		this.bp = bp;
	}
	 
    @OnOpen
    public void onOpen(Session session) throws Exception {
        logger.info("Connected ... " + session.getId());
        System.out.println("Session id from client:" + session.getId());
    }
 
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
    		logger.info("Received ...." + message);
    		bp.updateBuffersSize(Integer.parseInt(message));
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
        latch.countDown();
    }
}
