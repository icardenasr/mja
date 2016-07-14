package com.xabber.android.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class FileUtil {

	// private final static Integer IMAGE_MAX_SIZE = 70;
	private static final String PATH_FILE = "multas";

	public static File getSavePath() {
		File path;
		if (hasSDCard()) { // SD card
			path = new File(getSDCardPath() + "/" + PATH_FILE + "/");
			path.mkdir();
		} else {
			path = Environment.getDataDirectory();
		}
		return path;
	}

	public static File getCacheFilenameCard(String name) {
		File f;
		if (hasSDCard()) { // SD card
			f = new File(name);
			f.mkdir();
		} else {
			f = Environment.getDataDirectory();
		}
		return f;
	}

	public static String getCacheFilename(String name) {
		File f = getSavePath();

		return f.getAbsolutePath() + "/" + name;
	}

	public static String getCacheFilename(Integer id) {
		File f = getSavePath();

		return f.getAbsolutePath() + "/" + id;
	}

	public static void ShowPicture(String fileName, ImageView pic) {
		File f = new File(Environment.getExternalStorageDirectory(), fileName);
		FileInputStream is = null;
		try {
			is = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			Log.d("error: ", String.format(
					"ShowPicture.java file[%s]Not Found", fileName));
			return;
		}

		Bitmap bm = BitmapFactory.decodeStream(is, null, null);
		pic.setImageBitmap(bm);
	}

	public static Bitmap loadFromFile(String filename) {
		try {
			File f = new File(filename);
			if (!f.exists()) {
				return null;
			}
			Bitmap tmp = BitmapFactory.decodeFile(filename);
			return tmp;
		} catch (Exception e) {
			return null;
		}
	}

	public static Bitmap loadFromCacheFile(String name) {
		return loadFromFile(getCacheFilename(name));
	}

	public static void saveToCacheFile(Bitmap bmp, String name) {
		saveToFile(getCacheFilename(name), bmp);
	}

	public static void saveToFile(String filename, Bitmap bmp) {
		try {
			FileOutputStream out = new FileOutputStream(filename);
			bmp.compress(CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
		}
	}

	public static boolean hasSDCard() { // SD????????
		String status = Environment.getExternalStorageState();

		return status.equals(Environment.MEDIA_MOUNTED);
	}

	public static String getSDCardPath() {
		File path = Environment.getExternalStorageDirectory();
		return path.getAbsolutePath();
	}

	/**
	 * Creates the specified <code>toFile</code> as a byte for byte copy of the
	 * <code>fromFile</code>. If <code>toFile</code> already exists, then it
	 * will be replaced with a copy of <code>fromFile</code>. The name and path
	 * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
	 * <br/>
	 * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
	 * this function.</i>
	 * 
	 * @param fromFile
	 *            - FileInputStream for the file to copy from.
	 * @param toFile
	 *            - FileInputStream for the file to copy to.
	 */
	public static void copyFile(FileInputStream fromFile,
			FileOutputStream toFile) throws IOException {
		FileChannel fromChannel = null;
		FileChannel toChannel = null;
		try {
			fromChannel = fromFile.getChannel();
			toChannel = toFile.getChannel();
			fromChannel.transferTo(0, fromChannel.size(), toChannel);
		} finally {
			try {
				if (fromChannel != null) {
					fromChannel.close();
				}
			} finally {
				if (toChannel != null) {
					toChannel.close();
				}
			}
		}
	}

	public static File copyToTemp(File fromFileName) {

		File toFileName = null;
		try {
			toFileName = File.createTempFile("pre", "suf");
		} catch (IOException e) {
			e.printStackTrace();
		}

		copy(fromFileName, toFileName);

		return toFileName;

	}

	public static Boolean copy(String fromFileName, String toFileName)
			throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		return copy(fromFile, toFile);
	}

	// public static Boolean copy(File fromFile, File toFile) {
	//
	// Boolean result = false;
	// if (fromFile != null && toFile != null) {
	//
	// try {
	// if (!fromFile.exists())
	//
	// throw new IOException("FileCopy: "
	// + "no such source file: " + fromFile.getName());
	//
	// if (!fromFile.isFile())
	// throw new IOException("FileCopy: "
	// + "can't copy directory: " + fromFile.getName());
	// if (!fromFile.canRead())
	// throw new IOException("FileCopy: "
	// + "source file is unreadable: "
	// + fromFile.getName());
	//
	// if (toFile.isDirectory())
	// toFile = new File(toFile, fromFile.getName());
	//
	// if (toFile.exists()) {
	// if (!toFile.canWrite())
	// throw new IOException("FileCopy: "
	// + "destination file is unwriteable: "
	// + toFile.getName());
	// System.out.print("Overwrite existing file "
	// + toFile.getName() + "? (Y/N): ");
	// System.out.flush();
	// BufferedReader in = new BufferedReader(
	// new InputStreamReader(System.in));
	// String response = in.readLine();
	// if (!response.equals("Y") && !response.equals("y"))
	// throw new IOException("FileCopy: "
	// + "existing file was not overwritten.");
	// } else {
	// String parent = toFile.getParent();
	// if (parent == null)
	// parent = System.getProperty("user.dir");
	// File dir = new File(parent);
	// if (!dir.exists())
	// throw new IOException("FileCopy: "
	// + "destination directory doesn't exist: "
	// + parent);
	// if (dir.isFile())
	// throw new IOException("FileCopy: "
	// + "destination is not a directory: " + parent);
	// if (!dir.canWrite())
	// throw new IOException("FileCopy: "
	// + "destination directory is unwriteable: "
	// + parent);
	// }
	//
	// FileInputStream from = null;
	// FileOutputStream to = null;
	// try {
	// from = new FileInputStream(fromFile);
	// to = new FileOutputStream(toFile);
	// byte[] buffer = new byte[4096];
	// int bytesRead;
	//
	// while ((bytesRead = from.read(buffer)) != -1)
	// to.write(buffer, 0, bytesRead); // write
	// } finally {
	// if (from != null)
	// try {
	// from.close();
	// } catch (IOException e) {
	// ;
	// }
	// if (to != null)
	// try {
	// to.close();
	// } catch (IOException e) {
	// ;
	// }
	// result = true;
	// }
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// } else {
	// result = null;
	// }
	//
	// return result;
	// }
	public static Boolean copy(File src, File dst) {
		Boolean result = false;
		try {
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			result = true;
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		return result;
	}

}
