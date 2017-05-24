package distribution.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.hp.hpl.jena.util.FileManager;

public class MemoryPolicy extends RegistrationPolicy{
	List<Integer> 	bufferNums = new ArrayList<Integer>();;
	List<Long> buffersSize = new ArrayList<Long>();
	static int count = 0;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
    private static Object monitor;// = new CountDownLatch(1);
    
	private List<URI> serverURIs;
	List<Session> sessions;
	
	public MemoryPolicy(List<URI> serverURIs) {
		this.serverURIs = serverURIs;
        List<ClientManager> clients = new ArrayList<ClientManager>();
        
        for (int i = 0; i < serverURIs.size(); i++) {
        	clients.add(ClientManager.createClient());
        }
        
		sessions = new ArrayList<Session>();
        
        try {
        for (int i = 0; i < serverURIs.size(); i++) {
			Session session = clients.get(i).connectToServer(new MemoryQuerier(this, serverURIs.get(i)), serverURIs.get(i));
			sessions.add(session);
        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public int getTargetedServer() {

/**		int minBufferNum = Integer.MAX_VALUE;
		int srvIdx = -1;
		for (int i = 0; i < bufferNums.size(); i++){
			int bufferNum = bufferNums.get(i);
			if (minBufferNum > bufferNum) {
				srvIdx = i;
				minBufferNum = bufferNum;
			}
		}
		logger.info("getTargetedServer srvIdx = " + srvIdx);
		return serverURIs.get(srvIdx);*/
		long minSize = Long.MAX_VALUE;
		int srvIdx = -1;
		boolean same = true;
		for (int i = 0; i < buffersSize.size(); i++){
			long size = buffersSize.get(i);
			if (minSize > size) {
				srvIdx = i;
				minSize = size;
			}
			if (i < buffersSize.size() -1) {
				if (buffersSize.get(i) != buffersSize.get(i+1)) {
					same = false;
				}
			}
		}
		if (same) {
	        Random srvRandom = new Random();
	        if (buffersSize.size() == 1) {
	        	srvIdx = 0;
	        } else {
	        	srvIdx = srvRandom.nextInt(buffersSize.size() - 1);
	        }
		}
		logger.info("getTargetedServer srvIdx = " + srvIdx);
		return srvIdx;
	}

	boolean finished = false;
	public void searchtargetingServer(){
		String query;
		buffersSize.clear();
		finished = false;

		try {
		for (Session session : sessions) {
			session.getAsyncRemote().sendText("MEMORY!!!");
		}
		while (!finished){
			Thread.sleep(100);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/***
	public void updateBufferNum(int bufferNum) {
		logger.info("updateBufferNum");
		bufferNums.add(bufferNum);
		if (bufferNums.size() == serversInfo.size()) {
			synchronized(latch) {
				latch.notifyAll();
			}
		}
	}
	*/

	public void updateBuffersSize(long parseInt) {
		logger.info("updateBuffersSize");
		synchronized(buffersSize) {
			buffersSize.add(parseInt);
			if (buffersSize.size() >= serverURIs.size()) {
				finished = true;
			}
		}
	}
	
	public void closeSessions() throws Exception {
		for (Session session : sessions) {
			session.close();
		}
	}

}


