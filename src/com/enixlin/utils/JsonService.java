
package com.enixlin.utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

 public class JsonService {

	private JsonObject jsonObject;

	public JsonService(String result) {
		super();
		this.jsonObject = (JsonObject) new JsonParser().parse(result);
		// TODO Auto-generated constructor stub
	}

	public JsonArray getJsonArray(String key) {
		return this.jsonObject.get(key).getAsJsonArray();
	}
	
	public JsonObject getJsonObject(String key) {
		return this.jsonObject.get(key).getAsJsonObject();
	}

	public JsonObject getJsonObject() {
		return jsonObject;
	}
	

}
