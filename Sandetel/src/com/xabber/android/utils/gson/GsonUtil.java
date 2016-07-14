package com.xabber.android.utils.gson;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class GsonUtil<Tipo> {

	private GsonBuilder builder;

	public GsonUtil() {
		builder = new GsonBuilder();

		/*
		 * builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
		 * 
		 * @Override public JsonElement dserialize(Date t, Type type,
		 * JsonSerializationContext jsc) { return new
		 * JsonPrimitive(t.getTime()); } });
		 */

		builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

			@Override
			public Date deserialize(JsonElement json, Type arg1,
					JsonDeserializationContext arg2) throws JsonParseException {
				// TODO Auto-generated method stub
				// System.out.println(json);
				if (json.getAsString() != null
						&& "".compareTo(json.getAsString()) != 0) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.parseLong(json.getAsString()));
					return calendar.getTime();
					// (new Date(Long.parseLong(json.getAsString())),);
					// return new Date(Long.parseLong(json.getAsString())).;
				} else {
					return null;
				}
			}

		});

		builder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

			@Override
			public JsonElement serialize(Date arg0, Type arg1,
					JsonSerializationContext arg2) {

				return new JsonPrimitive(arg0.getTime());
			}

		});

	}

	public GsonUtil(GsonBuilder builder) {
		this.builder = builder;

		if (this.builder == null) {
			builder = new GsonBuilder();
		}

	}

	public Tipo getFromJsonString(String string, Class<Tipo> tipo) {
		Tipo objeto = null;
		objeto = (Tipo) this.builder.create().fromJson(string, tipo);

		return objeto;

	}

	public Tipo getFromJSonString(String string, TypeToken<Tipo> typeToken) {
		Tipo objeto = null;

		try {
			objeto = this.builder.create()
					.fromJson(string, typeToken.getType());
		} catch (Exception e) {
			objeto = null;
		}

		return objeto;
	}

	public List<Tipo> getJSon(String json) {

		try {
			TypeToken<List<Tipo>> listType = new TypeToken<List<Tipo>>() {
			};

			return new GsonUtil<List<Tipo>>().getFromJSonString(json, listType);
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	}

	public GsonBuilder getBuilder() {
		return builder;
	}

	/**
	 * Convert Object to json String
	 * 
	 * @param json
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getJsonObject(Object json) {
		String gsonString = "";

		if (json != null) {
			gsonString = new GsonUtil().getBuilder().create().toJson(json);
		}

		return gsonString;
	}

}
