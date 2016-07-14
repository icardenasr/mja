package com.xabber.android.utils.share;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.xabber.android.data.message.MessageItem;
import com.xabber.android.ui.ContactList;
import com.xabber.android.utils.clipboard.ClipBoardUtil;

import es.juntadeandalucia.android.im.R;

public class ChatMessageManager {

	private static final int ACTION_COPY = 0;
	private static final int ACTION_FORWARD = 1;

	private static ChatMessageManager instance;

	public static ChatMessageManager getInstance() {

		if (instance == null) {
			instance = new ChatMessageManager();
		}
		return instance;
	}

	/**
	 * Crea un diálogo de selección de télefonos
	 * 
	 * @param phones
	 */
	public void createDialogOptions(final Context context,
			final MessageItem messageItem) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.what_to_do).setItems(
				context.getResources().getTextArray(R.array.share_options),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case ACTION_COPY:
							actionCopy(context, messageItem);
							break;
						case ACTION_FORWARD:
							actionForward(context, messageItem);
							break;

						default:
							break;
						}
					}
				});
		builder.create().show();

	}

	private void actionCopy(Context context, MessageItem messageItem) {

		ClipBoardUtil.copyToClipBoard(context, messageItem.getText());

	}

	private void actionForward(Context context, MessageItem messageItem) {

		MessageItem sendMessage = new MessageItem(messageItem, false);

		context.startActivity(ContactList.createForwardIntent(context,
				sendMessage));

	}

}
