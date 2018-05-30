package org.esupportail.nfctag.service;

import java.util.Random;

import org.esupportail.nfctag.domain.Device;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {
	
	public String generateNumeroId() {
		while(true) {
			Long numeroRandom = Math.abs(new Random().nextLong());
			if(Device.countFindDevicesByNumeroIdEquals(numeroRandom.toString()) == 0){
				return numeroRandom.toString();
			}
		}
	}

}
