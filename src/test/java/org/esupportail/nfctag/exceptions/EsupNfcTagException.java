/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.nfctag.exceptions;

public class EsupNfcTagException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	static public enum EsupNfcTagErrorMessage{
		error_esupnfctagexception_serviceunavailable,
		error_esupnfctagexception_unknowcard,
		error_esupnfctagexception_nottagable,
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
		return this.message;
	}

}
