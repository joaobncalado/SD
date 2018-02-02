package org.komparator.security.handler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;
import org.w3c.dom.DOMException;

public class FreshnessHandler implements SOAPHandler<SOAPMessageContext> {
	
	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		
		String token =null;
		
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		try {
			if (outbound){
				
				SecureRandom random=null;
				try {
					random = SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e1) {
					System.out.println("NoSuchAlgorithmException: Ignoring exception");
					handleFault(smc);
				}

				final byte array[] = new byte[32];
				if(random!=null)random.nextBytes(array);
				
				token = printHexBinary(array);
				
				smc.getMessage().getSOAPHeader().setAttribute("token", ""+token);
				smc.getMessage().getSOAPHeader().setAttribute("timestamp", dateFormatter.format(new Date()));
			}else{//inbound
				//verifica data
				@SuppressWarnings("deprecation")
				Date creationDate= new Date(smc.getMessage().getSOAPHeader().getAttribute("timestamp"));
				if(creationDate.after(new Date(System.currentTimeMillis() + 3000L))||creationDate.before(new Date(System.currentTimeMillis() - 3000L)))
					throw new SOAPException("Invalid message age");
				
				token=smc.getMessage().getSOAPHeader().getAttribute("token");
				if(token==null||!CryptoUtil.addToken(token)){
					throw new SOAPException("Repeated message");
				}
			}
		} catch (DOMException e) {
			System.out.print("Ignoring DOMException in handler: ");
			handleFault(smc);
		} catch (SOAPException e) {
			System.out.print("Ignoring SOAPException in handler: ");
			handleFault(smc);
		} 
		return true;
	}

	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		// print current timestamp
		System.out.println("Caught exception on FreshnessHandler Log:");
		System.out.print("[");
		try {
			System.out.print(smc.getMessage().getSOAPHeader().getAttribute("timestamp"));
		} catch (SOAPException e) {
			System.out.print("Ignoring SOAPException in handler: ");
			e.printStackTrace();
		}
		System.out.print("] ");
		
		System.out.print("intercepted ");
		if (outbound)
			System.out.print("OUTbound ========>");
		else
			System.out.print(" INbound <========");
		System.out.println(" SOAP message:");

		SOAPMessage message = smc.getMessage();
		try {
			message.writeTo(System.out);
			System.out.println(); // add a newline after message

		} catch (SOAPException se) {
			System.out.print("Ignoring SOAPException in handler: ");
			System.out.println(se);
		} catch (IOException ioe) {
			System.out.print("Ignoring IOException in handler: ");
			System.out.println(ioe);
		}
		return true;
	}

	@Override
	public void close(MessageContext context) {
		
	}

	@Override
	public Set<QName> getHeaders() {
		return null;
	}
}
