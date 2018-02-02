package org.komparator.mediator.ws;

public class MediatorApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2 || args.length == 3) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String	uddiURL	= null;
		String	wsName	= null;
		String	wsURL	= null;
		int		wsId	= 0   ;
		int aliveDelay = 0;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length == 4) {
			uddiURL =	args[0];
			wsName	=	args[1];
			wsURL	=	args[2];
			wsId	=	Integer.parseInt(args[3]);
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL, wsId);
			endpoint.setVerbose(true);
		} else if (args.length >= 5) {
			uddiURL =	args[0];
			wsName	=	args[1];
			wsURL	=	args[2];
			wsId	=	Integer.parseInt(args[3]);
			aliveDelay = Integer.parseInt(args[4]);
			endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL, wsId, aliveDelay);
			endpoint.setVerbose(true);
		}

		try {
			endpoint.start();
			endpoint.startTimerThread();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
