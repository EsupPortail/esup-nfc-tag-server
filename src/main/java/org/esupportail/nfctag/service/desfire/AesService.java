package org.esupportail.nfctag.service.desfire;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

public class AesService {
	
	public String decryptHex(String secretKey, String encrypted, String iv) throws Exception {
		Key KEY = new SecretKeySpec(Hex.decodeHex(secretKey.toCharArray()), "AES");
		// -aes-128-ecb
		Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
		c.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
		byte[] decodedValue = Hex.decodeHex(encrypted.toCharArray());
		byte[] decValue = c.doFinal(decodedValue);
		String decryptedValue = Hex.encodeHexString(decValue);
		return decryptedValue;
	}

	public String encryptHex(String secretKey, String decrypted, String iv) throws Exception  {
		Key KEY = new SecretKeySpec(Hex.decodeHex(secretKey.toCharArray()), "AES");
		Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
		c.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
		byte[] encodedValue = Hex.decodeHex(decrypted.toCharArray());
		byte[] encValue = c.doFinal(encodedValue);
		String encryptedValue = Hex.encodeHexString(encValue);
		return encryptedValue;
	}
	
	public  byte[] decrypt( byte[] key,  byte[] encrypted,  byte[] iv) throws Exception {
		try {
			Key KEY = new SecretKeySpec(key, "AES");
			Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
			c.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(iv));
			return c.doFinal(encrypted);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
