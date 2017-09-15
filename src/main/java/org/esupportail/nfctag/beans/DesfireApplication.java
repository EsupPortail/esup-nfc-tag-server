package org.esupportail.nfctag.beans;

import java.io.Serializable;
import java.util.List;

import org.esupportail.nfctag.service.api.TagUpdateApi;
import org.esupportail.nfctag.service.api.TagWriteApi;
import org.esupportail.nfctag.service.api.impl.TagUpdateNone;
import org.esupportail.nfctag.service.api.impl.TagWriteNone;

public class DesfireApplication implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 3-byte AID
	 * -> app identication in NXP world
	 */
	private String desfireAppId;
	
	/**
	 * Application master key == key 0
	 */
	private String key;
	
	/**
	 * application master key settings
	 * for delete, create and modification of key
	 * -> 0B is cool
	 */
	private String amks;
	
	/**
	 * number of keys (concatenated with 0x40 or 0x80 for 3K3DES and AES respectively)
	 * -> 84 for 4 keys with AES
	 */
	private String nok; 
	
	private List<DesfireKey> keys;
	
	private List<DesfireFile> files;
	
	private String updateDate;
	
	TagWriteApi tagWriteApi = new TagWriteNone();
	
	TagUpdateApi tagUpdateApi = new TagUpdateNone();
	
	public TagWriteApi getTagWriteApi() {
		return tagWriteApi;
	}

	public void setTagWriteApi(TagWriteApi tagWriteApi) {
		this.tagWriteApi = tagWriteApi;
	}

	public void setTagUpdateApi(TagUpdateApi tagUpdateApi) {
		this.tagUpdateApi = tagUpdateApi;
	}
	
	public TagUpdateApi getTagUpdateApi() {
		return tagUpdateApi;
	}
	
	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getDesfireAppId() {
		return desfireAppId;
	}

	public void setDesfireAppId(String desfireAppId) {
		this.desfireAppId = desfireAppId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAmks() {
		return amks;
	}

	public void setAmks(String amks) {
		this.amks = amks;
	}

	public String getNok() {
		return nok;
	}

	public void setNok(String nok) {
		this.nok = nok;
	}

	public List<DesfireKey> getKeys() {
		return keys;
	}

	public void setKeys(List<DesfireKey> keys) {
		this.keys = keys;
	}

	public List<DesfireFile> getFiles() {
		return files;
	}

	public void setFiles(List<DesfireFile> files) {
		this.files = files;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((amks == null) ? 0 : amks.hashCode());
		result = prime * result + ((desfireAppId == null) ? 0 : desfireAppId.hashCode());
		result = prime * result + ((files == null) ? 0 : files.hashCode());
		result = prime * result + ((keys == null) ? 0 : keys.hashCode());
		result = prime * result + ((nok == null) ? 0 : nok.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DesfireApplication other = (DesfireApplication) obj;
		if (amks == null) {
			if (other.amks != null)
				return false;
		} else if (!amks.equals(other.amks))
			return false;
		if (desfireAppId == null) {
			if (other.desfireAppId != null)
				return false;
		} else if (!desfireAppId.equals(other.desfireAppId))
			return false;
		if (files == null) {
			if (other.files != null)
				return false;
		} else if (!files.equals(other.files))
			return false;
		if (keys == null) {
			if (other.keys != null)
				return false;
		} else if (!keys.equals(other.keys))
			return false;
		if (nok == null) {
			if (other.nok != null)
				return false;
		} else if (!nok.equals(other.nok))
			return false;
		return true;
	}
	
}
