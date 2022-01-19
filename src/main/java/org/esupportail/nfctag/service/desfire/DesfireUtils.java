/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.service.desfire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesfireUtils {

	protected final static Logger logger = LoggerFactory.getLogger(DesfireUtils.class);

	/* TODO : Not USED
	 * 
	 *
	public static String decryptHex(String secretKey, String encrypted, String iv) throws Exception {
		Key KEY = new SecretKeySpec(Hex.decodeHex(secretKey.toCharArray()), "AES");
		// -aes-128-ecb
		Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
		c.init(Cipher.DECRYPT_MODE, KEY, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
		byte[] decodedValue = Hex.decodeHex(encrypted.toCharArray());
		byte[] decValue = c.doFinal(decodedValue);
		String decryptedValue = Hex.encodeHexString(decValue);
		return decryptedValue;
	}
	*/
	
	/* TODO : Not USED
	 * 
	 *
	public static String encryptHex(String secretKey, String decrypted, String iv) throws Exception  {
		Key KEY = new SecretKeySpec(Hex.decodeHex(secretKey.toCharArray()), "AES");
		Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
		c.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
		byte[] encodedValue = Hex.decodeHex(decrypted.toCharArray());
		byte[] encValue = c.doFinal(encodedValue);
		String encryptedValue = Hex.encodeHexString(encValue);
		return encryptedValue;
	}
	*/
	
	public static byte[] hexStringToByteArray(String s) {
		if(s == null) return null;
		s = s.replace(" ", "");
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	public static byte hexStringToByte(String s) {
		return (byte) Integer.parseInt(s, 16);
	}


	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	public static String byteArrayToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length*2];
		int v;

		for(int j=0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j*2] = hexArray[v>>>4];
			hexChars[j*2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	public static String swapPairs(byte[] byteArray) {
		String s = new StringBuilder(byteArrayToHexString(byteArray)).reverse().toString();
		String even = "";
		String odd = "";
		int length = s.length();

		for (int i = 0; i <= length-2; i+=2) {
			even += s.charAt(i+1) + "" + s.charAt(i);
		}

		if (length % 2 != 0) {
			odd = even + s.charAt(length-1);
			return odd;
		} else {
			return even;
		}
	}

	public static byte[] swapPairsByte(byte[] byteArray) {
		String swapString = swapPairs(byteArray);
		return hexStringToByteArray(swapString);
	}

}
