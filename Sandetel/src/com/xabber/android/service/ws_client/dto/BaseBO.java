package com.xabber.android.service.ws_client.dto;

import java.io.Serializable;

/**
 * Objeto Base
 * 
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
@SuppressWarnings("serial")
public abstract class BaseBO implements Serializable {

	// public static List<BaseBO> getJSon(String json) {
	//
	// try {
	// TypeToken<List<BaseBO>> listType = new TypeToken<List<BaseBO>>() {
	// };
	//
	// return new GsonUtil<List<BaseBO>>().getFromJSonString(json,
	// listType);
	// } catch (Exception e) {
	// // TODO: handle exception
	// return null;
	// }
	// }
	//
	// public static BaseBO getJSonSingle(String json) {
	//
	// try {
	// BaseBO BaseBO = null;
	// if (json != null) {
	// BaseBO = new GsonUtil<BaseBO>().getFromJsonString(json,
	// BaseBO.class);
	// }
	// return BaseBO;
	// } catch (Exception e) {
	// // TODO: handle exception
	// return null;
	// }
	// }

}
