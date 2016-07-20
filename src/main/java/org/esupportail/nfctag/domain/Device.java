package org.esupportail.nfctag.domain;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(finders = { "findDevicesByNumeroIdEquals" , "findDevicesByEppnInitEquals", "findDevicesByLocationAndEppnInitAndImeiEquals", "findDevicesByLocationAndEppnInitAndMacAddressEquals" })
public class Device {

    private String numeroId;
   
    private boolean validateAuthWoConfirmation = false;

    private String eppnInit;
    
    private String imei;
    
    private String macAddress;
    
    private String userAgent;
    
    @NotNull
    private String location;
    
    @ManyToOne
    @NotNull
    private Application application;
    
    public String getApplicationName(){
    	return this.application.getName();
    }
}
