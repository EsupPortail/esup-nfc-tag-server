/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class VersionJarService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    private String versionName = "unknow";
    
    @PostConstruct
    protected void extractVersionJar() throws IOException {
    	try {
			InputStream jarFile = new ClassPathResource("jar/esupnfctagdesktop.jar").getInputStream();
			log.info("esupnfctagdesktop.jar found");
			ZipInputStream zin = new ZipInputStream(jarFile);
			ZipEntry ze = zin.getNextEntry();
			while (ze!=null && !"versionJar.txt".equals(ze.getName())) {
				log.trace("JAR scan : " + ze.getName());
			    zin.closeEntry();
			    ze = zin.getNextEntry();
			}
			if(ze!=null && "versionJar.txt".equals(ze.getName())) {
				byte[] bytes = new byte[(int)ze.getSize()];
				zin.read(bytes);
				versionName = new String(bytes);
			}
			zin.closeEntry();
			zin.close();
			log.info("JAR version is : " + versionName);
    	} catch(FileNotFoundException fnfe) {
    		log.warn("esupnfctagdesktop.jar not found");
    	}
    }
    
	public String getJarVersion() {
		return versionName;
	}
	
	private boolean isUserJarVersionDev(String jarVersion) {
		if(jarVersion.endsWith("-dev")){
			if(jarVersion.substring(0, jarVersion.length()-4).compareTo(getJarVersion())>0){
				return true;
			}
		}
		return false;
	}

	public boolean isUserJarVersionUp2Date(String apkVersion) {
		log.info("check version : client " + apkVersion + ", server " + versionName +".");
		return isUserJarVersionDev(apkVersion) || versionName.equals(apkVersion);
	}
	
}
