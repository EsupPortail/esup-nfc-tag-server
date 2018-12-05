package org.esupportail.nfctag.service.desfire;

import static org.junit.Assert.fail;

import java.util.List;

import org.esupportail.nfctag.beans.DesfireApplication;
import org.esupportail.nfctag.beans.DesfireFile;
import org.esupportail.nfctag.beans.DesfireTag;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
@WebAppConfiguration
public class DesfireServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	DesfireTag desfireComueTagEsupSgc;
	
	@Value("${test.desfire.csn:}") 
	String csnFromConfig; 
	
	@Test
	public void testWithFistCsnFind() throws Exception {
		Long nbTagLog = TagLog.countTagLogs();
		if(nbTagLog > 0) {
			List<TagLog> tagLogs = TagLog.findTagLogEntries(nbTagLog.intValue() - 1, 1);
			log.info("test with " + tagLogs.get(0).getCsn());
			testParseDesfireConfiguration(tagLogs.get(0).getCsn());
		}
	}
	
	@Test
	public void testWithConfigCsn() throws Exception {
		log.info("test with " + csnFromConfig);
		testParseDesfireConfiguration(csnFromConfig);
	}
	
	public void testParseDesfireConfiguration(String csn) throws Exception {
		if(desfireComueTagEsupSgc.getApplications() != null){
		for(DesfireApplication desApp : desfireComueTagEsupSgc.getApplications()) {
			for(DesfireFile desFile : desApp.getFiles()){
				try {
					desFile.getTagWriteApi().getIdFromCsn(csn);
				} catch (EsupNfcTagException e) {
					log.warn("sgc ws error : " + e.getMessage());
					if(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable.toString().equals(e.getMessage())) {
						log.error("sgc ws not responding");
						fail("error on tagWriteApi for " +  desApp.getDesfireAppId());
					} else {
						log.warn("sgc ws ok but not card fount with csn = " + csn);
					}
				}
			}
			
		}
		}
	}
}
