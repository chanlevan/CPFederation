package distribution.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

public class LatencyPolicy extends RegistrationPolicy{

	Double latency = new Double(-1);
	List<Double> latencies = new ArrayList<Double>();
	static int count = 0;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
    private static Object monitor = new Object();// = new CountDownLatch(1);
    
	private List<URI> serverURIs;
	List<Session> sessions;
	public LatencyPolicy(List<URI> serverURIs) {
		this.serverURIs = serverURIs;
        List<ClientManager> clients = new ArrayList<ClientManager>();
       
        for (int i = 0; i < serverURIs.size(); i++) {
        	clients.add(ClientManager.createClient());
        }
        
        sessions = new ArrayList<Session>();
        
        try {
        for (int i = 0; i < serverURIs.size(); i++) {
			Session session = clients.get(i).connectToServer(new LatencyQuerier(this, serverURIs.get(i)), serverURIs.get(i));
			sessions.add(session);
        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public int getTargetedServer() {
		logger.info("getTargetedServer");
		Double lowestLatency = Double.MAX_VALUE;
		int srvIdx = -1;
		for (int i = 0; i < latencies.size(); i++){
			Double latency = latencies.get(i);
			if (latency.isNaN()) {
				srvIdx = i;
				lowestLatency = Double.NaN;
				break;
				
			} else if (latency < lowestLatency) {
				srvIdx = i;
				lowestLatency = latency;
			}
		}
		if (lowestLatency == Double.MAX_VALUE) {
			if (latencies.size() == 1) {
				logger.info("getTargetedServer latencies.size() == 1 ");
				srvIdx = 0;
			} else {
				logger.info("getTargetedServer random");
				Random r = new Random();
				srvIdx = r.nextInt(latencies.size() - 1);
			}
		}
		logger.info("getTargetedServer srvIdx = " + srvIdx);
		return srvIdx;

	}

	boolean finished;
	
	public void updateLatency(double latency) {
		logger.info("updateLatency");
		synchronized(latencies) {
			latencies.add(latency);
			if (latencies.size() >= serverURIs.size()) {
				finished = true;
				return;
			}
		}
	}

	@Override
	public void searchtargetingServer() {
		latencies.clear();
		finished = false;
		try {
		for (Session session : sessions) {
			session.getAsyncRemote().sendText("LATENCY!!!");
		}
		while (!finished){
			Thread.sleep(10);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeSessions() throws Exception {
		for (Session session : sessions) {
			session.close();
		}
	}

}


