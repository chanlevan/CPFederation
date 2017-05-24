package distribution.server;
 
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;


@ServerEndpoint(value = "/subscribe")
public class SubscriberServerEndpoint {
 
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    CqelsManager cqelsManager = CqelsManager.getManager();
    @OnOpen
    public void onOpen(Session session) {
        logger.info("Subscriber connected ... " + session.getId());
    }
 
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
    	if (message.toUpperCase().contains("LATENCY!!!")) {
    		logger.info("onMessage ... request the latency");
    		getLatency(message, session);
    	} 
    	else if (message.toUpperCase().contains("MEMORY!!!")) {
    		logger.info("onMessage ... request the number of buffers");
   			getElementSize(session);
    	}
    	else if (message.toUpperCase().contains("NOQ!!!")) {
    		String[] elms = message.split("!!!");
    		cqelsManager.getCB().setNoQ(Integer.parseInt(elms[1]));
    	}
    	else {
    		logger.info("onMessage ... register query");
    		registerQuery(message, session);    		
    	}
    	
    }

	@OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }
 
    private void getNumofBuffers(String query, Session session) throws Exception {
    	Integer numOfBuffers = cqelsManager.getCB().getNumOfBuffers(query);
    	session.getAsyncRemote().sendText(numOfBuffers.toString());
	}
    
    private void getElementSize(Session session) throws Exception {
    	Long buffersSize = cqelsManager.getCB().getElementSize();
    	session.getAsyncRemote().sendText(buffersSize.toString());
	}

	private void getLatency(String message, Session session) throws Exception {
		Double latency = cqelsManager.getCB().getLatency();
		session.getAsyncRemote().sendText(latency.toString());
	}

    
    private void registerQuery(String query, Session session) {
		String[] elms = query.split("@@@");
		if (query.toUpperCase().contains("SELECT")) {
			try {
				cqelsManager.getCB().registerCQELSQueries(elms[1], session);
				
				//int queryId = cqelsManager.getCB().registerQuery(elms[1], session);
				
				//JsonResultMsg msg = new JsonResultMsg("Query is registered successfully", Integer.toString(queryId));
				
				logger.info("Query is registered successfully");
				
				if (session.isOpen()) {
					session.getAsyncRemote().sendText("finished registration@@@" + elms[0]);
					//session.getAsyncRemote().sendText((new Gson()).toJson(msg));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try  {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }    
}