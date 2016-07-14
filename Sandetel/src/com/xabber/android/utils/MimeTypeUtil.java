package com.xabber.android.utils;

import java.util.Hashtable;

public class MimeTypeUtil {

	static String audio[] = { "audio/mp3" };
	static String video[] = { "video/mpeg", "video/3gpp", "video/mpeg4" };
	static String image[] = { "image/bmp", "image/png", "image/jpeg",
			"image/jpg" };
	static String text[] = { "text/" };
	static String pdf[] = { "application/pdf" };
	static String comprimidos[] = { "application/x-rar-compressed",
			"application/zip", "application/x-gtar", "application/x-gzip",
			"application/octet-stream" };

	private static Hashtable<String, String> mimeTable = new Hashtable<String, String>();
	static {
		// Text Plain
		mimeTable.put("txt", "text/plain");

		// Image type
		mimeTable.put("jpeg", "image/jpeg");
		mimeTable.put("jpg", "image/jpg");
		mimeTable.put("png", "image/png");
		mimeTable.put("bmp", "image/bmp");

		// Video type
		mimeTable.put("mp4", "video/mpeg4");
		mimeTable.put("flv", "video/mpeg4");
		mimeTable.put("3gp", "video/3gpp");
		mimeTable.put("wmv", "video/mpeg");
		mimeTable.put("mpeg", "video/mpeg");
		mimeTable.put("mpg", "video/mpeg");

		// Audio type
		mimeTable.put("mp3", "audio/mp3");
		mimeTable.put("wma", "audio/mp3");

		// PDF
		mimeTable.put("psf", "application/pdf");

		// Comprimidos
		mimeTable.put("rar", "application/x-rar-compressed");
		mimeTable.put("zip", "application/zip");
		mimeTable.put("gtar", "application/x-gtar");
		mimeTable.put("gz", "application/x-gzip");
		mimeTable.put("zip", "application/octet-stream");

		// Text Plain
		mimeTable.put("text/plain", "txt");

		// Image type
		mimeTable.put("image/jpg", "jpg");
		mimeTable.put("image/jpeg", "jpeg");
		mimeTable.put("image/png", "png");
		mimeTable.put("image/bmp", "bmp");

		// Video type
		mimeTable.put("video/mpeg4", "mp4");
		mimeTable.put("video/mpeg4", "flv");
		mimeTable.put("video/3gpp", "3gp");
		mimeTable.put("video/mpeg", "wmv");
		mimeTable.put("video/mpeg", "mpeg");
		mimeTable.put("video/mpeg", "mpg");

		// Audio type
		mimeTable.put("audio/mp3", "mp3");
		mimeTable.put("audio/mp3", "wma");

		// PDF
		mimeTable.put("application/pdf", "pdf");

		// Comprimidos
		mimeTable.put("application/x-rar-compressed", "rar");
		mimeTable.put("application/zip", "zip");
		mimeTable.put("application/x-gtar", "gtar");
		mimeTable.put("application/x-gzip", "gz");
		mimeTable.put("application/octet-stream", "zip");

	}

	public static String getMimeType(String extension) {
		if (extension == null || extension.isEmpty()) {
			return null;
		}
		return mimeTable.get(extension.toLowerCase());
	}

	public static String getFileExtension(String url) {
		if ((url != null) && (url.indexOf('.') != -1)) {
			return url.substring(url.lastIndexOf('.') + 1);
		}

		return "";
	}

	private static Boolean isType(String[] list, String mime) {
		Boolean result = false;

		if (list != null) {
			for (String item : list) {
				String mimeType = mimeTable.get(item);
				if (mimeType != null) {
					if (mime != null && mime.toLowerCase().contains(mimeType)) {
						result = true;
						break;
					}
				}
			}
		}

		return result;

	}

	public static boolean isImageType(String mime) {
		return isType(image, mime);
	}

	public static boolean isVideoType(String mime) {
		return isType(video, mime);
	}

	public static boolean isAudioType(String mime) {
		return isType(audio, mime);
	}

	public static boolean isTextType(String mime) {
		return isType(text, mime);
	}

	public static boolean isPDFType(String mime) {
		return isType(pdf, mime);
	}

	public static boolean isCompressedType(String mime) {
		return isType(comprimidos, mime);
	}

}
