package com.xabber.android.utils.preferences;

import java.lang.reflect.Type;

import net.java.otr4j.crypto.Util;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.xabber.android.utils.gson.GsonUtil;

/**
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */
public class UtilPreferences {

	public static void setPreferences(Context ctx, String key, Object objeto) {

		// SharedPreferences prefs = PreferenceManager
		// .getDefaultSharedPreferences(activity);
		//
		SharedPreferences prefs = new ObscuredSharedPreferences(ctx,
				ctx.getSharedPreferences("preferencias", Context.MODE_PRIVATE));

		Editor ed = prefs.edit();
		if (objeto instanceof String) {
			ed.putString(key, (String) objeto);
		} else if (objeto instanceof Boolean) {
			ed.putBoolean(key, (Boolean) objeto);
		} else if (objeto instanceof Integer) {
			ed.putInt(key, (Integer) objeto);
		} else if (objeto instanceof Long) {
			ed.putLong(key, (Long) objeto);
		} else if (objeto instanceof Float) {
			ed.putFloat(key, (Float) objeto);
		} else {
			String json = GsonUtil.getJsonObject(objeto);
			ed.putString(key, json);
		}

		ed.commit();
	}

	@SuppressWarnings("rawtypes")
	public static Object getPreferences(Context ctx, String key, Type clase) {

		SharedPreferences prefs = new ObscuredSharedPreferences(ctx,
				ctx.getSharedPreferences("preferencias", Context.MODE_PRIVATE));

		try {
			if (clase.equals(String.class)) {
				return prefs.getString(key, "");
			} else if (clase.equals(Boolean.class)) {
				return prefs.getBoolean(key, false);
			} else if (clase.equals(Integer.class)) {
				return prefs.getInt(key, -1);
			} else if (clase.equals(Long.class)) {
				return prefs.getLong(key, -1);
			} else if (clase.equals(Float.class)) {
				return prefs.getFloat(key, -1);
			} else {

				return new GsonUtil().getBuilder().create()
						.fromJson(prefs.getString(key, ""), clase);
				// Object.getJSon(prefs.getString(key, ""));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		}
		return null;

	}

	public static void removePreference(Context ctx, String key) {

		SharedPreferences prefs = new ObscuredSharedPreferences(ctx,
				ctx.getSharedPreferences("preferencias", Context.MODE_PRIVATE));

		Editor ed = prefs.edit();
		ed.remove(key);
		ed.commit();

	}

}
