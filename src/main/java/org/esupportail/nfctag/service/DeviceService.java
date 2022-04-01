package org.esupportail.nfctag.service;

import org.esupportail.nfctag.dao.DeviceDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service
public class DeviceService {

	@Resource
	private DeviceDao deviceDao;
	
	public String generateNumeroId() {
		while(true) {
			String numeroRandom = UUID.randomUUID().toString();
			if(deviceDao.countFindDevicesByNumeroIdEquals(numeroRandom) == 0){
				return numeroRandom.toString();
			}
		}
	}
}
