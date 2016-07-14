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
package com.xabber.android.ui.adapter;

import java.util.Collection;

import org.jivesoftware.smackx.ChatState;

import Constantes.Constantes;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.account.StatusMode;
import com.xabber.android.data.extension.cs.ChatStateManager;
import com.xabber.android.data.extension.muc.RoomContact;
import com.xabber.android.data.message.chat.ChatManager;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.RosterContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.ui.helper.AbstractAvatarInflaterHelper;
import com.xabber.android.utils.Emoticons;
import com.xabber.android.utils.preferences.UtilPreferences;

import es.juntadeandalucia.android.im.R;

/**
 * Provides views and fills them with data for {@link BaseContactAdapter}.
 * 
 * @author alexander.ivanov
 * 
 */
public abstract class BaseContactInflater {

	final Activity activity;

	final LayoutInflater layoutInflater;

	final AbstractAvatarInflaterHelper avatarInflaterHelper;

	/**
	 * Repeated shadow for drawable.
	 */
	final BitmapDrawable shadowDrawable;

	/**
	 * Managed adapter.
	 */
	BaseAdapter adapter;

	/**
	 * Logued account
	 */
	private String account;

	public BaseContactInflater(Activity activity) {
		this.activity = activity;
		layoutInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		avatarInflaterHelper = AbstractAvatarInflaterHelper
				.createAbstractContactInflaterHelper();

		Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),
				R.drawable.shadow);
		shadowDrawable = new BitmapDrawable(bitmap);
		shadowDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);

		account = (String) UtilPreferences.getPreferences(activity,
				Constantes.ACCOUNT, String.class);

	}

	/**
	 * Sets managed adapter.
	 * 
	 * @param adapter
	 */
	void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Creates new view for specified position.
	 * 
	 * @param position
	 * @param parent
	 * @return
	 */
	abstract View createView(int position, ViewGroup parent);

	/**
	 * Creates new instance of ViewHolder.
	 * 
	 * @param position
	 * @param view
	 * @return
	 */
	abstract ViewHolder createViewHolder(int position, View view);

	/**
	 * Returns status text.
	 * 
	 * @param abstractContact
	 * @return
	 */
	String getStatusText(AbstractContact abstractContact) {
		return abstractContact.getStatusText();
	}

	public static void setStatusUser(Activity activity, AbstractContact ac,
			TextView tv, String text, ImageView iv) {

		if (text != null && !text.equals("")) {
			if (text.contains("consigna.juntadeandalucia.es")) {
				// Collection<MessageItem> messages =
				// MessageManager.getInstance()
				// .getMessages(ac.getAccount(), ac.getUser());
				// MessageItem messageItemNotify = null;
				// for (MessageItem messageItem : messages) {
				// if (messageItem.getText().equals(text)) {
				// messageItemNotify = messageItem;
				// break;
				// }
				// }
				//
				// if (messageItemNotify != null) {
				// if (messageItemNotify != null
				// && messageItemNotify.getConsigna() != null) {
				// text = messageItemNotify.getConsigna().getName()
				// + " | "
				// + messageItemNotify.getConsigna().getSize();
				// }
				//
				// tv.setText(text);
				// } else {
				// tv.setText(text);
				// }

				tv.setText(activity.getResources().getString(
						R.string.file_received));

			} else {
				tv.setText(text);
			}
		} else {
			tv.setText(activity.getString(ac.getStatusMode().getStringID()));
		}

		if (ac != null) {
			switch (ac.getStatusMode().getStatusLevel()) {

			case 0:

				tv.setTextColor(activity.getResources()
						.getColor(R.color.online));
				iv.setImageResource(R.drawable.chat_bola_verde_listo);

				break;

			case 1:

				tv.setTextColor(activity.getResources()
						.getColor(R.color.online));
				iv.setImageResource(R.drawable.chat_bola_verde);

				break;

			case 2:

				tv.setTextColor(activity.getResources().getColor(R.color.away));
				iv.setImageResource(R.drawable.chat_bola_ausente);

				break;

			case 3:

				tv.setTextColor(activity.getResources().getColor(R.color.away));
				iv.setImageResource(R.drawable.chat_bola_ausente_extendido);

				break;
			// NO autorizado
			case 8:

				tv.setTextColor(activity.getResources().getColor(
						R.color.offline));
				iv.setImageResource(R.drawable.chat_bola_roja);

				break;

			// case 6:
			// tv.setText(activity.getString(R.string.unsubscribed));
			// tv.setTextColor(activity.getResources().getColor(
			// R.color.offline));
			// iv.setImageResource(R.drawable.chat_bola_roja);
			//
			// break;

			default:

				tv.setTextColor(activity.getResources().getColor(
						R.color.offline));
				iv.setImageResource(R.drawable.chat_bola_roja);

				break;
			}
		}
	}

	/**
	 * Fills view for {@link BaseContactAdapter}.
	 * 
	 * @param view
	 *            view to be inflated.
	 * @param abstractContact
	 *            contact to be shown.
	 */
	public void getView(View view, AbstractContact abstractContact) {
		final ViewHolder viewHolder = (ViewHolder) view.getTag();
		// if (abstractContact.isConnected()) {
		// viewHolder.shadow.setVisibility(View.GONE);
		// } else {
		// viewHolder.shadow.setVisibility(View.VISIBLE);
		// }

		// viewHolder.color.setImageLevel(abstractContact.getColorLevel());

		if (SettingsManager.contactsShowAvatars()) {
			viewHolder.avatar.setVisibility(View.VISIBLE);
			Drawable d = abstractContact.getAvatarForContactList();
			if (d != null) {
				viewHolder.avatar.setImageDrawable(abstractContact
						.getAvatarForContactList());
			} else {
				viewHolder.avatar.setImageResource(R.drawable.ico_user);
			}
			// avatarInflaterHelper.updateAvatar(viewHolder.avatar,
			// abstractContact);
			// ((RelativeLayout.LayoutParams)
			// viewHolder.panel.getLayoutParams())
			// .addRule(RelativeLayout.RIGHT_OF, R.id.avatar);
		} else {
			viewHolder.avatar.setVisibility(View.GONE);
			// ((RelativeLayout.LayoutParams)
			// viewHolder.panel.getLayoutParams())
			// .addRule(RelativeLayout.RIGHT_OF, R.id.color);
		}

		if (account != null) {
			Uri uri = ChatManager.getInstance().getSound(account,
					abstractContact.getUser());

			if (uri != null) {
				viewHolder.statusSilenceMode.setVisibility(View.GONE);
			} else {
				viewHolder.statusSilenceMode.setVisibility(View.VISIBLE);
			}
		}

		// AccountManager.getInstance().getAccount(abstractContact.getAccount()).getColorIndex()

		if (abstractContact instanceof RoomContact) {

			// ((RoomContact) abstractContact).getStatusMode().get

			final CharSequence statusText;
			ChatState chatState = ChatStateManager.getInstance().getChatState(
					abstractContact.getAccount(), abstractContact.getUser());

			if (chatState == ChatState.composing)
				statusText = activity.getString(R.string.chat_state_composing);
			else if (chatState == ChatState.paused)
				statusText = activity.getString(R.string.chat_state_paused);
			else
				statusText = Emoticons.getSmiledText(activity,
						abstractContact.getStatusText());

			// ContactTitleInflater.setStatusUser(activity, abstractContact,
			// statusTextView, statusText.toString());

			if (statusText != null && !statusText.toString().equals("")) {

				viewHolder.name.setText(statusText);
			} else {
				viewHolder.name
						.setText(abstractContact.getName().split("@")[0]);
			}

		} else {

			viewHolder.name.setText(abstractContact.getName().split("@")[0]);
		}
		// final String statusText = getStatusText(abstractContact);

		final String statusText = getStatusText(abstractContact);
		viewHolder.name.getLayoutParams().height = activity.getResources()
				.getDimensionPixelSize(R.dimen.contact_name_height_show_status);
		viewHolder.name.setGravity(Gravity.BOTTOM);
		viewHolder.status.setVisibility(View.VISIBLE);

		if (abstractContact.getStatusMode() == StatusMode.unsubscribed) {
			viewHolder.status
					.setText(activity.getString(R.string.unsubscribed));
			viewHolder.status.setTextColor(activity.getResources().getColor(
					R.color.offline));
			viewHolder.iconLine.setImageResource(R.drawable.chat_bola_roja);
		} else {

			Boolean exist = false;
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

			setStatusUser(activity, abstractContact, viewHolder.status,
					statusText, viewHolder.iconLine);
		}

		// viewHolder.shadow.setBackgroundDrawable(shadowDrawable);
	}

	/**
	 * Holder for views in contact item.
	 */
	static class ViewHolder {

		// final ImageView color;
		final ImageView iconLine;
		final ImageView avatar;
		final LinearLayout panel;
		final TextView name;
		final TextView status;
		final ImageView statusSilenceMode;

		// final ImageView shadow;

		public ViewHolder(View view) {
			// color = (ImageView) view.findViewById(R.id.color);
			iconLine = (ImageView) view.findViewById(R.id.iv_online);
			avatar = (ImageView) view.findViewById(R.id.avatar);
			panel = (LinearLayout) view.findViewById(R.id.panel);
			name = (TextView) view.findViewById(R.id.name);
			status = (TextView) view.findViewById(R.id.status);
			statusSilenceMode = (ImageView) view.findViewById(R.id.iv_silence);
			// shadow = (ImageView) view.findViewById(R.id.shadow);
		}

	}

}
