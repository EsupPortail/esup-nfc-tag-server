package org.esupportail.nfctag.exceptions;

import org.esupportail.nfctag.beans.ApplicationContextProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class EsupNfcTagException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	static public enum EsupNfcTagErrorMessage{
		error_esupnfctagexception_serviceunavailable,
		error_esupnfctagexception_unknowcard,
		error_esupnfctagexception_typenotsupported,
		error_esupnfctagexception_unknowdevice,
		error_esupnfctagexception_locationnotfound,
		error_esupnfctagexception_tagidchecknotdefine,
		error_esupnfctagexception_tagvalidationerror,
		error_esupnfctagexception_httperror
		
	}
	
	String[] params;
	
	String message;
	
	public EsupNfcTagException(Throwable cause, EsupNfcTagErrorMessage message, String...params ) {
		super(message.toString(), cause);
		this.message = message.toString();
		this.params = params;
	}

	public EsupNfcTagException(EsupNfcTagErrorMessage message, String...params) {
		super(message.toString());
		this.message = message.toString();
		this.params = params;
	}

	public EsupNfcTagException(EsupNfcTagErrorMessage message) {
		super(message.toString());
		this.message = message.toString();
	}
	
	public String getMessage() {
		ReloadableResourceBundleMessageSource messageSource = ApplicationContextProvider.getApplicationContext().getBean("messageSource", ReloadableResourceBundleMessageSource.class);
		return messageSource.getMessage(this.message, this.params, LocaleContextHolder.getLocale());
	}

}
