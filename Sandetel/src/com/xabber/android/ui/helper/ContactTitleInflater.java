/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * 
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * 
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.xabber.android.ui.helper;

import java.util.Collection;

import org.jivesoftware.smackx.ChatState;

import android.app.Activity;
import android.content.res.TypedArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.StatusMode;
import com.xabber.android.data.extension.avatar.AvatarManager;
import com.xabber.android.data.extension.cs.ChatStateManager;
import com.xabber.android.data.extension.muc.RoomContact;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.RosterContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.utils.Emoticons;

import es.juntadeandalucia.android.im.R;

/**
 * Helper class to update <code>contact_title.xml</code>.
 * 
 * @author alexander.ivanov
 * 
 */
public class ContactTitleInflater {

	public static void setStatusUser(Activity activity, AbstractContact ac,
			TextView tv, String text, ImageView statusModeView) {

		if (text != null && !text.equals("")) {
			tv.setText(text);
		} else {
			if (ac != null) {
				tv.setText(activity.getString(ac.getStatusMode().getStringID()));
			}
		}

		if (ac != null) {
			switch (ac.getStatusMode().getStatusLevel()) {

			case 0:

				tv.setTextColor(activity.getResources()
						.getColor(R.color.online));

				break;

			case 1:

				tv.setTextColor(activity.getResources()
						.getColor(R.color.online));

				break;

			case 2:

				tv.setTextColor(activity.getResources().getColor(R.color.away));

				break;

			case 3:

				tv.setTextColor(activity.getResources().getColor(R.color.away));

				break;

			// case 6:
			// tv.setText(activity.getString(R.string.unsubscribed));
			// tv.setTextColor(activity.getResources().getColor(
			// R.color.offline));
			// statusModeView.setImageResource(R.drawable.chat_bola_roja);
			//
			// break;

			// No autorizado
			case 8:

				tv.setTextColor(activity.getResources().getColor(
						R.color.offline));

				statusModeView.setImageResource(R.drawable.chat_bola_roja);
				break;

			default:

				tv.setTextColor(activity.getResources().getColor(
						R.color.offline));

				break;
			}
		}
	}

	/**
	 * Fill title with information about {@link AbstractContact} and provides
	 * back button callback.
	 * 
	 * @param titleView
	 * @param activity
	 * @param abstractContact
	 */
	public static void updateTitle(View titleView, final Activity activity,
			AbstractContact abstractContact) {
		final TypedArray typedArray = activity
				.obtainStyledAttributes(R.styleable.ContactList);

		// final Drawable titleAccountBackground = typedArray
		// .getDrawable(R.styleable.ContactList_titleAccountBackground);

		typedArray.recycle();
		final TextView nameView = (TextView) titleView.findViewById(R.id.name);
		final ImageView avatarView = (ImageView) titleView
				.findViewById(R.id.avatar);
		final ImageView statusModeView = (ImageView) titleView
				.findViewById(R.id.status_mode);
		final TextView statusTextView = (TextView) titleView
				.findViewById(R.id.status_text);
		final ImageButton backButton = (ImageButton) titleView
				.findViewById(R.id.back_button);

		// final View shadowView = titleView.findViewById(R.id.shadow);

		// titleView.setBackgroundDrawable(titleAccountBackground);

		if (abstractContact instanceof RoomContact) {

			LinearLayout llStatusContainer = (LinearLayout) titleView
					.findViewById(R.id.ll_status_container);
			llStatusContainer.setVisibility(View.GONE);

			String statusRoom = ((RoomContact) abstractContact).getStatusText();
			if (statusRoom != null && !statusRoom.equals("")) {
				nameView.setText(statusRoom);
			} else {
				String[] name = abstractContact.getName().split("@");
				nameView.setText(name[0]);
			}

			backButton.setImageDrawable(activity.getResources().getDrawable(
					R.drawable.charlas));

		} else {

			LinearLayout llStatusContainer = (LinearLayout) titleView
					.findViewById(R.id.ll_status_container);
			llStatusContainer.setVisibility(View.VISIBLE);
			String[] name = abstractContact.getName().split("@");
			nameView.setText(name[0]);

			backButton.setImageDrawable(AvatarManager.getInstance()
					.getUserAvatarForContactList(abstractContact.getUser()));
		}

		// }
		statusModeView.setImageLevel(abstractContact.getStatusMode()
				.getStatusLevel());
		if (titleView.getBackground() != null)
			titleView.getBackground().setLevel(
					AccountManager.getInstance().getColorLevel(
							abstractContact.getAccount()));
		avatarView.setImageDrawable(abstractContact.getAvatar());
		ChatState chatState = ChatStateManager.getInstance().getChatState(
				abstractContact.getAccount(), abstractContact.getUser());

		StatusMode statusMode = abstractContact.getStatusMode();

		final CharSequence statusText;
		Boolean exist = false;
		if (statusMode == StatusMode.unsubscribed) {

			statusText = Emoticons.getSmiledText(activity,
					abstractContact.getStatusText());
			statusModeView.setImageResource(R.drawable.chat_bola_roja);

		} else {

			// Obtenemos todos los contactos del usuario
			Collection<RosterContact> contacts = RosterManager.getInstance()
					.getContacts();

			// Comparamos con el nuevo usuario para comprobar si ya está en la
			// lista

			String userAdd = abstractContact.getName();
			for (RosterContact rosterContact : contacts) {

				if (userAdd.equalsIgnoreCase(rosterContact.getName())) {
					exist = true;
					break;
				}

			}

			if (exist) {
				if (statusMode == StatusMode.available) {
					statusModeView.setImageResource(R.drawable.chat_bola_verde);
				}

				if (chatState == ChatState.composing)

					statusText = activity
							.getString(R.string.chat_state_composing);

				else if (chatState == ChatState.paused)
					statusText = activity.getString(R.string.chat_state_paused);
				else
					statusText = Emoticons.getSmiledText(activity,
							abstractContact.getStatusText());
			} else {
				statusText = "";
				statusTextView.setText(activity
						.getString(R.string.unsubscribed));
				statusTextView.setTextColor(activity.getResources().getColor(
						R.color.offline));
				statusModeView.setImageResource(R.drawable.chat_bola_roja);
				abstractContact = null;
			}
		}

		ContactTitleInflater.setStatusUser(activity, abstractContact,
				statusTextView, statusText.toString(), statusModeView);

		// statusTextView.setText(statusText);
		// final Bitmap bitmap = BitmapFactory.decodeResource(
		// activity.getResources(), R.drawable.shadow);
		// final BitmapDrawable shadowDrawable = new BitmapDrawable(bitmap);
		// shadowDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		// shadowView.setBackgroundDrawable(shadowDrawable);
		// if (abstractContact.isConnected())
		// shadowView.setVisibility(View.GONE);
		// else
		// shadowView.setVisibility(View.VISIBLE);

		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
			}
		});
	}
}
