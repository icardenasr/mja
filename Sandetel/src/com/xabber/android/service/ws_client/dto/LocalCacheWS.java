package com.xabber.android.service.ws_client.dto;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.xabber.android.service.ws_client.constantes.ConstantesWS;
import com.xabber.android.utils.gson.GsonUtil;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
@SuppressWarnings("serial")
public class LocalCacheWS extends BaseBO {

	private String url;
	private String contenido;
	private String fecha;

	public HashMap<String, String> parametrosEspeciales() {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put(ConstantesWS.SQLITE_TABLE, "CacheWS");
		params.put(ConstantesWS.SQLITE_PRIMARY_KEY, "url");

		return params;

	}

	public HashMap<String, String> parametrosEspecialesBD() {
		HashMap<String, String> params = new HashMap<String, String>();

		params.put(ConstantesWS.SQLITE_TABLE, "CacheWS");
		params.put(ConstantesWS.SQLITE_PRIMARY_KEY, "url");

		return params;

	}

	public void setIntField(String fieldName, Object value)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = getClass().getDeclaredField(fieldName);
		field.set(this, value);
	}

	public Object getIntField(String fieldName) throws NoSuchFieldException,
			IllegalAccessException {
		Field field = getClass().getDeclaredField(fieldName);
		return field.get(this);
	}

	public LocalCacheWS() {
	}

	public static List<LocalCacheWS> getJSon(String json) {
		try {
			TypeToken<List<LocalCacheWS>> listType = new TypeToken<List<LocalCacheWS>>() {
			};

			return new GsonUtil<List<LocalCacheWS>>().getFromJSonString(json,
					listType);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return null;
		}
	}

	public List<String> excluirParametrosBD() {
		List<String> list = new ArrayList<String>();

		return list;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

}
