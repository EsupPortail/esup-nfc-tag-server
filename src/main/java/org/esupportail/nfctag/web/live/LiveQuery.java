package org.esupportail.nfctag.web.live;

import java.io.Serializable;

import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEquals
public class LiveQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	String numeroId;
	
	Long authDateTimestamp;

	public LiveQuery() {
	}
	
	public LiveQuery(Long authDateTimestamp, String numeroId) {
		this.numeroId = numeroId;
		this.authDateTimestamp = authDateTimestamp;
	}
	
	
	
}
