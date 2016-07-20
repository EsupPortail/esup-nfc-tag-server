package org.esupportail.nfctag.web.manager;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/manager")
@Controller
public class IndexManagerController {

	@RequestMapping
	public String index(@RequestParam(required=false) String apkVersion) {
		return "redirect:/live";
	}
	
}
