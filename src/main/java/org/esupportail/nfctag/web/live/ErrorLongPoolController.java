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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.esupportail.nfctag.domain.TagError;
import org.esupportail.nfctag.service.NfcAuthConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

@RequestMapping("/live")
@Controller
@Transactional
public class ErrorLongPoolController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Map<DeferredResult<List<TagError>>, LiveQuery> suspendedTagErrorsRequests = new ConcurrentHashMap<DeferredResult<List<TagError>>, LiveQuery>();
	
	
	@RequestMapping(value = "/tagerror")
	@ResponseBody
	public DeferredResult<List<TagError>> tagError(@RequestParam Long errorDateTimestamp, @RequestParam(required=false) String numeroId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(numeroId==null && !auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_ADMIN")) && !auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_SUPERVISOR"))) {
			return null;
		}
		LiveQuery liveQuery = new LiveQuery(errorDateTimestamp, numeroId);
		final DeferredResult<List<TagError>> tagErrors = new DeferredResult<List<TagError>>(null, Collections.emptyList());
		
		this.suspendedTagErrorsRequests.put(tagErrors, liveQuery);

		tagErrors.onCompletion(new Runnable() {
			public void run() {
				suspendedTagErrorsRequests.remove(tagErrors);
			}
		});
		
		return tagErrors;
	}

	public void handleError(TagError tagError) {
		log.debug("Client received error " + tagError);
		for (Entry<DeferredResult<List<TagError>>, LiveQuery> entry : this.suspendedTagErrorsRequests.entrySet()) {
			if(entry.getValue().getNumeroId().equals(tagError.getNumeroId()) && entry.getValue().getAuthDateTimestamp()<= tagError.getErrorDate().getTime()) {
				log.info("Error match : " + tagError);
				List<TagError> tagErrors = Arrays.asList(tagError); 
				if(!tagErrors.isEmpty()) {
					entry.getKey().setResult(tagErrors);
				}
			}
		}
	}

}