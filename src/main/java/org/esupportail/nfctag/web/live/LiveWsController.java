package org.esupportail.nfctag.web.live;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Resource;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.esupportail.nfctag.domain.Application;
import org.esupportail.nfctag.domain.Device;
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

@RequestMapping("/live-ws")
@Controller
@Transactional
public class LiveWsController {
	
	@Resource
	ApplisExtService applisExtService;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@RequestMapping(value="/headerImage",params = {"numeroId"})
	@ResponseBody
	public void getHeaderImage(@RequestParam(required=false) String numeroId, HttpServletResponse response, Model uiModel) {
		URL imageURL = null;
		try{
			Device device = Device.findDevicesByNumeroIdEquals(numeroId).getSingleResult();
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


