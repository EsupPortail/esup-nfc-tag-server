package org.esupportail.nfctag.service.desfire;

import static org.junit.Assert.fail;

import java.util.List;

import javax.annotation.Resource;

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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
@WebAppConfiguration
public class DesfireServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Resource
    protected RestTemplate restTemplate;
	
	@Autowired
	DesfireService desfireService;
	
	@Autowired
	DesfireTag desfireComueTagEsupSgc;
	
	@Resource
	DesfireAuthSession desfireAuthSession;
	
	@Test
	public void testParseDesfireConfiguration() throws Exception {
		
		for(DesfireApplication desApp : desfireComueTagEsupSgc.getApplications()){
			String hexNbKey = "0" + desApp.getNok().substring(1); 
			int nbKey = Integer.parseInt(hexNbKey, 16 );
			if(nbKey != desApp.getKeys().size()){
				fail("error on desfireTest, nb key not good for " + desApp.getDesfireAppId());
			}
			for(DesfireFile desFile : desApp.getFiles()){
				try {
					List<TagLog> tagLogs = TagLog.findAllTagLogs();
					if(tagLogs.size() > 0 ) {
						desFile.getTagWriteApi().getIdFromCsn(tagLogs.get(tagLogs.size()-1).getCsn());
					}
				} catch (EsupNfcTagException e){
					if(e.getMessage().equals(EsupNfcTagErrorMessage.error_esupnfctagexception_serviceunavailable)){
						fail("error on tagWriteApi for " +  desApp.getDesfireAppId());
					}
				}
			}
			
		}

	}
}
