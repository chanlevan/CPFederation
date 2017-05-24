package distribution.server;
 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.glassfish.tyrus.server.Server;
 
public class CqelsplusServer {
 
    public static void main(String[] args) {
        runServer(args);
    }
 
    public static void runServer(String[] args) {
    	//server information
    	String hostIP = args[0];
    	int port = Integer.parseInt(args[1]);
    	String propertiesInp = args[2];
    	String inputFile=args[3];
    	int startId = Integer.parseInt(args[4]);
    	int policiId = Integer.parseInt(args[5]);
        CountDownLatch latch;
    	try {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        List<String> inputArgs = new ArrayList<String>();
        
        String param = null;
		while ((param = reader.readLine()) != null) {
        	inputArgs.add(param);
        }
        reader.close();
        
		CqelsManager.setInpFile(propertiesInp, inputArgs);
		
    	} catch (Exception e) {
    		e.printStackTrace();
    		return;
    	}
    	
    	CqelsManager.getManager().initCQELS4CityBench(args[4] +"_"  + args[5] + "_" + args[0] + "_" + args[1]);
    	
    	Server server = new Server(hostIP, port, "/websockets", SubscriberServerEndpoint.class);
    	latch = new CountDownLatch(1);
    	
        try {
            server.start();
    		latch.await();

    		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please press a key to stop the server.");
            reader.readLine();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    }
}