package org.komparator.security.handler;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.komparator.security.CryptoUtil;
import org.w3c.dom.NodeList;


public class CCHandler implements SOAPHandler<SOAPMessageContext> {

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		String originalCC="";
		String encriptedCC="";
		NodeList creditCard=null;
		
		try {
			creditCard=smc.getMessage().getSOAPBody().getElementsByTagName("creditCardNr");
			if(outbound){
				if(creditCard!=null&&creditCard.getLength()>0){
					originalCC=creditCard.item(0).getTextContent();
					encriptedCC=CryptoUtil.cifer(originalCC);
					creditCard.item(0).setTextContent(encriptedCC);
				}
			}else{
				if(creditCard!=null&&creditCard.getLength()>0){
					encriptedCC=creditCard.item(0).getTextContent();
					originalCC=CryptoUtil.decifer(encriptedCC);
					creditCard.item(0).setTextContent(originalCC);
				}
			}
		} catch (SOAPException e) {
			System.out.println("CCHandler, SOAPException: " + e.getMessage());
		}
		
		
		
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
