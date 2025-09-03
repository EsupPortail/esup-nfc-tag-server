package org.esupportail.nfctag.service.desfire;

import jakarta.annotation.Resource;
import org.esupportail.nfctag.beans.DesfireApplication;
import org.esupportail.nfctag.beans.DesfireFile;
import org.esupportail.nfctag.beans.DesfireTag;
import org.esupportail.nfctag.dao.TagLogDao;
import org.esupportail.nfctag.domain.TagLog;
import org.esupportail.nfctag.exceptions.EsupNfcTagException;
import org.esupportail.nfctag.exceptions.EsupNfcTagException.EsupNfcTagErrorMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
@WebAppConfiguration
public class DesfireServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired(required = false)
	DesfireTag desfireComueTagEsupSgc;

	@Resource
	private TagLogDao tagLogDao;

	@Value("${test.desfire.csn:}") 
	String csnFromConfig; 
	
	@Test
	public void testWithFistCsnFind() throws Exception {
		Long nbTagLog = tagLogDao.countTagLogs();
		if(nbTagLog > 0) {
			List<TagLog> tagLogs = tagLogDao.findTagLogEntries(nbTagLog.intValue() - 1, 1);
			log.info("test with " + tagLogs.get(0).getCsn());
			testParseDesfireConfiguration(tagLogs.get(0).getCsn());
		}
	}
	
	@Test
	public void testWithConfigCsn() throws Exception {
        assumeTrue(!csnFromConfig.isEmpty(), "no test.desfire.csn configured");
        assumeTrue(desfireComueTagEsupSgc != null, "no desfireComueTagEsupSgc configured");
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
						fail("error on tagWriteApi for " +  desApp.getDesfireAppId() + " sgc ws not responding");
					} else {
						log.warn("sgc ws ok but not card fount with csn = " + csn);
					}
				}
			}
			
		}
		}
	}
}
