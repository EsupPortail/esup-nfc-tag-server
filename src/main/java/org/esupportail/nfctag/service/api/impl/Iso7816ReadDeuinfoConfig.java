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
package org.esupportail.nfctag.service.api.impl;
import org.esupportail.nfctag.service.TagAuthService;
import org.esupportail.nfctag.service.desfire.DesfireService;
import org.esupportail.nfctag.service.desfire.actions.DesfireActionService;
import org.esupportail.nfctag.service.desfire.actions.Iso7816DeuinfoActionService;
import org.esupportail.nfctag.web.live.LiveLongPoolController;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class Iso7816ReadDeuinfoConfig extends DesfireReadConfig {

	@Override
	public DesfireActionService getDesfireActionService(DesfireService desfireService, TagAuthService tagAuthService, LiveLongPoolController liveController) {
		return new Iso7816DeuinfoActionService(this, desfireService, tagAuthService, liveController);
	}
    
}
