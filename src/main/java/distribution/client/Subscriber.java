package distribution.client;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import com.csvreader.CsvWriter;
import com.hp.hpl.jena.util.FileManager;


@ClientEndpoint
public class Subscriber {

    static String[] queryPaths = null;
    static int nOq;
    static int policyId;
    static int regInterval;
    static int numOfStreams;
	private static Logger logger = Logger.getLogger(Subscriber.class.getName());

	private static String registeredQuery = null;
	private static long startTime;
	private static long[] counts;
	CsvWriter csv;
	int instanceId;
	static Integer latch;
	static Object monitor = new Object();
	static Boolean finished = false;
	public Subscriber(CsvWriter cw, int instanceId) {
		this.csv = cw;
		this.instanceId = instanceId;
	}
    @OnOpen
    public void onOpen(Session session) {
        logger.info("onOpen Connected ... " + session.getId());
        System.out.println("Session id from client:" + session.getId());
		
    }
 
    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
    	long endTime = System.currentTimeMillis();	
    	logger.info("onMessage Received ...." + message);
    	String[] elms = message.split("@@@");
    	csv.write(counts[instanceId] +"");
    	csv.write((endTime - Long.parseLong(elms[1]))/1000.0 +"");
    	csv.write((Long.parseLong(elms[1]))+"");
    	csv.endRecord();
    	csv.flush();
//    	latch.
    	synchronized (latch) {
    		latch--;	
        	logger.info("latch = " + latch);
        	if (latch == 0) {
        		synchronized (monitor) {
    				monitor.notify();
    	        	System.exit(0);
    			}
        	} 
    	}
    	finished = true;
    }
 
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }
    
    public static void main(String[] args) throws Exception {
    	
    	   /**work with stream*/
    	
        File f = new File("streams");
        File[] streamFiles = f.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().contains("AarhusTrafficData")) {
					return true;
				}
				return false;
			}
		});
        
        BufferedWriter bw = new BufferedWriter(new FileWriter("streamId.txt"));
        for (File streamFile : streamFiles) {
        	String streamId = streamFile.getName();
        	streamId = streamId.replace(".stream", "");
        	bw.write( streamId + "\n");        
        }
        bw.flush();
        bw.close();
        
       numOfStreams = Integer.parseInt(args[1]); 
       List<String> streamIds = new ArrayList<String>();
       for (int i = 0; i < streamFiles.length; i++) {
    	   File file = streamFiles[i];
    	   String streamId = file.getName().replace(".stream", "");
    	   streamIds.add(streamId);
    	   System.out.println(streamId);
    	   if (i >= numOfStreams - 1) {
    		   break;
    	   }
       }   
       
		if (args.length < 1){
    		System.out.println("Input file not found");
    	}

		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        
		try {
			policyId = Integer.parseInt(reader.readLine());
			queryPaths = reader.readLine().split(" ");
			nOq = Integer.parseInt(reader.readLine());
			regInterval = Integer.parseInt(reader.readLine());
		
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		/**Start test*/
//		
//		int id1 = 0, id2 = 0;
//		Random r = new Random();
//		while (id1 == id2) {
//			id1 = r.nextInt(streamIds.size() - 1);
//    		id2 = r.nextInt(streamIds.size() - 1);
//		}
//		
//		String streamId1 = streamIds.get(id1);
//		String streamId2 = streamIds.get(id2);
//		
//		FileManager filemanager = FileManager.get();
//		registeredQuery = filemanager.readWholeFileAsUTF8(queryPaths[0]);
//		
//		System.out.println(registeredQuery);
//		
//		registeredQuery = registeredQuery.replace("$CHANLE1$", streamId1);
//		registeredQuery = registeredQuery.replace("$CHANLE2$", streamId2);
//		
//		System.out.println(registeredQuery);
		/**End test*/
        
		String param = null;
		List<String> serverInfo = new ArrayList<String>();
        while ((param = reader.readLine()) != null) {
        	serverInfo.add(param);
        }
        reader.close();
        
        /**Init connections 2 severs*/
        
        List<URI> serverURIs = createServersURIs(serverInfo);
        List<ClientManager> clients = new ArrayList<ClientManager>();
        for (int i = 0; i < serverURIs.size(); i++) {
        	clients.add(ClientManager.createClient());
        }
        
        List<CsvWriter> cws = new ArrayList<CsvWriter>();
        List<Session> sessions = new ArrayList<Session>();
        counts = new long[serverInfo.size()];
        String uid = UUID.randomUUID().toString();
        for (int i = 0; i < serverURIs.size(); i++) {
    		CsvWriter cw = new CsvWriter(new FileWriter(uid + "_" + serverInfo.size()+ "_" +
    				i + "_" + serverInfo.get(i) + "_qrlat_"+ policyId), ',');
    				cws.add(cw);
			Session session = clients.get(i).connectToServer(new Subscriber(cw, i), serverURIs.get(i));
			sessions.add(session);
			counts[i] = 0;
         }
        
        /**Init policies*/
        RegistrationPolicy rp = null;
        switch (policyId) {
        case 1:
        	rp = new RotationPolicy(serverURIs);
        	break;  	
        case 2:
        	rp = new LatencyPolicy(serverURIs);
        	break;
        case 3:
        	rp = new MemoryPolicy(serverURIs);
        	break;
        default:
        	return;
        }
        Set<String> streamIdSet = new HashSet<String>();
        Set<String> queriesSet = new HashSet<String>();
        
        List<Set<String>> regStreamList = new ArrayList<Set<String>>();
        for (int i = 0; i < serverURIs.size(); i++) {
        	Set<String> regStreams = new HashSet<String>();
        	regStreamList.add(regStreams);
        }
        /***/ 
        Random queryIdxRandom = new Random();
        latch = nOq * queryPaths.length;
        int idxCount = 0;
        for (int i = 0; i < nOq; i++) {
        	for (int j = 0; j < queryPaths.length; j++) {
        		int queryIdx = 0;
        		idxCount++;
        		if (queryPaths.length > 1) {
        			queryIdx = queryIdxRandom.nextInt(queryPaths.length - 1);
        		}
        		
        		logger.info("number of different queries:" + queriesSet.size());
        		
        		rp.searchtargetingServer();
        		
        		int srvIdx = rp.getTargetedServer();

    			Session session = sessions.get(srvIdx);
    			
    			counts[srvIdx] += 1;
    			startTime = System.currentTimeMillis();


        		finished = false;
        		
        		String queryPath = queryPaths[queryIdx];
        		
        		int id1 = 0, id2 = 0;
        		String streamId1 = "", streamId2 ="";
        		while (true) {
        			id1 = queryIdxRandom.nextInt(streamIds.size());
            		id2 = queryIdxRandom.nextInt(streamIds.size());
            		if (id1 == id2) {
            			continue;
            		}

            		streamId1 = streamIds.get(id1);
            		streamId2 = streamIds.get(id2);
            		Set<String> regStreamIds = regStreamList.get(srvIdx);
            		if (regStreamIds.size() == numOfStreams) {
            			break;
            		}
            		
            		if (regStreamIds.size() == numOfStreams - 1) {
                 		if ((!regStreamIds.contains(streamId1)) || (!regStreamIds.contains(streamId2))) {
                 			regStreamIds.add(streamId1);
                 			regStreamIds.add(streamId2);
                 			break;
                 		}
                		logger.info("In here");
             		}
               		
            		if ((!regStreamIds.contains(streamId1)) && (!regStreamIds.contains(streamId2))) {
            			regStreamIds.add(streamId1);
            			regStreamIds.add(streamId2);
            			break;
            		}
        		}
        		
        		System.out.println(regStreamList);
        		

        		streamIdSet.add(streamId1);
        		streamIdSet.add(streamId2);
        		
        		
        		logger.info("num of stream ids: " + streamIdSet.size());
        		FileManager filemanager = FileManager.get();
        		registeredQuery = filemanager.readWholeFileAsUTF8(queryPath);
        		
        		registeredQuery = registeredQuery.replace("$CHANLE1$", streamId1);
        		registeredQuery = registeredQuery.replace("$CHANLE2$", streamId2);
        		
        		//System.out.println(registeredQuery);
        		queriesSet.add(registeredQuery);
        		
    			logger.info("Start register new query");
    			session.getAsyncRemote().sendText(startTime + "@@@" + registeredQuery);

    			while(!finished) {
    				Thread.sleep(500);
    			}
//        		try {
//        			long maxCount = maxCount();
//        			if (maxCount < 100) {
//        				Thread.sleep(1000);
//        			} else if (maxCount < 200) {
//        				Thread.sleep(3000);
//        			} else if (maxCount < 300) {
//        				Thread.sleep(7000);
//        			}  else {
//        				Thread.sleep(10000);
//        			}
//        			
//        		} catch (Exception e) {
//        			e.printStackTrace();
//        		}
        	}
        }
//        rp.closeSessions();
//        for (int i = 0; i < sessions.size(); i++) {
//        	sessions.get(i).close();
//        	cws.get(i).close();
//        }
//        
//        System.exit(0);
        try {
        	synchronized(monitor) {
        		monitor.wait();
        	}

        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

	private static long maxCount() {
		long max = Long.MIN_VALUE;
		for (long count : counts) {
			if (max < count) {
				max = count;
			}
		}
		return max;
	}
	private static List<URI> createServersURIs(List<String> serverInfo) {
        List<URI> serverURIs = new ArrayList<URI>();
		for (String si : serverInfo) { 
			String[] ipAndPort = si.split(" ");
			try {
				URI serverURI = new URI("ws://" + ipAndPort[0] + ":" + 
								ipAndPort[1]+ "/websockets/subscribe");
				serverURIs.add(serverURI);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return serverURIs;
	}
 
}