package org.esupportail.nfctag.domain;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findTagLogsByLocation", "findTagLogsByApplication", "findTagLogsByAuthDateGreaterThan", "findTagLogsByAuthDateGreaterThanAndNumeroIdEquals", "findTagLogsByNumeroIdEquals" } )
public class TagLog {
	
	public enum Status{
		none, valid, cancel
	}
	
	private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

    private String desfireId;
    
    private String csn;
    
    private String eppn;

    private String firstname;

    private String lastname;
    
    private String numeroId;
    
    private String eppnInit;
    
    private String applicationName;

    private String location;
    
	@Enumerated(EnumType.STRING)
	private Status status;

    @DateTimeFormat(style = "MM")
    private Date authDate;
    
    public String getAuthDateString() {
    	return dateFormatter.format(authDate);
    }
    
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
}
