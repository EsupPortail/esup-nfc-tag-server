package org.esupportail.nfctag;

import org.esupportail.nfctag.beans.NfcResultBean;
import org.esupportail.nfctag.pcsc.PcscUsbService;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.esupportail.nfctag.service.api.NfcAuthConfig;
import org.esupportail.nfctag.service.desfire.DESFireEV1Service;
import org.esupportail.nfctag.service.desfire.DesfireAuthSession;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.actions.DesfireActionService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
public class DesfireNfcTest {

	@Autowired
	NfcAuthConfigService nfcAuthConfigService;

	@Test
	public void testNfc() throws Exception {
		ClassPathXmlApplicationContext springContext = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext*.xml");
		String result = "";
		NfcAuthConfig nfcAuthConfig = nfcAuthConfigService.get("testDesfireAuthConfigWriteEsupSgc");
		DesfireService desfireService = new DesfireService();
		desfireService.setDesFireEV1Service(new DESFireEV1Service());
		DesfireAuthSession desfireAuthSession = new DesfireAuthSession();
		desfireAuthSession.setDesfireAuthConfig(nfcAuthConfig);
		desfireService.setDesfireAuthSession(desfireAuthSession);
		DesfireActionService desfireActionService = nfcAuthConfig.getDesfireActionService(desfireService, null, null);
		PcscUsbService pcscUsbService = new PcscUsbService();
		pcscUsbService.connection();
		while (!pcscUsbService.isCardPresent()) {
			Thread.sleep(1000);
			System.out.println("waiting for card...");
		}
		System.out.println("card present");
		NfcResultBean nfcResultBean = desfireActionService.process("test", null, "test", "test", result);
		while (!"END".equals(nfcResultBean.getFullApdu())) {
			System.out.println("< " + nfcResultBean.getFullApdu());
			result = pcscUsbService.sendAPDU(nfcResultBean.getFullApdu());
			System.out.println("> " + result);
			nfcResultBean = desfireActionService.process("test", null, "test", "test", result);
		}
	}
}
