package org.komparator.security;

import java.io.*;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import javax.crypto.*;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CAClientException;
import static pt.ulisboa.tecnico.sdis.cert.CertUtil.*;
import java.util.*;

public class CryptoUtil {
	private static List<String> tokenList =new ArrayList<>();
	private static final String cipherAlgorithm="RSA/ECB/PKCS1padding";
	private static final char[] keyPass="0f7x7hJP".toCharArray();
	private static CAClient caCli=null;
	private static Cipher cipher=null;
	private static PublicKey pubKey=null;
	private static X509Certificate certificate=null;
	
	
	
	private static List<String> getTokenList() {
		return tokenList;
	}
	
	public static boolean addToken(String token) {
		for(String item:getTokenList()){
			if(item.equals(token)){
				return false;
			}
		}
		tokenList.add(token);
		return true;
	}
	
	public static String cifer(String token){
		byte[] plainBytes=token.getBytes();
		byte[] cipherBytes=null;
		try {
			cipher = Cipher.getInstance(cipherAlgorithm);
			
			certificate=(X509Certificate) getX509CertificateFromResource("t13_mediator.cer");
			if(certificate==null)
			{
				caCli=new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
				certificate=(X509Certificate) getX509CertificateFromBytes(caCli.getCertificate("t13_mediator").getBytes());
			}
			pubKey=certificate.getPublicKey();
		
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			
			cipherBytes = cipher.doFinal(plainBytes);
			
			token=printBase64Binary(cipherBytes);
			
		} catch (IllegalBlockSizeException e) {
			System.out.println("CryptoUtil: cifer, IllegalBlockSizeException: " + e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println("CryptoUtil: cifer, BadPaddingException: " + e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println("CryptoUtil: cifer, InvalidKeyException: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("CryptoUtil: cifer, NoSuchAlgorithmException: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
			System.out.println("CryptoUtil: cifer, NoSuchPaddingException: " + e.getMessage());
		} catch (CertificateException e) {
			System.out.println("CryptoUtil: cifer, CertificateException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("CryptoUtil: cifer, IOException: " + e.getMessage());
		} catch (CAClientException e) {
			System.out.println("CryptoUtil: cifer, CAClientException: " + e.getMessage());
		}
		return token;
	}
	
	public static String decifer(String token){
		String keyAlias="t13_mediator", pvtKpath="t13_mediator.jks";
		PrivateKey privKey=null;
		byte[] cipherBytes=parseBase64Binary(token);
		byte[] plainBytes=null;
		
		try {
			
			cipher = Cipher.getInstance(cipherAlgorithm);
			
			privKey=(PrivateKey)getPrivateKeyFromKeyStoreResource(pvtKpath, keyPass, keyAlias, keyPass);
			
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			
			plainBytes = cipher.doFinal(cipherBytes);
			
			token=new String(plainBytes);
			
		} catch (UnrecoverableKeyException e) {
			System.out.println("CryptoUtil: decifer, UnrecoverableKeyException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println("CryptoUtil: decifer, FileNotFoundException: " + e.getMessage());
		} catch (KeyStoreException e) {
			System.out.println("CryptoUtil: decifer, KeyStoreException: " + e.getMessage());
		} catch (InvalidKeyException e) {
			System.out.println("CryptoUtil: decifer, InvalidKeyException: " + e.getMessage());
		} catch (IllegalBlockSizeException e) {
			System.out.println("CryptoUtil: decifer, IllegalBlockSizeException: " + e.getMessage());
		} catch (BadPaddingException e) {
			System.out.println("CryptoUtil: decifer, BadPaddingException: " + e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.out.println("CryptoUtil: decifer, NoSuchAlgorithmException: " + e.getMessage());
		} catch (NoSuchPaddingException e) {
			System.out.println("CryptoUtil: decifer, NoSuchPaddingException: " + e.getMessage());
		}
		
		
		return token;
	}
	public static byte[] makeDigitalSignature(final String signatureMethod, final byte[] bytesToSign) {
		String keyAlias="t13_mediator", pvtKpath="t13_mediator.jks";
		PrivateKey privKey=null;
		try {
			Signature sig = Signature.getInstance(signatureMethod);
			privKey=(PrivateKey)getPrivateKeyFromKeyStoreResource(pvtKpath, keyPass, keyAlias, keyPass);
			sig.initSign(privKey);
			sig.update(bytesToSign);
			byte[] signatureResult = sig.sign();
			return signatureResult;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while making signature: " + e);
				System.err.println("Returning null.");
			}
			return null;
		} catch (UnrecoverableKeyException e) {
			System.out.println("CryptoUtil: makeDigitalSignature, UnrecoverableKeyException: " + e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println("CryptoUtil: makeDigitalSignature, FileNotFoundException: " + e.getMessage());
		} catch (KeyStoreException e) {
			System.out.println("CryptoUtil: makeDigitalSignature, KeyStoreException: " + e.getMessage());
		}
		return null;
	}
	
	public static boolean verifyDigitalSignature(final String signatureMethod,
			byte[] bytesToVerify, byte[] signature) {
		try {
		certificate=(X509Certificate) getX509CertificateFromResource("t13_mediator.cer");
		if(certificate==null)
		{
			caCli=new CAClient("http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca?WSDL");
			certificate=(X509Certificate) getX509CertificateFromBytes(caCli.getCertificate("t13_mediator").getBytes());
		}
		pubKey=certificate.getPublicKey();
		
			Signature sig = Signature.getInstance(signatureMethod);
			sig.initVerify(pubKey);
			sig.update(bytesToVerify);
			return sig.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			if (outputFlag) {
				System.err.println("Caught exception while verifying signature " + e);
				System.err.println("Returning false.");
			}
			return false;
		} catch (CAClientException e) {
			System.out.println("CryptoUtil: verifyDigitalSignature, CAClientException: " + e.getMessage());
		} catch (CertificateException e) {
			System.out.println("CryptoUtil: verifyDigitalSignature, CertificateException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("CryptoUtil: verifyDigitalSignature, IOException: " + e.getMessage());
		}
		return false;
	}

}
