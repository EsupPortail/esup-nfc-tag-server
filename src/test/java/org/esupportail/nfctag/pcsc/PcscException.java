package org.esupportail.nfctag.pcsc;

public class PcscException extends Exception {

	private static final long serialVersionUID = 1L;

	public PcscException(String message) {
		super(message);
	}

	public PcscException(String message, Throwable cause) {
		super(message, cause);
	}

	
}
