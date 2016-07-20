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
