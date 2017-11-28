package org.esupportail.nfctag.beans;

import java.io.Serializable;

public class DesfireFlowStep implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public enum Action {
	    READ, 
	    END, 
	    GET_APPS, 
	    CHECK_APP,
	    DELETE_APP, 
	    CREATE_APP, 
	    CHANGE_APP_MASTER_KEY, 
	    CHANGE_KEYS, 
	    CREATE_FILE, 
	    WRITE_FILE, 
	    SELECT_ROOT, 
	    FORMAT, 
	    CHANGE_FILE_KEY_SET, 
	    CHANGE_PICC_KEY,
	    UNBRICK, 
	    SWITCH_TO_AES
	}
	
	public int authStep = 1;
	
	public int writeStep = 0;
	 
	public Action action;
	
	public int currentFile = 0;
	
	// key 0 is app master key and is modified here in specific step
	public int currentKey = 1;
	
	public int currentApp = 0;
	
}
