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
public class VersionApkService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

    private String versionName = "unknow";
    
    @PostConstruct
    protected void extractVersionApk() throws IOException {
    	try {
			InputStream apkFile = new ClassPathResource("apk/esupnfctagdroid.apk").getInputStream();
			log.info("esupnfctagdroid.apk found");
			ZipInputStream zin = new ZipInputStream(apkFile);
			ZipEntry ze = zin.getNextEntry();
			while (ze!=null && !"assets/versionApk.txt".equals(ze.getName())) {
				log.trace("APK scan : " + ze.getName());
			    zin.closeEntry();
			    ze = zin.getNextEntry();
			}
			if(ze!=null && "assets/versionApk.txt".equals(ze.getName())) {
				byte[] bytes = new byte[(int)ze.getSize()];
				zin.read(bytes);
				versionName = new String(bytes);
			}
			zin.closeEntry();
			zin.close();
			log.info("APK version is : " + versionName);
    	} catch(FileNotFoundException fnfe) {
    		log.warn("esupnfctagdroid.apk not found");
    	}
    }
    
	public String getApkVersion() {
		return versionName;
	}
	
	private boolean isUserApkVersionDev(String apkVersion) {
		if(apkVersion.endsWith("-dev")){
			if(apkVersion.substring(0, apkVersion.length()-4).compareTo(getApkVersion())>0){
				return true;
			}
		}
		return false;
	}

	public boolean isUserApkVersionUp2Date(String apkVersion) {
		return isUserApkVersionDev(apkVersion) || versionName.equals(apkVersion);
	}
	
}
