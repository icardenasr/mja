package com.xabber.android.service.ws_client.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class WebServiceCacheController {

	private static WebServiceCacheController instance = null;
	private Calendar fechaFreeMemory;

	// Minutes
	public final static int TIME_WS_CACHE = 60;

	public final static int TIME_WS_CACHE_MEMORY = 1;

	public HashMap<String, WebServiceCache> listaWS;

	private WebServiceCacheController() {
		this.listaWS = new HashMap<String, WebServiceCache>();
		fechaFreeMemory = Calendar.getInstance();
	}

	public static WebServiceCacheController getInstance() {
		if (instance == null) {
			instance = new WebServiceCacheController();
		}
		return instance;
	}

	public String getObject(String key) {

		String salida = null;

		synchronized (instance) {

			if (isFreeMemory()) {
				freeMemory();
			}

			WebServiceCache WSCache = this.listaWS.get(key);

			if (WSCache != null) {

				Calendar now = Calendar.getInstance();

				Date fechaCad = WSCache.getFecha();

				// No está caducada
				if (now.getTime().before(fechaCad)) {
					now.add(Calendar.MINUTE,
							WebServiceCacheController.TIME_WS_CACHE);
					WSCache.setFecha(now.getTime());
					salida = WSCache.getJsonObject();
				}

			}

		}
		return salida;
	}

	public String getObjectLocal(String key) {

		String salida = null;

		synchronized (instance) {

			if (isFreeMemory()) {
				freeMemory();
			}

			WebServiceCache WSCache = this.listaWS.get(key);

			if (WSCache != null) {

				Calendar now = Calendar.getInstance();

				Date fechaCad = WSCache.getFecha();

				// No está caducada
				if (now.getTime().before(fechaCad)) {
					now.add(Calendar.MINUTE,
							WebServiceCacheController.TIME_WS_CACHE);
					WSCache.setFecha(now.getTime());
					salida = WSCache.getJsonObject();
				}

			}

		}
		return salida;
	}

	public void putObject(String key, String jsonObject) {

		synchronized (instance) {

			Calendar now = Calendar.getInstance();
			now.add(Calendar.MINUTE, WebServiceCacheController.TIME_WS_CACHE);
			WebServiceCache wsCache = new WebServiceCache();
			wsCache.setFecha(now.getTime());
			wsCache.setJsonObject(jsonObject);

			this.listaWS.put(key, wsCache);

		}
	}

	private void freeMemory() {

		for (Entry<String, WebServiceCache> entrada : this.listaWS.entrySet()) {

			Calendar now = Calendar.getInstance();

			Date fechaCad = entrada.getValue().getFecha();

			// No está caducada
			if (now.getTime().after(fechaCad)) {
				this.listaWS.remove(entrada.getKey());
			}

		}

		fechaFreeMemory = Calendar.getInstance();

	}

	private Boolean isFreeMemory() {
		Calendar now = Calendar.getInstance();
		// fechaFreeMemory.add(Calendar.MINUTE,
		// WebServiceCacheController.TIME_WS_CACHE_MEMORY);now.getTime();fechaFreeMemory.getTime();
		Calendar calAux = Calendar.getInstance();
		calAux.setTime(fechaFreeMemory.getTime());
		calAux.add(Calendar.MINUTE,
				WebServiceCacheController.TIME_WS_CACHE_MEMORY);
		now.getTime();
		fechaFreeMemory.getTime();
		calAux.getTime();
		if (now.before(calAux)) {
			return false;
		}
		return true;
	}
}
