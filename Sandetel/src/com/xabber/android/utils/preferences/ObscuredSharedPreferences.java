package com.xabber.android.utils.preferences;

import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Base64;

/**
 * Warning, this gives a false sense of security. If an attacker has enough
 * access to acquire your password store, then he almost certainly has enough
 * access to acquire your source binary and figure out your encryption key.
 * However, it will prevent casual investigators from acquiring passwords, and
 * thereby may prevent undesired negative publicity.
 * 
 * 
 * @version 1.0 24 Oct 2013
 * @author José Fernández
 */

public class ObscuredSharedPreferences implements SharedPreferences {
	protected static final String UTF8 = "utf-8";
	public char[] sekrit;

	protected SharedPreferences delegate;
	protected Context context;

	public ObscuredSharedPreferences(Context context, SharedPreferences delegate) {
		this.delegate = delegate;
		this.context = context;

		sekrit = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID).toCharArray();
	}

	public class Editor implements SharedPreferences.Editor {
		protected SharedPreferences.Editor delegate;

		public Editor() {
			this.delegate = ObscuredSharedPreferences.this.delegate.edit();
		}

		@Override
		public Editor putBoolean(String key, boolean value) {
			delegate.putString(encrypt(key), encrypt(Boolean.toString(value)));
			return this;
		}

		@Override
		public Editor putFloat(String key, float value) {
			delegate.putString(encrypt(key), encrypt(Float.toString(value)));
			return this;
		}

		@Override
		public Editor putInt(String key, int value) {
			delegate.putString(encrypt(key), encrypt(Integer.toString(value)));
			return this;
		}

		@Override
		public Editor putLong(String key, long value) {
			delegate.putString(encrypt(key), encrypt(Long.toString(value)));
			return this;
		}

		@Override
		public Editor putString(String key, String value) {
			delegate.putString(encrypt(key), encrypt(value));
			return this;
		}

		@Override
		public void apply() {
			delegate.apply();
		}

		@Override
		public Editor clear() {
			delegate.clear();
			return this;
		}

		@Override
		public boolean commit() {
			return delegate.commit();
		}

		@Override
		public Editor remove(String s) {
			delegate.remove(s);
			return this;
		}

		@Override
		public android.content.SharedPreferences.Editor putStringSet(
				String arg0, Set<String> arg1) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public Editor edit() {
		return new Editor();
	}

	@Override
	public Map<String, ?> getAll() {
		throw new UnsupportedOperationException(); // left as an exercise to the
													// reader
	}

	@Override
	public boolean getBoolean(String key, boolean defValue) {
		final String v = delegate.getString(encrypt(key), null);
		return v != null ? Boolean.parseBoolean(decrypt(v)) : defValue;
	}

	@Override
	public float getFloat(String key, float defValue) {
		final String v = delegate.getString(encrypt(key), null);
		return v != null ? Float.parseFloat(decrypt(v)) : defValue;
	}

	@Override
	public int getInt(String key, int defValue) {
		final String v = delegate.getString(encrypt(key), null);
		return v != null ? Integer.parseInt(decrypt(v)) : defValue;
	}

	@Override
	public long getLong(String key, long defValue) {
		final String v = delegate.getString(encrypt(key), null);
		return v != null ? Long.parseLong(decrypt(v)) : defValue;
	}

	@Override
	public String getString(String key, String defValue) {
		final String v = delegate.getString(encrypt(key), null);
		return v != null ? decrypt(v) : defValue;
	}

	@Override
	public boolean contains(String s) {
		return delegate.contains(s);
	}

	@Override
	public void registerOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		delegate.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	@Override
	public void unregisterOnSharedPreferenceChangeListener(
			OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
		delegate.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	@SuppressWarnings("deprecation")
	protected String encrypt(String value) {

		try {
			final byte[] bytes = value != null ? value.getBytes(UTF8)
					: new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(sekrit));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(
					Cipher.ENCRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(
							context.getContentResolver(),
							Settings.System.ANDROID_ID).getBytes(UTF8), 20));
			return new String(Base64.encode(pbeCipher.doFinal(bytes),
					Base64.NO_WRAP), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("deprecation")
	protected String decrypt(String value) {
		try {
			final byte[] bytes = value != null ? Base64.decode(value,
					Base64.DEFAULT) : new byte[0];
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance("PBEWithMD5AndDES");
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(sekrit));
			Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
			pbeCipher.init(
					Cipher.DECRYPT_MODE,
					key,
					new PBEParameterSpec(Settings.Secure.getString(
							context.getContentResolver(),
							Settings.System.ANDROID_ID).getBytes(UTF8), 20));
			return new String(pbeCipher.doFinal(bytes), UTF8);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Set<String> getStringSet(String arg0, Set<String> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}

/**
 * Warning, this gives a false sense of security. If an attacker has enough
 * access to acquire your password store, then he almost certainly has enough
 * access to acquire your source binary and figure out your encryption key.
 * However, it will prevent casual investigators from acquiring passwords, and
 * thereby may prevent undesired negative publicity.
 * 
 * public class ObscuredSharedPreferences {
 * 
 * private ObscuredSharedPreferences instance = null;
 * 
 * protected static final String UTF8 = "utf-8";
 * 
 * public char[] sekrit;
 * 
 * protected SharedPreferences delegate; protected Context context;
 * 
 * private SecretKeyFactory keyFactory; private SecretKey key;
 * 
 * private Cipher pbeCipher;
 * 
 * private ObscuredSharedPreferences() {
 * 
 * try { sekrit = Secure.getString(context.getContentResolver(),
 * Secure.ANDROID_ID).toCharArray();
 * 
 * keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
 * 
 * key = keyFactory.generateSecret(new PBEKeySpec(sekrit));
 * 
 * pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
 * 
 * } catch (InvalidKeySpecException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } catch (NoSuchAlgorithmException e) { // TODO
 * Auto-generated catch block e.printStackTrace(); } catch (Exception e) {
 * e.printStackTrace(); } }
 * 
 * public ObscuredSharedPreferences getInstance() { if (instance == null) {
 * instance = new ObscuredSharedPreferences(); } return instance; }
 * 
 * public ObscuredSharedPreferences(Context context, SharedPreferences delegate)
 * { this.delegate = delegate; this.context = context; }
 * 
 * protected String encrypt(String value) {
 * 
 * try { final byte[] bytes = value != null ? value.getBytes(UTF8) : new
 * byte[0];
 * 
 * pbeCipher.init( Cipher.ENCRYPT_MODE, key, new
 * PBEParameterSpec(Settings.Secure.getString( context.getContentResolver(),
 * Secure.ANDROID_ID) .getBytes(UTF8), 20)); return new
 * String(Base64.encode(pbeCipher.doFinal(bytes), Base64.NO_WRAP), UTF8);
 * 
 * } catch (Exception e) { throw new RuntimeException(e); }
 * 
 * }
 * 
 * protected String decrypt(String value) { try { final byte[] bytes = value !=
 * null ? Base64.decode(value, Base64.DEFAULT) : new byte[0];
 * 
 * pbeCipher.init( Cipher.DECRYPT_MODE, key, new
 * PBEParameterSpec(Settings.Secure.getString( context.getContentResolver(),
 * Secure.ANDROID_ID) .getBytes(UTF8), 20)); return new
 * String(pbeCipher.doFinal(bytes), UTF8);
 * 
 * } catch (Exception e) { throw new RuntimeException(e); } }
 * 
 * }
 */
