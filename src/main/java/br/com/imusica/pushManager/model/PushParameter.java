package br.com.imusica.pushManager.model;

import java.io.Serializable;

public class PushParameter implements Serializable
{		
	private static final long serialVersionUID = 3153992796189424791L;
	
	private String key;
	private String value;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public PushParameter() {}
	
	public PushParameter(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "PushParameter [key=" + key + ", value=" + value + "]";
	}
}
