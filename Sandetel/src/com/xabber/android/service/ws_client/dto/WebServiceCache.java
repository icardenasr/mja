package com.xabber.android.service.ws_client.dto;

import java.util.Date;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class WebServiceCache {

	private Date fecha;
	private String jsonObject;

	public WebServiceCache() {

	}

	/**
	 * Copy Constructor
	 * 
	 * @param webServiceCache
	 *            a <code>WebServiceCache</code> object
	 */
	public WebServiceCache(WebServiceCache webServiceCache) {
		if (webServiceCache != null) {
			this.fecha = webServiceCache.fecha;
			this.jsonObject = webServiceCache.jsonObject;
		}
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(String jsonObject) {
		this.jsonObject = jsonObject;
	}

}
