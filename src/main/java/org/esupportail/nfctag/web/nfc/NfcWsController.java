package org.esupportail.nfctag.web.nfc;

import java.io.IOException;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.service.TagAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/nfc-ws")
@Controller
@Transactional
public class NfcWsController {

	@Resource
	TagAuthService tagAuthService;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@RequestMapping(value="/validate",params = {"id"})
	@ResponseBody
	public Boolean validateTag(long id, Model uiModel) throws IOException {
		return tagAuthService.validateTag(id);
	}

	@RequestMapping(value="/cancel",params = {"id"})
	@ResponseBody
	public Boolean cancelTag(long id, Model uiModel) throws IOException {
		tagAuthService.cancelTag(id);
		return true;
	}

	
}


