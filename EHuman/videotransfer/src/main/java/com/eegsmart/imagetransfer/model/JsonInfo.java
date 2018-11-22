package com.eegsmart.imagetransfer.model;

public class JsonInfo {
	private String key="";
	private String array="";
	private int result=0;	
	public JsonInfo(String key, int result) {
		super();
		this.key = key;
		this.result = result;
	}
	
	public JsonInfo(String key, String array) {
		super();
		this.key = key;
		this.array = array;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getArray() {
		return array;
	}
	public void setArray(String array) {
		this.array = array;
	}
	/*public int getResult() {
		return result;
	}*/
	public boolean getResult() {
		return result==1?true:false;
	}
	public void setResult(int result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "JsonInfo [key=" + key + ", array=" + array + ", result="
				+ result + "]";
	}

}
