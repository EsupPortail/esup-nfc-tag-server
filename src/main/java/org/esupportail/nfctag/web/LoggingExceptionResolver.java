package org.esupportail.nfctag.web;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

public class LoggingExceptionResolver extends SimpleMappingExceptionResolver {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected void logException(Exception ex, HttpServletRequest request) {
		log.error(buildLogMessage(ex, request), ex);
	}
}