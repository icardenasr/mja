package com.xabber.android.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class UtilLogin {

	public static String md5(String in) {

		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(in.getBytes());
			byte[] a = digest.digest();
			int len = a.length;
			StringBuilder sb = new StringBuilder(len << 1);
			for (int i = 0; i < len; i++) {
				sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
				sb.append(Character.forDigit(a[i] & 0x0f, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Encrypts string and encode in Base64
	public static String encrypt3DES(String plainText) {
		// ---- Use specified 3DES key and IV from other source --------------
		byte[] plaintext = plainText.getBytes();// input
		byte[] tdesKeyData = "d918103b5a2f4525851a268a".getBytes();// your
																	// encryption
																	// key

		// byte[] myIV = new byte[8];// initialization vector

		String encryptedString = null;
		try {

			Cipher c3des = Cipher.getInstance("DESede/ECB/PKCS7Padding");
			SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");

			// IvParameterSpec ivspec = new IvParameterSpec(myIV);

			c3des.init(Cipher.ENCRYPT_MODE, myKey);
			byte[] cipherText = c3des.doFinal(plaintext);
			String salida = Base64.encodeToString(cipherText, Base64.DEFAULT);
			// if(salida.endsWith("\n"))
			// {
			// salida=salida.substring(0,salida.length()-1);
			// }
			encryptedString = salida;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		// return Base64Coder.encodeString(new String(cipherText));
		return encryptedString;
	}

}
