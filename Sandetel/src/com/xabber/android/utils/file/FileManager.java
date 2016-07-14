package com.xabber.android.utils.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import com.xabber.android.data.Application;
import com.xabber.android.service.constantes.Urls;
import com.xabber.android.service.ws_client.dto.Data;

public class FileManager {

	public FileManager() {
	}

	private final static FileManager instance;

	static {
		instance = new FileManager();
		Application.getInstance().addManager(instance);
	}

	public static FileManager getInstance() {
		return instance;
	}

	public void openFile(Context context, String path) throws IOException {
		// Create URI
		File url = new File(path);
		Uri uri = Uri.fromFile(url);

		Intent intent = new Intent(Intent.ACTION_VIEW);

		String pathAux = path.toLowerCase();
		// Check what kind of file you are trying to open, by comparing the url
		// with extensions.
		// When the if condition is matched, plugin sets the correct intent
		// (mime) type,
		// so Android knew what application to use to open the file
		if (pathAux.toString().contains(".doc")
				|| pathAux.toString().contains(".docx")) {
			// Word document
			intent.setDataAndType(uri, "application/msword");
		} else if (pathAux.toString().contains(".pdf")) {
			// PDF file
			intent.setDataAndType(uri, "application/pdf");
		} else if (pathAux.toString().contains(".ppt")
				|| pathAux.toString().contains(".pptx")) {
			// Powerpoint file
			intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		} else if (pathAux.toString().contains(".xls")
				|| pathAux.toString().contains(".xlsx")) {
			// Excel file
			intent.setDataAndType(uri, "application/vnd.ms-excel");
		} else if (pathAux.toString().contains(".zip")
				|| pathAux.toString().contains(".rar")) {
			// WAV audio file
			intent.setDataAndType(uri, "application/x-wav");
		} else if (pathAux.toString().contains(".rtf")) {
			// RTF file
			intent.setDataAndType(uri, "application/rtf");
		} else if (pathAux.toString().contains(".wav")
				|| pathAux.toString().contains(".mp3")) {
			// WAV audio file
			intent.setDataAndType(uri, "audio/x-wav");
		} else if (pathAux.toString().contains(".gif")) {
			// GIF file
			intent.setDataAndType(uri, "image/gif");
		} else if (pathAux.toString().contains(".jpg")
				|| pathAux.toString().contains(".jpeg")
				|| pathAux.toString().contains(".png")) {
			// JPG file
			intent.setDataAndType(uri, "image/jpeg");
		} else if (pathAux.toString().contains(".txt")) {
			// Text file
			intent.setDataAndType(uri, "text/plain");
		} else if (pathAux.toString().contains(".3gp")
				|| pathAux.toString().contains(".mpg")
				|| pathAux.toString().contains(".mpeg")
				|| pathAux.toString().contains(".mpe")
				|| pathAux.toString().contains(".mp4")
				|| pathAux.toString().contains(".avi")) {
			// Video files
			intent.setDataAndType(uri, "video/*");
		} else {
			// if you want you can also define the intent type for any other
			// file

			// additionally use else clause below, to manage other unknown
			// extensions
			// in this case, Android will show all applications installed on the
			// device
			// so you can choose which application to use
			intent.setDataAndType(uri, "*/*");
		}

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public String saveFile(Context context, String fileName, Data base64) {

		String absolutePath = null;
		Boolean availableToWrite = checkExternalMedia();
		if (availableToWrite != null) {
			absolutePath = writeToSDFile(fileName, base64);
		}

		// readRaw();

		return absolutePath;
	}

	/**
	 * Method to check whether external media available and writable. This is
	 * adapted from
	 * http://developer.android.com/guide/topics/data/data-storage.html
	 * #filesExternal
	 */

	private Boolean checkExternalMedia() {
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// Can read and write the media
			mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Can only read the media
			mExternalStorageWriteable = false;
		} else {
			// Can't read or write
			mExternalStorageWriteable = false;
		}

		return mExternalStorageWriteable;
	}

	private String writeToSDFile(String fileName, Data data) {

		File root = android.os.Environment.getExternalStorageDirectory();

		File dir = new File(root.getAbsolutePath() + File.separator
				+ Urls.LOCAL_CONSIGNA);
		dir.mkdirs();
		File file = new File(dir, fileName);

		try {

			FileOutputStream os = new FileOutputStream(file, true);
			os.write(data.getContenido());
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String path = null;
		if (file.exists()) {
			path = file.getAbsolutePath();
		}
		return path;
	}

	public String encodeTobase64(Bitmap image) {
		Bitmap immagex = image;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

		return imageEncoded;
	}

	public Bitmap decodeBase64(String input) {
		byte[] decodedByte = Base64.decode(input, 0);
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}
