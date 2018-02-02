package org.komparator.mediator.ws;

import java.io.IOException;
import java.util.Date;
import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

/** End point manager */
public class MediatorEndpointManager {

	/** UDDI naming server location */
	private String uddiURL = null;
	/** Web Service name */
	private String wsName = null;
	/** Web Service location to publish */
	private String wsURL = null;
	
	private int aliveDelay = 5;

	/** Web Service endpoint */
	private Endpoint endpoint = null;
	/** UDDI Naming instance for contacting UDDI server */
	private UDDINaming uddiNaming = null;
	/** Port implementation */
	private MediatorPortImpl portImpl = new MediatorPortImpl(this);
	/** output option **/
	private boolean verbose = true;

	private Date lastFoundAlive= new Date();
	
	public LifeProofTime timer = new LifeProofTime(true);
	
	private int wsId;
	
	public int getWsId() {
		return wsId;
	}
	
	public int getAliveDelay(){
		return aliveDelay;
	}

	public void setWsId(int wsId) {
		this.wsId = wsId;
	}
	
	/** Get Web Service UDDI publication name */
	public String getWsName() {
		return wsName;
	}
	
	public void setWsURL(String wsURL) {
		this.wsURL = wsURL;
	}
	
	public String getWsURL() {
		return wsURL;
	}

	/** Obtain Port implementation */
	public MediatorPortImpl getPort() {
        return portImpl;
	}
	
	public Endpoint getEndpoint(){
		return this.endpoint;
	}

	/** Get UDDI Naming instance for contacting UDDI server */
	UDDINaming getUddiNaming() {
		return uddiNaming;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided UDDI location, WS name,  WS URL e WsId*/
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL, int wsId) {
		if (uddiURL == null)
			throw new NullPointerException("UDDI URL cannot be null!");
		if (wsName == null)
			throw new NullPointerException("Ws Name cannot be null!");
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		if (wsId == 0)
			throw new NullPointerException("Mediator id cannot be 0!");
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
		this.wsId = wsId;
	}
	
	/** constructor with provided UDDI location, WS name,  WS URL, WsId and aliveDelay in seconds*/
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL, int wsId, int aliveDelay) {
		if (uddiURL == null)
			throw new NullPointerException("UDDI URL cannot be null!");
		if (wsName == null)
			throw new NullPointerException("Ws Name cannot be null!");
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		if (wsId == 0)
			throw new NullPointerException("Mediator id cannot be 0!");
		if (aliveDelay < 0)
			throw new NullPointerException("Delay must not be negative!");
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
		this.wsId = wsId;
		this.aliveDelay = aliveDelay;
	}
	
	/** constructor with provided UDDI location, WS name, and WS URL */
	public MediatorEndpointManager(String uddiURL, String wsName, String wsURL) {
		if (uddiURL == null)
			throw new NullPointerException("UDDI URL cannot be null!");
		if (wsName == null)
			throw new NullPointerException("Ws Name cannot be null!");
		if (wsURL == null)
			throw new NullPointerException("Mediator id cannot be 0!");
		this.uddiURL = uddiURL;
		this.wsName = wsName;
		this.wsURL = wsURL;
	}

	/** constructor with provided web service URL */
	public MediatorEndpointManager(String wsURL) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		this.wsURL = wsURL;
	}

	/* end point management */

	public void start() throws Exception {
		try {
			// publish end point
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
		if(wsId==1){
			System.out.println("I'm primary.");
			publishToUDDI();
		}else{
			System.out.println("I'm secondary.");
		}
	}

	public void stop() throws Exception {
		timer.cancel();
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		this.portImpl = null;
		unpublishFromUDDI();
		
	}
	

	public void startTimerThread() {
		timer.schedule(new LifeProof(this), 0, aliveDelay*1000);
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	/* UDDI */

	void publishToUDDI() throws Exception {
		try {
			// publish to UDDI
			if (uddiURL != null) {
				if (verbose) {
					System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
				}
				uddiNaming = new UDDINaming(uddiURL);
				uddiNaming.rebind(wsName, wsURL);
			}
		} catch (Exception e) {
			uddiNaming = null;
			if (verbose) {
				System.out.printf("Caught exception when binding to UDDI: %s%n", e);
			}
			throw e;
		}
	}

	void unpublishFromUDDI() {
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(wsName);
				if (verbose) {
					System.out.printf("Unpublished '%s' from UDDI%n", wsName);
				}
				uddiNaming = null;
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when unbinding: %s%n", e);
			}
		}
	}
	
	
	public Date getLastAlive(){
		return this.lastFoundAlive;
	}
	public void setLastAlive(Date imAlive){
		this.lastFoundAlive=imAlive;
	}


}
