package distribution.client;

import java.net.URI;
import java.util.List;


public abstract class RegistrationPolicy {
	protected List<URI> serverURIs;
	protected String[] queries;
	protected int nOq;
	URI targetedServer;

	public abstract int getTargetedServer();
	public abstract void searchtargetingServer();
	public void closeSessions() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
