package org.esupportail.nfctag.logs;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class UserConverter extends ClassicConverter {
	
	final static String NO_USER = "NO_USER";
	
	@Override
    public String convert(ILoggingEvent event) {
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       if (auth != null) {
            return auth.getName();
       }
       return NO_USER;
    }
	
}