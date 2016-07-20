package org.esupportail.nfctag.web.nfc;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.esupportail.nfctag.domain.Device;
import org.esupportail.nfctag.service.VersionApkService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/nfc-index")
@Controller
@Transactional
public class NfcIndexController {

	@Resource
	private VersionApkService versionApkService;
	
	@RequestMapping
	public String index(@RequestParam(required=false) String numeroId, 
			@RequestParam(required=false) String imei,
			@RequestParam(required=false) String macAddress,
			@RequestParam(required=false) String apkVersion) {
		if(!versionApkService.isUserApkVersionUp2Date(apkVersion)){
			return "redirect:/nfc-index/download?apkVersion=" + versionApkService.getApkVersion();
		} else {
			if(numeroId==null || numeroId.isEmpty() || Device.findDevicesByNumeroIdEquals(numeroId).getResultList().isEmpty()) {
				return "redirect:/nfc/locations?imei=" + imei + "&macAddress=" + macAddress;
			} else {
				return "redirect:/live?numeroId=" + numeroId;
			}
		}
		
	}
	
	@RequestMapping(value = "/download")
	public String download(Model uiModel) throws IOException {
		return "nfc/download";
	}
	
	@RequestMapping(value = "/download-apk")
	public void downloadApk(Model uiModel, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String contentType = "application/vnd.android.package-archive";
        response.setContentType(contentType);
        ClassPathResource apkFile = new ClassPathResource("apk/esupnfctagdroid.apk");
        response.setContentLength((int)apkFile.contentLength());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + apkFile.getFilename() + "\"");
        IOUtils.copy(apkFile.getInputStream(), response.getOutputStream());
	}

}


