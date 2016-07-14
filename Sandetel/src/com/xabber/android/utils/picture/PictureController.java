package com.xabber.android.utils.picture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PictureController {

	// FOTO
	public static final Integer ALTURA = 480;
	public static final Integer ANCHURA = 640;

	private static final int CAMERA_REQUEST = 1888;

	// Indica si se ha abierto la camara
	private static Boolean foto;

	// Directorio del archivo donde se guarda la foto
	private static String path;

	// Nombre del archivo
	private static String name;

	// ImageView donde se pinta el bitmap
	private static ImageView ivImagen;

	// Bitmap de la foto
	private static Bitmap fotoGuardar;

	private static Context context;

	public static void onActivityResult(int requestCode, int resultCode,
			Intent data) {

		if (requestCode == CAMERA_REQUEST && foto
				&& resultCode != Activity.RESULT_CANCELED) {
			try {

				Uri chosenImageUri = null;
				chosenImageUri = Uri.fromFile(new File(path, name));

				fotoGuardar = decodeUri(context, chosenImageUri);

				if (fotoGuardar != null) {

					if (ivImagen != null) {

						ivImagen.setScaleType(ScaleType.CENTER_CROP);
						ivImagen.setImageBitmap(fotoGuardar);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (resultCode == android.app.Activity.RESULT_OK && !foto) {
			Uri chosenImageUri = null;
			try {
				chosenImageUri = data.getData();

				fotoGuardar = decodeUri(context, chosenImageUri);

				if (fotoGuardar != null) {

					if (ivImagen != null) {

						ivImagen.setScaleType(ScaleType.CENTER_CROP);
						ivImagen.setImageBitmap(fotoGuardar);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public boolean hasImageCaptureBug() {

		// list of known devices that have the bug
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");

		return devices.contains(android.os.Build.BRAND + "/"
				+ android.os.Build.PRODUCT + "/" + android.os.Build.DEVICE);

	}

	/**
	 * Establece la imagen con un tamaño determinado
	 * 
	 * @param selectedImage
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Bitmap decodeUri(Context context, Uri selectedImage)
			throws FileNotFoundException {

		int desiredWidth = ANCHURA;
		int desiredHeight = ALTURA;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(context.getContentResolver()
				.openInputStream(selectedImage), null, options);

		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;

		// Only scale if the source is big enough. This code is just trying to
		// fit a image into a certain width.

		if (srcWidth > srcHeight) {

			if (desiredWidth > srcWidth)
				desiredWidth = srcWidth;

			// Calculate the correct inSampleSize/scale value. This helps reduce
			// memory use. It should be a power of 2
			// from:
			// http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
			int inSampleSize = 1;
			while (srcWidth / 2 > desiredWidth) {
				srcWidth /= 2;
				srcHeight /= 2;
				inSampleSize *= 2;
			}

			float desiredScale = (float) desiredWidth / srcWidth;

			// Decode with inSampleSize
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inSampleSize = inSampleSize;
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap sampledSrcBitmap = BitmapFactory.decodeStream(context
					.getContentResolver().openInputStream(selectedImage), null,
					options);

			Matrix matrix = new Matrix();
			matrix.postScale(desiredScale, desiredScale);

			return Bitmap.createBitmap(sampledSrcBitmap, 0, 0,
					sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(),
					matrix, true);
		} else {
			if (desiredHeight > srcHeight)
				desiredHeight = srcHeight;

			// Calculate the correct inSampleSize/scale value. This helps reduce
			// memory use. It should be a power of 2
			// from:
			// http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
			int inSampleSize = 1;
			while (srcHeight / 2 > desiredHeight) {
				srcWidth /= 2;
				srcHeight /= 2;
				inSampleSize *= 2;
			}

			float desiredScale = (float) desiredHeight / srcHeight;

			// Decode with inSampleSize
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inSampleSize = inSampleSize;
			options.inScaled = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap sampledSrcBitmap = BitmapFactory.decodeStream(context
					.getContentResolver().openInputStream(selectedImage), null,
					options);

			Matrix matrix = new Matrix();
			matrix.postScale(desiredScale, desiredScale);

			return Bitmap.createBitmap(sampledSrcBitmap, 0, 0,
					sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(),
					matrix, true);
		}

	}

	public static String getBase64() {

		String encode = null;
		if (fotoGuardar != null) {
			byte[] byteImag = convertBitmapToByte(fotoGuardar);
			encode = Base64.encodeToString(byteImag, Base64.DEFAULT);
		}
		return encode;
	}

	public static byte[] convertBitmapToByte(Bitmap bmp) {
		byte[] byteArray = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byteArray = stream.toByteArray();

		return byteArray;
	}

}
