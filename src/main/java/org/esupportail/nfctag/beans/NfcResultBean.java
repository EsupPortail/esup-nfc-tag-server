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
package org.esupportail.nfctag.beans;

import java.io.Serializable;

public class NfcResultBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum CODE {
        ERROR,
        OK,
        END
    }
	
	public static enum Action {
		none, auth, read, write, update, format
	}

    private CODE code;

    private Action action;
    
    private String cmd;
    
    private String param;
    
    private String size;
    
    private String jSessionId;

    private String fullApdu;
    
    private String msg;
    
    public void setSize(String size) {
		this.size = size;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getjSessionId() {
		return jSessionId;
	}

	public void setjSessionId(String jSessionId) {
		this.jSessionId = jSessionId;
	}

	public String getSize() {
		return size;
	}
   
	public void setSize(int size) {
		this.size = String.valueOf(size);
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public NfcResultBean() {
		super();
	}  
    
    public NfcResultBean(CODE code, String msg) {
		super();
		this.code = code;
		this.fullApdu = msg;
	}

	public CODE getCode() {
		return code;
	}

	public void setCode(CODE code) {
        this.code = code;
    }

    public String getFullApdu() {
        return fullApdu;
    }

    public void setFullApdu(String fullApdu) {
        this.fullApdu = fullApdu;
    }

    public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
}
