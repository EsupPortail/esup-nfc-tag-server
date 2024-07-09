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
package org.esupportail.nfctag.web.admin;

import org.esupportail.nfctag.domain.NfcHttpSession;
import org.esupportail.nfctag.security.NfcHttpSessionsListenerService;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@RequestMapping("/admin/currentsessions")
@Controller
public class CurrentSessionsController {
	
	@Resource
	private SessionRegistry sessionRegistry;
	
	@Resource
	private LiveLongPoolController liveLongPoolController;

	@Resource
	NfcHttpSessionsListenerService nfcHttpSessionsListenerService;
		
	@ModelAttribute("active")
	String getCurrentMenu() {
		return "sessions";
	}
	
	@RequestMapping
	public String getCurrentSessions(Model uiModel) throws IOException {

		Map<String, NfcHttpSession> allSessions = nfcHttpSessionsListenerService.getSessions();
		List<String> sessions = new Vector<String>();
		List<Object> principals = sessionRegistry.getAllPrincipals();
		
		for(Object p: principals) {
			String eppn = ((UserDetails) p).getUsername();
			sessions.add(eppn);
			for(SessionInformation sessionInformation: sessionRegistry.getAllSessions(p, false)) {
				if(allSessions.containsKey(sessionInformation.getSessionId())) {
					allSessions.get(sessionInformation.getSessionId()).setUserEppn(eppn);
				}
			}
		}
		
		uiModel.addAttribute("sessions", sessions);
		uiModel.addAttribute("devices", liveLongPoolController.getDevices());
		uiModel.addAttribute("active", "sessions");
		uiModel.addAttribute("allSessions", allSessions.values());
		
		return "admin/currentsessions";
	}
	
	

}
