package com.xabber.android.utils.share.email;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import es.juntadeandalucia.android.im.R;

public class EmailManager {

	public static void sendFile(Context context, String pathFile) {
		sendFile(context, null, null, pathFile);
	}

	public static void sendFile(Context context, List<String> emailsTo) {
		sendFile(context, null, emailsTo, null);
	}

	public static void sendFile(Context context, String subject,
			List<String> emailsTo) {
		sendFile(context, subject, emailsTo, null);
	}

	public static void sendFile(Context context, String subject, String pathFile) {
		sendFile(context, subject, null, pathFile);
	}

	public static void sendFile(Context context, List<String> emailsTo,
			String pathFile) {
		sendFile(context, null, emailsTo, pathFile);
	}

	public static void sendFile(Context context, String subject,
			List<String> emailsTo, String pathFile) {

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB) {
			// Do something for GINGERBREAD and above versions
			sendFileGingerbread(context, pathFile, subject, emailsTo);
		} else {
			// do something for phones running an SDK before GINGERBREAD
			sendFileAllVersion(context, subject, emailsTo, pathFile);
		}

	}

	private static void sendFileAllVersion(Context context, String subject,
			List<String> emailsTo, String pathFile) {

		Intent intent = new Intent(android.content.Intent.ACTION_SENDTO);
		intent.setType("text/plain");
		if (subject != null) {
			intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if (emailsTo != null && !emailsTo.isEmpty()) {
			String emails = "";
			for (String email : emailsTo) {
				if (!emails.equalsIgnoreCase("")) {
					emails = emails + ",";
				}
				emails = emails + email;
			}
			if (!emails.equalsIgnoreCase("")) {
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { emails });
			}
		}
		intent.setData(Uri.parse("mailto:")); // only email apps should
												// handle this
		if (pathFile != null && !pathFile.equalsIgnoreCase("")) {
			Uri uri = Uri.fromFile(new File(pathFile));
			intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
		}

		Intent i = Intent.createChooser(intent,
				context.getString(R.string.export_chat));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(i);

	}

	private static void sendFileGingerbread(Context context, String pathFile,
			String subject, List<String> emailsTo) {

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("application/image");

		if (subject != null) {
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		}
		if (emailsTo != null && !emailsTo.isEmpty()) {
			String emails = "";
			for (String email : emailsTo) {
				if (!emails.equalsIgnoreCase("")) {
					emails = emails + ",";
				}
				emails = emails + email;
			}
			if (!emails.equalsIgnoreCase("")) {
				emailIntent.putExtra(Intent.EXTRA_EMAIL,
						new String[] { emails });
			}
		}
		emailIntent.putExtra(Intent.EXTRA_STREAM,
				Uri.parse("file://" + pathFile));

		Intent i = Intent.createChooser(emailIntent,
				context.getString(R.string.export_chat));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}
