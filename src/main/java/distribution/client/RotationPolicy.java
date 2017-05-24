package distribution.client;

import java.net.URI;
import java.util.List;

import javax.websocket.ClientEndpoint;



@ClientEndpoint
public class RotationPolicy extends RegistrationPolicy {

	int lastRegisteredServerId;
	
	public RotationPolicy(List<URI> serverURIs, String[] queries, int nOq) {
		this.serverURIs = serverURIs;
		this.queries = queries;
		this.nOq = nOq;
		
	}
	
	public RotationPolicy(List<URI> serverURIs) {
		this.serverURIs = serverURIs;
		lastRegisteredServerId = -1;
	}

	public int getTargetedServer() {
		lastRegisteredServerId += 1;
		if (lastRegisteredServerId == serverURIs.size()) {
			lastRegisteredServerId = 0;
		}
		return lastRegisteredServerId;
	}
	
	@Override
	public void searchtargetingServer() {
	}
	
	public void closeSessions() {
		// TODO Auto-generated method stub
		
	}

}
