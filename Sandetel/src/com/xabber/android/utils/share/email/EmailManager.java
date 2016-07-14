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

	public static void sendFile(Context context, List<String> emailsTo,
			String pathFile) {
		sendFile(context, null, emailsTo, pathFile);
	}

	public static void sendFile(Context context, String subject,
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
		context.startActivity(Intent.createChooser(intent,
				context.getString(R.string.export_chat)));

	}

}
