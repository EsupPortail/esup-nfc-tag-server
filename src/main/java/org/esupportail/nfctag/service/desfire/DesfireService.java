package org.esupportail.nfctag.service.desfire;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.service.ApplicationsService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.api.impl.DesfireAuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import nfcjlib.core.util.CMAC;
import nfcjlib.core.util.CRC32;

public class DesfireService {
	
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
	private AesService aesService;
	
	private DesfireAuthSession desfireAuthSession;
	
	private ApplicationsService applicationsService;
	
	private NfcAuthConfigService nfcAuthConfigService;
	
	public void setAesService(AesService aesService) {
		this.aesService = aesService;
	}
	
	public void setDesfireAuthSession(DesfireAuthSession desfireAuthSession) {
		this.desfireAuthSession = desfireAuthSession;
	}

	public void setApplicationsService(ApplicationsService applicationsService) {
		this.applicationsService = applicationsService;
	}

	public void setNfcAuthConfigService(NfcAuthConfigService nfcAuthConfigService) {
		this.nfcAuthConfigService = nfcAuthConfigService;
	}

	public String getSelectAppCommand() {
		String appCommand = "905A000003" + desfireAuthSession.getDesfireAuthConfig().getDesfireAppId() + "00" ;
		return appCommand;
	}
	
	/**
	 * Authentication - phase 1
	 */
	public String getRndbCommand() {
		return "90AA000001" + desfireAuthSession.getDesfireAuthConfig().getDesfireKeyNumber() + "00" ;
	}
	
	/**
	 * Authentication - phase 2
	 * @throws Exception 
	 */
	public String getRndAPrimEncCommand(String rndb) throws Exception {
		String secretKey = desfireAuthSession.getDesfireAuthConfig().getDesfireKey();
		log.trace("RndB returned : " + rndb);
		String rndBdecrypt = aesService.decryptHex(secretKey, rndb, "00000000000000000000000000000000");
		log.trace("RndB décrypté : " + rndBdecrypt);
		desfireAuthSession.setRndBdecrypt(rndBdecrypt);
		String rndBPrim = rndBdecrypt.substring(2, 32) + rndBdecrypt.substring(0, 2);
		log.trace("rndBPrim  : " + rndBPrim);
		String rndA = getRandomHexString(32);
		log.trace("rndA  : " + rndA);
		desfireAuthSession.setRndA(rndA);
		String rndAEnc = aesService.encryptHex(secretKey, rndA, rndb);
		log.trace("rndAEnc  : " + rndAEnc);
		String rndBPrimEnc = aesService.encryptHex(secretKey, rndBPrim, rndAEnc);
		log.trace("rndBPrimEnc  : " + rndBPrimEnc);
		desfireAuthSession.setRndBPrimEnc(rndBPrimEnc);
		String rndAEncRndBPrimEnc = rndAEnc + rndBPrimEnc;
		String cmdAuth = "90AF000020" + rndAEncRndBPrimEnc + "00";
		return cmdAuth;
	}
	
	/**
	 * Authentication - phase 3
	 */
	public String getReadFileCommand() {
		return desfireAuthSession.getDesfireAuthConfig().getReadFileCommand();
	}
	
	protected void checkAuthIsOkAndSetSessionKey(String secretKey, String rndAPrimEnc) throws Exception {
		String rndA = desfireAuthSession.getRndA();
		String rndBPrimEnc = desfireAuthSession.getRndBPrimEnc();
		String rndBdecrypt = desfireAuthSession.getRndBdecrypt();
		log.trace("rndAPrimEnc : " + rndAPrimEnc);
		String rndAPrimdecryptLeft = aesService.decryptHex(secretKey, rndAPrimEnc, rndBPrimEnc);
		log.trace("RndaPrimLeft décrypté : " + rndAPrimdecryptLeft);
		String rndAPrimdecrypt = rndAPrimdecryptLeft.substring(30, 32) + rndAPrimdecryptLeft.substring(0, 30);
		log.trace("RndaPrim décrypté : " + rndAPrimdecrypt);
		if(rndAPrimdecrypt.equals(rndA)) {
			log.info("Desfire auth OK");
		} else {
			throw new BadCredentialsException("Desfire Authentication error");
		}
		// session key := RndA1st half + RndB1st half + RndA2nd half + RndB2nd half 
		String sessionKey = rndA.substring(0, 8) + rndBdecrypt.substring(0, 8) + rndA.substring(24, 32) + rndBdecrypt.substring(24, 32);
		log.trace("sessionKey : " + sessionKey);
		desfireAuthSession.setSessionKey(sessionKey);
	}
	
	/**
	 * Authentication - phase 4
	 */
	public String getDescryptedDesfireId(String desfireId, String rndAPrimEnc) throws Exception {
		String secretKey = desfireAuthSession.getDesfireAuthConfig().getDesfireKey();
		log.trace("getLoginFromDesfireId - desfireId = " + desfireId + " ; rndAPrimEnc = " + rndAPrimEnc);
		checkAuthIsOkAndSetSessionKey(secretKey, rndAPrimEnc);
		log.trace("desfireId : " + desfireId);
		String sessionKey = desfireAuthSession.getSessionKey();
		String iv = "00000000000000000000000000000000";
		//aesService.encrypt(sessionKey, iv, iv);
		byte[] niv = calculateApduCMAC(Hex.decodeHex(desfireAuthSession.getDesfireAuthConfig().getReadFileCommand().toCharArray()), Hex.decodeHex(sessionKey.toCharArray()), Hex.decodeHex(iv.toCharArray()));
		byte[] decryptDesfireId = postprocessEnciphered(Hex.decodeHex(sessionKey.toCharArray()), Hex.decodeHex(desfireId.toCharArray()), niv, 22);
		String realDesfireId = new String(decryptDesfireId, "UTF-8");
		realDesfireId = realDesfireId.substring(1, 16);
	    return realDesfireId;
	}
	
	protected String getRandomHexString(int numchars){
		Random r = new Random();
		StringBuffer sb = new StringBuffer();
		while(sb.length() < numchars){
			sb.append(Integer.toHexString(r.nextInt()));
		}
		return sb.toString().substring(0, numchars);
	}
	
	private byte[] calculateApduCMAC(byte[] apdu, byte[] sessionKey, byte[] iv) {
		byte[] block;

		if (apdu.length == 5) {
			block = new byte[apdu.length - 4];
		} else {
			// trailing 00h exists
			block = new byte[apdu.length - 5];
			System.arraycopy(apdu, 5, block, 1, apdu.length - 6);
		}
		block[0] = apdu[1];
		
		return CMAC.get(CMAC.Type.AES, sessionKey, block, iv);
	}


	private byte[] postprocessEnciphered(byte[] skey, byte[] apdu, byte[] iv, int length) throws Exception {
		
		byte[] plaintext = aesService.decrypt(skey, apdu, iv); 

		iv = Arrays.copyOfRange(apdu, apdu.length - iv.length, apdu.length);
		byte[] crc = calculateApduCRC32R(plaintext, length);
		for (int i = 0; i < crc.length; i++) {
			if (crc[i] != plaintext[i + length]) {
				System.err.println("Received CMAC does not match calculated CMAC.");
				return null;
			}
		}
		return Arrays.copyOfRange(plaintext, 0, length);
	}
	
	

	private byte[] calculateApduCRC32R(byte[] apdu, int length) {
		byte[] data = new byte[length + 1];

		System.arraycopy(apdu, 0, data, 0, length);// response code is at the end

		return CRC32.get(data);
	}

	public void setNumeroId(String numeroId) throws EsupNfcTagException {
		String nfcAuthConfigKey = applicationsService.getApplicationFromNumeroId(numeroId).getNfcConfig();
		DesfireAuthConfig desfireAuthConfig = (DesfireAuthConfig)nfcAuthConfigService.get(nfcAuthConfigKey);
		desfireAuthSession.setDesfireAuthConfig(desfireAuthConfig);;
	}
	
}
