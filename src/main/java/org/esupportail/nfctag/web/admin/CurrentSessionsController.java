package org.esupportail.nfctag.web.admin;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/currentsessions")
@Controller
public class CurrentSessionsController {
	
	@ModelAttribute("active")
	String getCurrentMenu() {
		return "sessions";
	}

	@Autowired
	@Qualifier("sessionRegistry")
	private SessionRegistry sessionRegistry;
	
	@RequestMapping
	public String getCurrentSessions(Model uiModel) throws IOException {

		
		
		List<String> sessions = new Vector<String>();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		
		for(Object p: principals) {
			sessions.add(((UserDetails) p).getUsername());
		}
		
		uiModel.addAttribute("sessions", sessions);
		uiModel.addAttribute("active", "sessions");
		
		return "admin/currentsessions";
	}

}
