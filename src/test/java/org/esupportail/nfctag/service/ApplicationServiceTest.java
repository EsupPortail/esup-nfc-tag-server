package org.esupportail.nfctag.service;

import java.util.List;

import jakarta.annotation.Resource;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/applicationContext*.xml"})
@WebAppConfiguration
public class ApplicationServiceTest {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	ApplicationsService applicationService;

	@Resource
	ApplicationDao applicationDao;

	@Resource
	private DeviceDao deviceDao;

	@Value("${test.ldap.eppn:dummy@example.org}")
	String testLdapEppn;

	@Test
	public void testApplications() throws Exception {
		List<Application> applications = applicationDao.findAllApplications();
		int nbAppOK = 0;
		for(Application application : applications){
			if(applicationService.checkApplication(application.getId())){
				nbAppOK++;
			} else {
				log.error("Application " + application.getName() + " KO");
			}
		}
		if(nbAppOK < applications.size()){
			fail("error on testApps");
		}
	}

	
	//@Test
	public void testDevices() throws Exception {

		List<Device> devices = deviceDao.findAllDevices();
		int nbDeviceOK = 0;
		for(Device device : devices){
			if(applicationService.checkApplicationFromNumeroId(device.getNumeroId())){
				nbDeviceOK++;
			} else {
				log.error("device " + device.getNumeroId() + " KO");
			}
		}
		if(nbDeviceOK < devices.size()){
			fail("error on testDevices");
		}
	}

	@Test
	public void getApplications4EppnTest() {
		try {
			List<Application> locations = applicationService.getApplications4Eppn(testLdapEppn, false);
			log.info("locations for " + testLdapEppn + " : " + locations);
		} catch (Exception e) {
			fail("error on getApplications4EppnTest : " + e.getMessage());
		}
	}
	
}
