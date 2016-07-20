package org.esupportail.nfctag.domain;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
public class TagError {
	
    private Exception exception;

    @DateTimeFormat(style = "MM")
    private Date errorDate;
    
    private String numeroId;

	public TagError() {
	}
    
	public TagError(Exception exception) {
		this.exception = exception;
		this.errorDate = new Date();
	}
	
}
