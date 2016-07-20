package org.esupportail.nfctag.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
public class AppLocation {
	
	Application application;
	
	List<String> locations = new ArrayList<String>();
	
	Boolean available;
	
	public AppLocation() {
	}

	public AppLocation(Application application, Boolean available) {
		this.application = application;
		this.available = available;
	}

	
}
