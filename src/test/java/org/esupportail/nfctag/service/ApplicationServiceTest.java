package org.esupportail.nfctag.service;

import static org.junit.Assert.fail;

import java.util.List;

import javax.annotation.Resource;

import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.dao.ApplicationDao;
import org.esupportail.nfctag.dao.DeviceDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(SpringJUnit4ClassRunner.class)
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
	
}
