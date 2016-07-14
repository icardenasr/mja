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
package com.xabber.android.ui;

import java.util.Collection;

import org.jivesoftware.smack.util.StringUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xabber.android.data.Application;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.extension.muc.RoomInvite;
import com.xabber.android.data.intent.AccountIntentBuilder;
import com.xabber.android.data.intent.EntityIntentBuilder;
import com.xabber.android.data.message.MessageManager;
import com.xabber.android.data.notification.NotificationManager;
import com.xabber.android.ui.adapter.AccountChooseAdapter;
import com.xabber.android.ui.dialog.ConfirmDialogBuilder;
import com.xabber.android.ui.dialog.ConfirmDialogListener;
import com.xabber.android.ui.dialog.DialogBuilder;
import com.xabber.android.ui.helper.ManagedActivity;

import es.juntadeandalucia.android.im.R;

public class MUCEditor extends ManagedActivity implements View.OnClickListener,
		OnItemSelectedListener, ConfirmDialogListener {

	/**
	 * Action for MUC invitation to be show.
	 * 
	 * Clear action on dialog dismiss.
	 */
	private static final String ACTION_MUC_INVITE = "com.xabber.android.data.MUC_INVITE";

	private static final String SAVED_ACCOUNT = "com.xabber.android.ui.MUCEditor.SAVED_ACCOUNT";
	private static final String SAVED_ROOM = "com.xabber.android.ui.MUCEditor.SAVED_ROOM";

	private static final int DIALOG_MUC_INVITE_ID = 100;

	private String account;
	private String room;

	/**
	 * Last selected account.
	 */
	private int selectedAccount;

	/**
	 * Invite intent.
	 */
	private RoomInvite roomInvite;

	/**
	 * Views.
	 */
	private Spinner accountView;
	private EditText serverView;
	private EditText roomView;
	private EditText nickView;
	private EditText passwordView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing())
			return;

		setContentView(R.layout.muc_editor);

		ImageView imgSetting = (ImageView) findViewById(R.id.iv_options);
		if (imgSetting != null) {
			imgSetting.setVisibility(View.GONE);
		}

		accountView = (Spinner) findViewById(R.id.contact_account);
		serverView = (EditText) findViewById(R.id.muc_server);
		roomView = (EditText) findViewById(R.id.muc_room);
		nickView = (EditText) findViewById(R.id.muc_nick);
		passwordView = (EditText) findViewById(R.id.muc_password);

		ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				MUCEditor.this.finish();

			}
		});

		String[] connection = getResources().getStringArray(
				R.array.account_type_xmpp);
		if (connection != null) {

			String host = connection[connection.length - 1];
			serverView.setText("conference." + host);
			((LinearLayout) findViewById(R.id.ll_muc_server))
					.setVisibility(View.GONE);
			((TextView) findViewById(R.id.tv_muc_server))
					.setVisibility(View.GONE);

		} else {
			((LinearLayout) findViewById(R.id.ll_muc_server))
					.setVisibility(View.VISIBLE);

			((TextView) findViewById(R.id.tv_muc_server))
					.setVisibility(View.VISIBLE);
		}

		((Button) findViewById(R.id.ok)).setOnClickListener(this);
		accountView.setAdapter(new AccountChooseAdapter(this));
		accountView.setOnItemSelectedListener(this);

		Intent intent = getIntent();
		if (savedInstanceState != null) {
			account = savedInstanceState.getString(SAVED_ACCOUNT);
			room = savedInstanceState.getString(SAVED_ROOM);
		} else {
			account = getAccount(intent);
			room = getUser(intent);
			if (room != null) {
				serverView.setText(StringUtils.parseServer(room));
				roomView.setText(StringUtils.parseName(room));
			}
			if (account != null && room != null) {
				MUCManager.getInstance()
						.removeAuthorizationError(account, room);
				nickView.setText(MUCManager.getInstance().getNickname(account,
						room));
				passwordView.setText(MUCManager.getInstance().getPassword(
						account, room));
			}
		}
		if (account == null) {
			Collection<String> accounts = AccountManager.getInstance()
					.getAccounts();
			if (accounts.size() == 1)
				account = accounts.iterator().next();
		}
		if (account != null) {
			for (int position = 0; position < accountView.getCount(); position++)
				if (account.equals(accountView.getItemAtPosition(position))) {
					accountView.setSelection(position);
					break;
				}
		}
		if ("".equals(nickView.getText().toString()))
			nickView.setText(getNickname(((String) accountView
					.getSelectedItem())));
		if (ACTION_MUC_INVITE.equals(intent.getAction())) {
			roomInvite = MUCManager.getInstance().getInvite(account, room);
			if (roomInvite == null) {
				Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
				finish();
				return;
			}
			passwordView.setText(roomInvite.getPassword());
		} else {
			roomInvite = null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		account = (String) accountView.getSelectedItem();
		outState.putString(SAVED_ACCOUNT, account);
		outState.putString(SAVED_ROOM, room);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		selectedAccount = accountView.getSelectedItemPosition();
		if (roomInvite != null) {
			showDialog(DIALOG_MUC_INVITE_ID);
			if (roomView != null) {
				roomView.setInputType(InputType.TYPE_NULL);
			}
		} else {
			if (roomView != null) {
				roomView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				// roomView.setKeyListener(DigitsKeyListener
				// .getInstance("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"));

			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ok:
			actionOk();
			break;
		default:
			break;
		}
	}

	/**
	 * @param account
	 * @return Suggested nickname in the room.
	 */
	private String getNickname(String account) {
		if (account == null)
			return "";
		String nickname = AccountManager.getInstance().getNickName(account);
		String name = StringUtils.parseName(nickname);
		if ("".equals(name))
			return nickname;
		else
			return name;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_MUC_INVITE_ID:
			return new ConfirmDialogBuilder(this, DIALOG_MUC_INVITE_ID, this)
					.setMessage(roomInvite.getConfirmation()).create();
		default:
			return null;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String current = nickView.getText().toString();
		String previous;
		if (selectedAccount == AdapterView.INVALID_POSITION)
			previous = "";
		else
			previous = getNickname((String) accountView.getAdapter().getItem(
					selectedAccount));
		if (current.equals(previous))
			nickView.setText(getNickname((String) accountView.getSelectedItem()));
		selectedAccount = accountView.getSelectedItemPosition();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		selectedAccount = accountView.getSelectedItemPosition();
	}

	@Override
	public void onAccept(DialogBuilder dialogBuilder) {
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_MUC_INVITE_ID:
			MUCManager.getInstance().removeInvite(roomInvite);
			getIntent().setAction(null);
			account = null;
			room = null;

			actionOk();

			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialogBuilder) {
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_MUC_INVITE_ID:
			MUCManager.getInstance().removeInvite(roomInvite);
			finish();
			break;
		}
	}

	@Override
	public void onCancel(DialogBuilder dialogBuilder) {
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_MUC_INVITE_ID:
			finish();
			break;
		}
	}

	public static Intent createIntent(Context context) {
		return MUCEditor.createIntent(context, null, null);
	}

	public static Intent createIntent(Context context, String account,
			String room) {
		return new EntityIntentBuilder(context, MUCEditor.class)
				.setAccount(account).setUser(room).build();
	}

	public static Intent createInviteIntent(Context context, String account,
			String user) {
		Intent intent = createIntent(context, account, user);
		intent.setAction(ACTION_MUC_INVITE);
		return intent;
	}

	private static String getAccount(Intent intent) {
		return AccountIntentBuilder.getAccount(intent);
	}

	private static String getUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

	private void actionOk() {

		String account = (String) accountView.getSelectedItem();
		if (account == null) {
			Toast.makeText(this, getString(R.string.EMPTY_ACCOUNT),
					Toast.LENGTH_LONG).show();
			return;
		}
		String server = serverView.getText().toString();
		if ("".equals(server)) {
			Toast.makeText(this, getString(R.string.EMPTY_SERVER_NAME),
					Toast.LENGTH_LONG).show();
			return;
		}
		String room = roomView.getText().toString();
		if ("".equals(room)) {
			Toast.makeText(this, getString(R.string.EMPTY_ROOM_NAME),
					Toast.LENGTH_LONG).show();
			return;
		}

		if (!supportedRoomName(room)) {
			Toast.makeText(this, getString(R.string.NOT_SUPPORTED_ROOM_NAME),
					Toast.LENGTH_LONG).show();
			return;
		}

		String nick = nickView.getText().toString();
		if ("".equals(nick)) {
			Toast.makeText(this, getString(R.string.EMPTY_NICK_NAME),
					Toast.LENGTH_LONG).show();
			return;
		}

		String password = passwordView.getText().toString();
		boolean join = ((CheckBox) findViewById(R.id.muc_join)).isChecked();
		room = room.toLowerCase();
		room = room + "@" + server;
		if (this.account != null && this.room != null)
			if (!account.equals(this.account) || !room.equals(this.room)) {
				MUCManager.getInstance().removeRoom(this.account, this.room);
				MessageManager.getInstance().closeChat(this.account, this.room);
				NotificationManager.getInstance().removeMessageNotification(
						this.account, this.room);
			}
		MUCManager.getInstance()
				.createRoom(account, room, nick, password, join);
		finish();

	}

	private Boolean supportedRoomName(String texto) {

		if (texto != null && !texto.equalsIgnoreCase("")) {
			for (int i = 0; i < texto.length(); i++) {

				CharSequence letra = String.valueOf(texto.charAt(i));
				if (!"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
						.contains(letra)) {

					return false;
				}

			}
		}

		return true;
	}
}
