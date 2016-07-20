package org.esupportail.nfctag.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.esupportail.nfctag.service.VersionApkService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/")
@Controller
public class IndexController {

	@Resource
	private VersionApkService versionApkService;

	@RequestMapping
	public String index(@RequestParam(required = false) String apkVersion, HttpServletRequest request) {
		if (apkVersion != null) {
			if (versionApkService.isUserApkVersionUp2Date(apkVersion)) {
				return "redirect:nfc-index";
			}
		}
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		if (authentication.getCredentials() != "") {
			return "redirect:live";
		}
		return "index";
	}

}
