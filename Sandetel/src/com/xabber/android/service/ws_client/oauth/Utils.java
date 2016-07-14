package com.xabber.android.service.ws_client.oauth;

import java.util.Iterator;
import java.util.Map;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class Utils {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String cookies(String cookieFija, Map<String, String> cookies) {
		String cookieValue = "";

		boolean stop = false;
		Iterator it = cookies.entrySet().iterator();

		while (it.hasNext() && !stop) {
			Map.Entry<String, String> e = (Map.Entry<String, String>) it.next();
			if (e.getKey().startsWith(cookieFija)) {
				stop = true;
				cookieValue = e.getValue();
			}
		}
		return cookieValue;
	}

}
