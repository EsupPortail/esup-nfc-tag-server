package org.esupportail.nfctag.service;

import org.esupportail.nfctag.dao.DeviceDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Random;

@Service
public class DeviceService {

	@Resource
	private DeviceDao deviceDao;
	
	public String generateNumeroId() {
		while(true) {
			Long numeroRandom = Math.abs(new Random().nextLong());
			if(deviceDao.countFindDevicesByNumeroIdEquals(numeroRandom.toString()) == 0){
				return numeroRandom.toString();
			}
		}
	}

}
