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

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesfireDiversification {

	protected final static Logger logger = LoggerFactory.getLogger(DesfireDiversification.class);

	byte[] init_vector = new byte[16];
	byte[] cmac_subkey_0 = new byte[16];
	byte[] cmac_subkey_1 = new byte[16];
	byte[] cmac_subkey_2 = new byte[16];

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

	public  byte[] encrypt( byte[] key,  byte[] data,  byte[] iv) throws Exception {
		try {
			Key KEY = new SecretKeySpec(key, "AES");
			Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
			c.init(Cipher.ENCRYPT_MODE, KEY, new IvParameterSpec(iv));
			return c.doFinal(data);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	private byte[] rol(byte[] b) {
		byte[] r = new byte[b.length];
		byte carry = 0;
		for (int i = b.length - 1; i >= 0; i--) {
			int u = b[i] << 1;
			r[i] = (byte)((u & 0xff) + carry);
			carry = (byte)((u & 0xff00) >> 8);
		}
		return r;
	}

	void init(byte[] base_key) throws Exception {
		byte bMSB;
		byte block_size = 0;
		byte rb_xor_value = 0;
            /*BYTE abSavedInitVktr[16];
            DWORD t, i;*/
		int i = 0;

		/*KEY_ISO_AES*/
		rb_xor_value = (byte) 0x87;
		block_size = 16;

		cmac_subkey_0 = encrypt(base_key, cmac_subkey_0, init_vector);
		cmac_subkey_1 = Arrays.copyOf(cmac_subkey_0, cmac_subkey_0.length);
		// If the MSBit of the generated cipher == 1 -> K1 = (cipher << 1) ^ Rb ...
		// store MSB:
		bMSB = cmac_subkey_1[0];

		// Shift the complete cipher for 1 bit ==> K1:
		for (i = 0; i < (int)(block_size - 1); i++)
		{
			cmac_subkey_1[i] <<= 1;
			// add the carry over bit:
			cmac_subkey_1[i] |= (byte)(((cmac_subkey_1[i + 1] & 0x80) == 0x80 ? 0x01 : 0x00));
		}
		cmac_subkey_1[block_size - 1] <<= 1;
		if ((bMSB & 0x80) == 0x80)
		{
			// XOR with Rb:
			cmac_subkey_1[block_size - 1] ^= rb_xor_value;
		}

		// store MSB:
		bMSB = cmac_subkey_1[0];

		// Shift K1 ==> K2:
		for (i = 0; i < (int)(block_size - 1); i++)
		{
			cmac_subkey_2[i] = (byte)(cmac_subkey_1[i] << 1);
			cmac_subkey_2[i] |= (byte)(((cmac_subkey_1[i + 1] & 0x80) == 0x80 ? 0x01 : 0x00));
		}
		cmac_subkey_2[block_size - 1] = (byte) (cmac_subkey_1[block_size - 1] << 1);

		if ((bMSB & 0x80) == 0x80)
		{
			// XOR with Rb:
			cmac_subkey_2[block_size - 1] ^= rb_xor_value;
		}
	}

	public byte[] diversificationAES128(byte[] base_key, byte[] diversification_input, int diversification_length) throws Exception {
		byte[] diversified_key = new byte[16];
		int i = 0;
		byte[] m = new byte[32];
		boolean padd = false;

		// prepare the padding
		init(base_key);
		logger.debug(String.format("TEST K0=%s", DesfireUtils.byteArrayToHexString(cmac_subkey_0)));
		logger.debug(String.format("TEST K0=%s", DesfireUtils.byteArrayToHexString(cmac_subkey_1)));
		logger.debug(String.format("TEST K0=%s", DesfireUtils.byteArrayToHexString(cmac_subkey_2)));

		// add the div constant at the beginning of M
		m[0] = 0x01;
		for (i = 0; i < diversification_length; i++)
		{
			m[1 + i] = diversification_input[i];
		}
		i++;

		// add the padding
		if (((i % 32)!=0) && (i < 32))
		{
			m[i] = (byte) 0x80;
			i++;
			for (; i < 32; i++)
			{
				m[i] = (byte) 0x00;
			}
			padd = true;
		}

		logger.debug("CMAC Input D=" + DesfireUtils.byteArrayToHexString(m));

		/* XOR the last 16 bytes with CMAC_SubKey */
		for (i = 0; i < 16; i++)
		{
			if (padd)
				m[16 + i] ^= cmac_subkey_2[i];
			else
				m[16 + i] ^= cmac_subkey_1[i];
		}

		logger.debug("XOR the last 16 bytes with CMAC_SubKey2=" + DesfireUtils.byteArrayToHexString(m));
		int lsize = 32;
		byte[] iv= new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		/* Encryption using M */
		byte[] result = encrypt(base_key, m, iv);

		logger.debug("Encryption using M=" + DesfireUtils.byteArrayToHexString(m));

		for (i = 0; i < 16; i++) {
			diversified_key[i] = result[16 + i];
		}

		logger.debug("Diversification key=" + DesfireUtils.byteArrayToHexString(diversified_key));

		return diversified_key;
	}

}
