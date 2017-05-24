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
public class LatencyQuerier {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	LatencyPolicy lp;
	static CountDownLatch latch= new CountDownLatch(1);
	
	public LatencyQuerier(LatencyPolicy lp, URI serverURI) {
		this.lp = lp;
	}
	 
    @OnOpen
    public void onOpen(Session session) throws Exception {
        logger.info("Connected ... " + session.getId());
        System.out.println("Session id from client:" + session.getId());
    }
 
    @OnMessage
    public void onMessage(String message, Session session)  throws Exception {
    		logger.info("Received ...." + message);
    		lp.updateLatency(Double.parseDouble(message));
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
        latch.countDown();
    }
}
