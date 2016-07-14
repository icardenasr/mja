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

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.extension.muc.Occupant;
import com.xabber.android.data.message.chat.ChatManager;
import com.xabber.android.ui.OccupantList;
import com.xabber.xmpp.muc.Role;

import es.juntadeandalucia.android.im.R;

/**
 * Adapter for {@link OccupantList}.
 * 
 * @author alexander.ivanov
 * 
 */
public class OccupantListAdapter extends BaseAdapter implements
		UpdatableAdapter {

	private final Activity activity;
	private final String account;
	private final String room;

	private final ArrayList<Occupant> occupants;

	public OccupantListAdapter(Activity activity, String account, String room) {
		this.activity = activity;
		this.account = account;
		this.room = room;
		occupants = new ArrayList<Occupant>();
	}

	@Override
	public void onChange() {
		occupants.clear();
		occupants.addAll(MUCManager.getInstance().getOccupants(account, room));

		// Eliminamos al propio usuario de la lista a mostrar
		for (Occupant ocu : occupants) {

			if (ocu.getJid().equals(account)) {
				occupants.remove(ocu);
			}
		}

		Collections.sort(occupants);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return occupants.size();
	}

	@Override
	public Object getItem(int position) {
		return occupants.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		if (convertView == null) {
			view = activity.getLayoutInflater().inflate(
					R.layout.occupant_list_item, parent, false);
		} else {
			view = convertView;
		}
		final Occupant occupant = (Occupant) getItem(position);
		// final ImageView avatarView = (ImageView)
		// view.findViewById(R.id.avatar);
		final ImageView affilationView = (ImageView) view
				.findViewById(R.id.affilation);
		final TextView nameView = (TextView) view.findViewById(R.id.name);
		final TextView statusTextView = (TextView) view
				.findViewById(R.id.status);
		final ImageView statusModeView = (ImageView) view
				.findViewById(R.id.status_mode);

		// if (MUCManager.getInstance().getNickname(account, room)
		// .equalsIgnoreCase(occupant.getNickname()))
		// avatarView.setImageDrawable(AvatarManager.getInstance()
		// .getAccountAvatar(account));
		// else
		// avatarView.setImageDrawable(AvatarManager.getInstance()
		// .getOccupantAvatar(room + "/" + occupant.getNickname()));
		affilationView.setImageLevel(occupant.getAffiliation().ordinal());
		nameView.setText(occupant.getNickname());
		int textStyle;
		if (occupant.getRole() == Role.moderator)
			textStyle = R.style.OccupantList_Moderator_Own;
		else if (occupant.getRole() == Role.participant)
			textStyle = R.style.OccupantList_Participant_Own;
		else
			textStyle = R.style.OccupantList_Visitor_Own;
		nameView.setTextAppearance(activity, textStyle);
		statusTextView.setText(activity.getString(occupant.getStatusMode()
				.getStringID()));
		setStatusUser(activity, occupant, statusTextView);
		statusModeView.setImageLevel(occupant.getStatusMode().getStatusLevel());

		return view;
	}

	public void setStatusUser(Activity activity, Occupant ac, TextView tv) {

		// if (text != null && !text.equals("")) {
		// tv.setText(text);
		// } else {
		// tv.setText(activity.getString(ac.getStatusMode().getStringID()));
		// }

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
			case 8:

				tv.setTextColor(activity.getResources().getColor(
						R.color.offline));

				break;

			default:

				tv.setTextColor(activity.getResources().getColor(
						R.color.offline));

				break;
			}
		}
	}
}
