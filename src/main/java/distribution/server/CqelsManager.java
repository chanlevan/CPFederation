package distribution.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.insight_centre.citybench.main.CityBench2;

public class CqelsManager {
    final static Logger logger = Logger.getLogger(CqelsManager.class);
	private CityBench2 cb;
	private static CqelsManager engine;
	private static String inputFile = null;
	private static String[] args;
	private static int count = 0;
	private CqelsManager() {
		if (inputFile == null) {
			System.out.println("set input parameters first!"); 
			return;
		}
	}
	
	public static void setInpFile(String inpFile, List<String> params) {
		inputFile = inpFile;
		args = new String[params.size()];
		for (int i = 0; i < params.size(); i++) {
			args[i] = params.get(i);
		}
	}

	CityBench2 getCB() {
		return cb;
	}
	
	public static CqelsManager getManager() {
		if (engine == null) {
			engine = new CqelsManager();
			System.out.println("Created engine time: " + ++count);
		};
		return engine;
	}

	public void initCQELS4CityBench(String serverId) {
		try {
			Properties prop = new Properties();
			// logger.info(Main.class.getClassLoader().);
			File in = new File(inputFile);
			FileInputStream fis = new FileInputStream(in);
			prop.load(fis);
			fis.close();
			// Thread.
			HashMap<String, String> parameters = new HashMap<String, String>();
			for (String s : args) {
				parameters.put(s.split("=")[0], s.split("=")[1]);
			}
			cb = new CityBench2(prop, parameters);
			cb.startTest(serverId);
			// BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			// System.out.print("Please press a key to stop the server.");
			// reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			// logger.error(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void streamIntoCQELSP(String data) {
		cb.streamIntoCQELSP(data);
	}
}
