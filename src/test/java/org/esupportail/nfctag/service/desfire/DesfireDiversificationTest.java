package org.esupportail.nfctag.service.desfire;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



/**
* ESC DEUINFO use AES128 key for diversification. 
* Please refer to [AN10922] NXP document to retrieve the way for generation diversified key from a base key.
* We use here example data from 2.2.1 chapter of this [AN10922] NXP document : "AES-128 key diversification example"
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class DesfireDiversificationTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	String BASE_KEY = "00112233445566778899AABBCCDDEEFF";
	
	String DIVERSIFICATION_INPUT = "04782E21801D803042F54E585020416275";
	
	String DIVERSIFIED_KEY = "A8DD63A3B89D54B37CA802473FDA9175";
	
	@Test
	public void testDiversificationInit() throws Exception {
		DesfireDiversification desfireDiversification = new DesfireDiversification();
		
		byte[] baseKey = DesfireUtils.hexStringToByteArray(BASE_KEY);

		desfireDiversification.init(baseKey);
		
		assertEquals("FDE4FBAE4A09E020EFF722969F83832B", DesfireUtils.byteArrayToHexString(desfireDiversification.cmac_subkey_0));
		assertEquals("FBC9F75C9413C041DFEE452D3F0706D1", DesfireUtils.byteArrayToHexString(desfireDiversification.cmac_subkey_1));
		assertEquals("F793EEB928278083BFDC8A5A7E0E0D25", DesfireUtils.byteArrayToHexString(desfireDiversification.cmac_subkey_2));
	}
	
	@Test
	public void testDiversification_AES128() throws Exception {
		DesfireDiversification desfireDiversification = new DesfireDiversification();
		
		byte[] baseKey = DesfireUtils.hexStringToByteArray(BASE_KEY);
		byte[] diversificationInput = DesfireUtils.hexStringToByteArray(DIVERSIFICATION_INPUT);

		byte[] diversifiedKeyComputed = desfireDiversification.diversificationAES128(baseKey, diversificationInput, diversificationInput.length);
		
		String diversifiedKeyComputedHexString = DesfireUtils.byteArrayToHexString(diversifiedKeyComputed);
		
		log.info("diversifiedKey goal : " + DIVERSIFIED_KEY);
		log.info("diversifiedKey computed : " + diversifiedKeyComputedHexString);
		
		assertEquals(DIVERSIFIED_KEY, diversifiedKeyComputedHexString);
	}
}
