package org.komparator.mediator.ws;

import java.util.Date;
import java.util.TimerTask;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

public class LifeProof extends TimerTask {
	private MediatorClient medCli=null;
	private MediatorEndpointManager mepm=null;
	public LifeProof(MediatorEndpointManager mepm) {
		this.mepm=mepm;
	}
	@Override
	public void run() {
		String wsURLPrimario="http://localhost:8071/mediator-ws/endpoint";
		String wsURLSecundario="http://localhost:8072/mediator-ws/endpoint";
		
		if(mepm.getWsId()==1){
			try {
				medCli = new MediatorClient(wsURLSecundario);
				System.out.println("Sending imAlive.");
				medCli.iamAlive();
			} catch (MediatorClientException e) {
				System.out.println("LifeProof primary Run(): " + e.getMessage());
			}
		}
		
		if(mepm.getWsId()!=1){
			System.out.println("Checking if primary is alive.");
			int timeout = mepm.getAliveDelay()+3;
			if(mepm.getLastAlive().before(new Date(System.currentTimeMillis() - timeout*1000L))){
				try {
					System.out.println("Assuming primary role.");
					mepm.getEndpoint().stop();
					mepm.unpublishFromUDDI();
					mepm.setWsURL(wsURLPrimario);
					mepm.setWsId(1);
					mepm.start();
				} catch (Exception e) {
					System.out.println("LifeProof secundary Run(): " + e.getMessage());
				}
			}
		}
	}
}
