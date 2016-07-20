package org.esupportail.nfctag.logs;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class RemoteConverter extends ClassicConverter {
    
	final static String NO_REMOTE_ADDRESS = "NO_REMOTE_ADDRESS";
	
	@Override
	public String convert(ILoggingEvent event) {
		String remoteAddress = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes())
				.getRequest().getRemoteAddr();
		if (remoteAddress != null) {
			return remoteAddress;
		}
		return NO_REMOTE_ADDRESS;     
	}
}