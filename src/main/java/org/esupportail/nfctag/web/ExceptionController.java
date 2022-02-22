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

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import org.esupportail.nfctag.web.HiddenHttpMethodFilter.HttpMethodRequestWrapper;

@Service
@Controller
public class ExceptionController implements HandlerExceptionResolver {

	private final Logger log = LoggerFactory.getLogger(getClass());

	
	@Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		
		if(ex instanceof AccessDeniedException) {
			return this.deniedHandler(request, response);
		}
		
        String ip = request.getRemoteAddr();	
		if(ex instanceof MultipartException || ex instanceof IOException) {
			log.warn("MultipartException or IOException with this client " + ip + ". We can assume that the client has canceled his request (because of a double-click / double-submit of the form for example).", ex);
		} else {	
			log.error("Uncaught exception  with this client " + ip, ex);
		}

		if(response.isCommitted()) {
			// Client can't get exception page here. 
			return null;
		} else {
	    	//response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        ModelAndView modelAndview = new ModelAndView("uncaughtException");
	        modelAndview.addObject("exception", ex);
	        avoid405Error(request);
	        return modelAndview;
		}
    }

	@RequestMapping("/denied")
    public ModelAndView deniedHandler(HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();	
	    log.warn("Access Denied for " + ip);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        avoid405Error(request);
		return new ModelAndView("accessDenied");
    }

	
	@RequestMapping("/uncaughtException")
    public ModelAndView uncaughtExceptionView(HttpServletRequest request) {
	    Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
	    ModelAndView modelAndview = new ModelAndView("uncaughtException");
	    modelAndview.addObject("uncaughtException", true);
	    modelAndview.addObject("exception", exception);	    
	    avoid405Error(request);
		return modelAndview;
    }
   
	/**
	 * Try to avoid 405 - JSPs only permit GET POST or HEAD with exceptions on put/delete/patch
	 * @param request
	 */
	private void avoid405Error(HttpServletRequest request) {		
		ServletRequest servletRequest = (ServletRequest) request;
	    while(servletRequest!= null && !(servletRequest instanceof HttpMethodRequestWrapper)  && (servletRequest instanceof HttpServletRequestWrapper)) {
	    	servletRequest = ((HttpServletRequestWrapper)servletRequest).getRequest();
	    }
	    if(servletRequest instanceof HttpMethodRequestWrapper) {
	    	((HttpMethodRequestWrapper) servletRequest).setMethod("GET");
	    }
	}
	
}

