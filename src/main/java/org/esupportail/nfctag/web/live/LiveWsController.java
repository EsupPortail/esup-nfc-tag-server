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
package org.esupportail.nfctag.web.live;

import org.apache.commons.io.IOUtils;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.dao.DeviceDao;
import org.esupportail.nfctag.service.ApplisExtService;
import org.esupportail.nfctag.service.api.AppliExtApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@RequestMapping("/live-ws")
@Controller
@Transactional
public class LiveWsController {
	
	@Resource
	ApplisExtService applisExtService;

	@Resource
	private DeviceDao deviceDao;

	private final Logger log = LoggerFactory.getLogger(getClass());

	@RequestMapping(value="/headerImage",params = {"numeroId"})
	@ResponseBody
	public void getHeaderImage(@RequestParam(required=false) String numeroId, HttpServletResponse response, Model uiModel) {
		URL imageURL = null;
		try{
			Device device = deviceDao.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
			Application app = device.getApplication();
			AppliExtApi appliExtApi = applisExtService.get(app.getAppliExt());
	        imageURL = new URL(appliExtApi.getHeader());
	        String imgFile = imageURL.getFile();
	        String[] imgExt = imgFile.split("\\.");
			HttpHeaders headers = new HttpHeaders();
	        headers.add("Content-Type", "image/"+imgExt[imgExt.length-1]);
	        InputStream is = imageURL.openStream();
	        IOUtils.copy(is, response.getOutputStream());
		}catch(NoResultException e) {
			log.warn("Device not found", e);
		}catch(MalformedURLException e) {
			log.warn("headerImage not found", e);
		} catch (IOException e) {
			log.warn("headerImage download fail", e);
		}
	}
	
}


