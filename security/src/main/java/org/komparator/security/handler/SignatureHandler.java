package org.komparator.security.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
public class SignatureHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
//		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
//		String signature=null;
//			if (outbound){
//				signature=printBase64Binary(makeDigitalSignature("SHA256withRSA",smc.getMessage().toString().getBytes()));
//				try {
//					if(signature!=null)
//						smc.getMessage().getSOAPHeader().setAttribute("signature", signature);
//				} catch (DOMException e) {
//					System.out.println("SignatureHandler: DOMException: " + e.getMessage());
//				} catch (SOAPException e) {
//					System.out.println("SignatureHandler: SOAPException: " + e.getMessage());
//				}
//			}else{//inbound
//				
//			}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
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
