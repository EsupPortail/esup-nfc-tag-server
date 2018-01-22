package org.esupportail.nfctag.batch;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.esupportail.nfctag.domain.AppliVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DbToolService {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	final static String currentEsupSgcVersion = "0.1.x";
		
	@Resource
	DataSource dataSource;

	@Transactional
	public void upgrade() {
		AppliVersion appliVersion = null;
		List<AppliVersion> appliVersions = AppliVersion.findAllAppliVersions();
		if(appliVersions.isEmpty()) {
			appliVersion = new AppliVersion();
			appliVersion.setEsupNfcTagVersion("0.0.x");
			appliVersion.persist();
		} else {
			appliVersion = appliVersions.get(0);
		}
		upgradeIfNeeded(appliVersion);
	}

	private void upgradeIfNeeded(AppliVersion appliVersion) {
		String esupSgcVersion = appliVersion.getEsupNfcTagVersion();
		try{
			if("0.0.x".equals(esupSgcVersion)) {
				
				InputStream is = getClass().getResourceAsStream("/update-0.0.x.sql");
				String sqlUpdate = new Scanner(is,"UTF-8").useDelimiter("\\A").next();
		        
				log.warn("La commande SQL suivante va être exécutée : \n" + sqlUpdate);
				Connection connection = dataSource.getConnection();
				CallableStatement statement = connection.prepareCall(sqlUpdate);
				statement.execute();
				connection.close();
				
				log.warn("\n\n#####\n\t" +
	    				"Pensez à mettre à jour les configurations de l'application depuis l'IHM !" +
	    				"\n#####\n");
	    		
	    		esupSgcVersion = "0.1.x";
	    		
			} else {
				log.warn("\n\n#####\n\t" +
	    				"Base de données à jour !" +
	    				"\n#####\n");
			}
			appliVersion.setEsupNfcTagVersion(currentEsupSgcVersion);
			appliVersion.merge();
		} catch(Exception e) {
			throw new RuntimeException("Erreur durant le mise à jour de la base de données", e);
		}
	}

}
