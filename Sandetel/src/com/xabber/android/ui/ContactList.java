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

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xabber.android.data.ActivityManager;
import com.xabber.android.data.Application;
import com.xabber.android.data.ContactManager;
import com.xabber.android.data.DatabaseManager;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.account.AccountItem;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.IActionAfterLoad;
import com.xabber.android.data.account.OnAccountChangedListener;
import com.xabber.android.data.account.StatusMode;
import com.xabber.android.data.connection.ConnectionManager;
import com.xabber.android.data.connection.ConnectionState;
import com.xabber.android.data.entity.BaseEntity;
import com.xabber.android.data.extension.avatar.AvatarManager;
import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.extension.muc.Occupant;
import com.xabber.android.data.extension.vcard.VCardManager;
import com.xabber.android.data.intent.EntityIntentBuilder;
import com.xabber.android.data.message.AbstractChat;
import com.xabber.android.data.message.MessageItem;
import com.xabber.android.data.message.MessageManager;
import com.xabber.android.data.message.OnChatChangedListener;
import com.xabber.android.data.notification.NotificationManager;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.GroupManager;
import com.xabber.android.data.roster.OnContactChangedListener;
import com.xabber.android.data.roster.PresenceManager;
import com.xabber.android.data.roster.RosterContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.data.roster.ShowOfflineMode;
import com.xabber.android.service.dto.UsuarioDTO;
import com.xabber.android.service.ws_client.service_ws.UsuarioWS;
import com.xabber.android.ui.adapter.AccountConfiguration;
import com.xabber.android.ui.adapter.AccountToggleAdapter;
import com.xabber.android.ui.adapter.ContactListAdapter;
import com.xabber.android.ui.adapter.GroupConfiguration;
import com.xabber.android.ui.dialog.AccountChooseDialogBuilder;
import com.xabber.android.ui.dialog.ConfirmDialogBuilder;
import com.xabber.android.ui.dialog.ConfirmDialogListener;
import com.xabber.android.ui.dialog.DialogBuilder;
import com.xabber.android.ui.dialog.GroupRenameDialogBuilder;
import com.xabber.android.ui.helper.ManagedListActivity;
import com.xabber.android.ui.own.AcercadeAct;
import com.xabber.android.ui.own.LoginAct;
import com.xabber.android.utils.ddbb.DDBBManager;
import com.xabber.android.utils.dialogs.DialogGeneric;
import com.xabber.android.utils.dialogs.IDialogAction;
import com.xabber.android.utils.notification.UtilNotification;
import com.xabber.android.utils.preferences.UtilPreferences;
import com.xabber.androiddev.async.AsyncTask;
import com.xabber.xmpp.address.Jid;
import com.xabber.xmpp.muc.Role;
import com.xabber.xmpp.uri.XMPPUri;

import es.juntadeandalucia.android.im.R;

/**
 * Main application activity.
 * 
 * @author alexander.ivanov
 * 
 */
public class ContactList extends ManagedListActivity implements
		OnContactChangedListener, OnAccountChangedListener,
		OnChatChangedListener, View.OnClickListener, ConfirmDialogListener,
		OnItemClickListener, OnLongClickListener {

	/**
	 * Pick Up File to change Avatar
	 */
	public static final int FILE_SELECT_CODE = 0;

	private EditText etSearch;

	private Boolean connected = false;
	private ImageView imgSettings;
	// private List<String> phones;

	// private String nameId = "ChatViewer";

	private MessageItem messageItem;

	/**
	 * Select contact to be invited to the room was requested.
	 */
	private static final String ACTION_ROOM_INVITE = "com.xabber.android.ui.ContactList.ACTION_ROOM_INVITE";
	private static final String ACTION_FORWARD_MESSAGE = "com.xabber.android.ui.ContactList.ACTION_FORDWARD_MESSAGE";
	private static final String ACTION_IMPORT_DDBB = "com.xabber.android.ui.ContactList.ACTION_IMPORT_DDBB";

	public static final String EXTRA_FORWARD_MESSAGE = "EXTRA_FORDWARD_MESSAGE";

	private static final long CLOSE_ACTIVITY_AFTER_DELAY = 300;

	private static final String SAVED_ACTION = "com.xabber.android.ui.ContactList.SAVED_ACTION";
	private static final String SAVED_ACTION_WITH_ACCOUNT = "com.xabber.android.ui.ContactList.SAVED_ACTION_WITH_ACCOUNT";
	private static final String SAVED_ACTION_WITH_GROUP = "com.xabber.android.ui.ContactList.SAVED_ACTION_WITH_GROUP";
	private static final String SAVED_ACTION_WITH_USER = "com.xabber.android.ui.ContactList.SAVED_ACTION_WITH_USER";
	private static final String SAVED_SEND_TEXT = "com.xabber.android.ui.ContactList.SAVED_SEND_TEXT";
	private static final String SAVED_OPEN_DIALOG_USER = "com.xabber.android.ui.ContactList.SAVED_OPEN_DIALOG_USER";
	private static final String SAVED_OPEN_DIALOG_TEXT = "com.xabber.android.ui.ContactList.SAVED_OPEN_DIALOG_TEXT";

	// private static final int OPTION_MENU_ADD_CONTACT_ID = 0x02;
	// private static final int OPTION_MENU_STATUS_EDITOR_ID = 0x04;
	private static final int OPTION_MENU_PREFERENCE_EDITOR_ID = 0x05;
	private static final int OPTION_MENU_CHAT_LIST_ID = 0x06;
	private static final int OPTION_MENU_JOIN_ROOM_ID = 0x07;
	private static final int OPTION_MENU_EXIT_ID = 0x08;
	private static final int OPTION_MENU_SEARCH_ID = 0x0A;
	private static final int OPTION_MENU_CLOSE_CHATS_ID = 0x0B;

	private static final int CONTEXT_MENU_VIEW_CHAT_ID = 0x12;
	private static final int CONTEXT_MENU_EDIT_CONTACT_ID = 0x13;
	private static final int CONTEXT_MENU_DELETE_CONTACT_ID = 0x14;
	private static final int CONTEXT_MENU_CLOSE_CHAT_ID = 0x15;
	private static final int CONTEXT_MENU_REQUEST_SUBSCRIPTION_ID = 0x16;
	private static final int CONTEXT_MENU_ACCEPT_SUBSCRIPTION_ID = 0x17;
	private static final int CONTEXT_MENU_DISCARD_SUBSCRIPTION_ID = 0x18;
	private static final int CONTEXT_MENU_LEAVE_ROOM_ID = 0x19;
	private static final int CONTEXT_MENU_JOIN_ROOM_ID = 0x1A;
	private static final int CONTEXT_MENU_EDIT_ROOM_ID = 0x1B;
	private static final int CONTEXT_MENU_VIEW_CONTACT_ID = 0x1C;
	private static final int CONTEXT_MENU_EMAIL = 0x1D;
	private static final int CONTEXT_MENU_PHONE = 0x1E;

	private static final int CONTEXT_MENU_GROUP_RENAME_ID = 0x31;
	private static final int CONTEXT_MENU_GROUP_DELETE_ID = 0x32;

	private static final int CONTEXT_MENU_ACCOUNT_EDITOR_ID = 0x33;
	private static final int CONTEXT_MENU_ACCOUNT_STATUS_ID = 0x34;
	private static final int CONTEXT_MENU_ACCOUNT_ADD_CONTACT_ID = 0x35;
	private static final int CONTEXT_MENU_ACCOUNT_RECONNECT_ID = 0x39;
	private static final int CONTEXT_MENU_ACCOUNT_VCARD_ID = 0x3A;

	private static final int CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID = 0x40;
	private static final int CONTEXT_MENU_SHOW_OFFLINE_ALWAYS_ID = 0x41;
	private static final int CONTEXT_MENU_SHOW_OFFLINE_NORMAL_ID = 0x42;
	private static final int CONTEXT_MENU_SHOW_OFFLINE_NEVER_ID = 0x43;

	private static final int DIALOG_DELETE_CONTACT_ID = 0x50;
	private static final int DIALOG_DELETE_GROUP_ID = 0x51;
	private static final int DIALOG_RENAME_GROUP_ID = 0x52;
	private static final int DIALOG_START_AT_BOOT_ID = 0x53;
	private static final int DIALOG_CONTACT_INTEGRATION_ID = 0x54;
	private static final int DIALOG_OPEN_WITH_ACCOUNT_ID = 0x55;
	private static final int DIALOG_CLOSE_APPLICATION_ID = 0x57;

	/**
	 * Adapter for contact list.
	 */
	private ContactListAdapter contactListAdapter;

	/**
	 * Adapter for account list.
	 */
	private AccountToggleAdapter accountToggleAdapter;

	/**
	 * Current action.
	 */
	private String action;

	/**
	 * Dialog related values.
	 */
	private String actionWithAccount;
	private String actionWithGroup;
	private String actionWithUser;
	private String sendText;
	private String openDialogUser;
	private String openDialogText;

	/**
	 * Title view.
	 */
	private View titleView;
	private TextView tvTitulo;
	private ImageView ivStatus;
	private TextView tvStatus;

	/**
	 * instance of contact manager
	 */
	private ContactManager contactManager;

	private Bundle savedInstanceState;

	@SuppressWarnings({ "deprecation", "null" })
	@Override
	public void onCreate(Bundle savedInstanceState) {

		this.savedInstanceState = savedInstanceState;

		if (Intent.ACTION_VIEW.equals(getIntent().getAction())
				|| Intent.ACTION_SEND.equals(getIntent().getAction())
				|| Intent.ACTION_SENDTO.equals(getIntent().getAction())
				|| Intent.ACTION_CREATE_SHORTCUT
						.equals(getIntent().getAction()))
			ActivityManager.getInstance().startNewTask(this);
		super.onCreate(savedInstanceState);
		if (isFinishing())
			return;

		contactManager = new ContactManager(ContactList.this);

		setContentView(R.layout.contact_list);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String uri = extras.getString(LoginAct.EXTRA_URI_IMPORT);
			if (uri != null) {
				Uri uriImport = Uri.parse(uri);
				DDBBManager.getInstance(ContactList.this)
						.startImport(uriImport);
			}
		}

		// if (findViewById(R.id.tv_status_line) != null) {
		// findViewById(R.id.tv_status_line).setVisibility(View.VISIBLE);
		// }

		etSearch = ((EditText) findViewById(R.id.et_search));

		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				searchAction(s.toString());

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		findViewById(R.id.img_search).setOnClickListener(this);

		titleView = findViewById(android.R.id.title);

		ListView listView = getListView();
		listView.setOnItemClickListener(this);
		listView.setItemsCanFocus(true);

		registerForContextMenu(listView);
		contactListAdapter = new ContactListAdapter(this);
		setListAdapter(contactListAdapter);
		accountToggleAdapter = new AccountToggleAdapter(this, this,
				(LinearLayout) findViewById(R.id.account_list));

		View commonStatusText = null;
		// if (findViewById(R.id.tv_status_line) != null) {
		//
		// commonStatusText = (TextView) findViewById(R.id.tv_status_line);
		//
		// }

		View commonStatusMode = findViewById(R.id.common_status_mode);
		TypedArray typedArray = obtainStyledAttributes(R.styleable.ContactList);
		ColorStateList textColorPrimary = typedArray
				.getColorStateList(R.styleable.ContactList_textColorPrimaryNoSelected);
		Drawable titleMainBackground = typedArray
				.getDrawable(R.styleable.ContactList_titleMainBackground);
		typedArray.recycle();

		TextView tv = null;
		if (commonStatusText != null) {
			tv = ((TextView) commonStatusText);
		}
		if (tv != null && textColorPrimary != null) {
			tv.setTextColor(textColorPrimary);
		}
		titleView.setBackgroundDrawable(titleMainBackground);

		if (commonStatusText != null) {
			commonStatusText.setOnLongClickListener(this);
		}
		commonStatusMode.setOnClickListener(this);
		if (commonStatusText != null) {
			commonStatusText.setOnClickListener(this);
		}
		titleView.setOnClickListener(this);
		findViewById(R.id.button).setOnClickListener(this);
		findViewById(R.id.back_button).setOnClickListener(this);

		if (savedInstanceState != null) {
			actionWithAccount = savedInstanceState
					.getString(SAVED_ACTION_WITH_ACCOUNT);
			actionWithGroup = savedInstanceState
					.getString(SAVED_ACTION_WITH_GROUP);
			actionWithUser = savedInstanceState
					.getString(SAVED_ACTION_WITH_USER);
			sendText = savedInstanceState.getString(SAVED_SEND_TEXT);
			openDialogUser = savedInstanceState
					.getString(SAVED_OPEN_DIALOG_USER);
			openDialogText = savedInstanceState
					.getString(SAVED_OPEN_DIALOG_TEXT);
			action = savedInstanceState.getString(SAVED_ACTION);
		} else {
			actionWithAccount = null;
			actionWithGroup = null;
			actionWithUser = null;
			sendText = null;
			openDialogUser = null;
			openDialogText = null;
			action = getIntent().getAction();
		}

		getIntent().setAction(null);

		imgSettings = (ImageView) findViewById(R.id.iv_options);

		if (imgSettings != null) {
			imgSettings.setOnClickListener(this);
		}

		tvTitulo = (TextView) findViewById(R.id.tv_titulo);
		ivStatus = (ImageView) findViewById(R.id.iv_status);
		tvStatus = (TextView) findViewById(R.id.tv_status);

		UsuarioDTO user = (UsuarioDTO) UtilPreferences.getPreferences(this,
				UsuarioWS.KEY_USUARIO, UsuarioDTO.class);
		if (user != null || user.getNick() != null) {
			UtilNotification.register(ContactList.this, user.getNick());

			tvTitulo.setText(user.getNick().split("@")[0]);
		}

	}

	/**
	 * Cambia a offline el estado del usuario con el que se chatea en la barra
	 * superior de navegacion
	 */
	public void setOfflineState(String text, Drawable d, TextView tvDesc,
			ImageView ivState) {

		if (tvDesc != null && ivState != null) {
			tvDesc.setText(text);
			tvDesc.setTextColor(this.getResources().getColor(R.color.offline));

			ivState.setImageDrawable(d);
		}
	}

	/**
	 * Cambia a online el estado del usuario con el que se chatea en la barra
	 * superior de navegacion
	 */
	public void setOnlineState(String text, Drawable d, TextView tvDesc,
			ImageView ivState) {

		if (tvDesc != null && ivState != null) {
			tvDesc.setText(text);
			tvDesc.setTextColor(this.getResources().getColor(R.color.online));

			ivState.setImageDrawable(d);
		}

	}

	/**
	 * Cambia a away el estado del usuario con el que se chatea en la barra
	 * superior de navegacion
	 */
	public void setAwayState(String text, Drawable d, TextView tvDesc,
			ImageView ivState) {

		if (tvDesc != null && ivState != null) {
			tvDesc.setText(text);
			tvDesc.setTextColor(this.getResources().getColor(R.color.away));

			ivState.setImageDrawable(d);
		}

	}

	public void setStatusUser(TextView tvDesc, ImageView ivState,
			int statusLevel, String state) {

		String statusOwn = SettingsManager.statusText();
		StatusMode statusMode = SettingsManager.statusMode();
		String finalState = this.getString(statusMode.getStringID());
		if (state != null && !state.equals("")) {

			String connectionStrig = getString(R.string.account_state_connected);
			if (!connectionStrig.equals(state)) {

				connected = false;

				finalState = state;
				statusLevel = 9;
			} else {
				connected = true;

			}
		}

		if (statusOwn != null && !statusOwn.equals("") && finalState != null) {
			finalState = finalState + " (" + statusOwn + ")";
		}

		switch (statusLevel) {

		case 0:

			setOnlineState(
					finalState,
					this.getResources().getDrawable(
							R.drawable.chat_bola_verde_listo), tvDesc, ivState);

			break;

		case 1:

			setOnlineState(
					finalState,
					this.getResources().getDrawable(R.drawable.chat_bola_verde),
					tvDesc, ivState);

			break;

		case 2:

			setAwayState(
					finalState,
					this.getResources().getDrawable(
							R.drawable.chat_bola_ausente), tvDesc, ivState);

			break;

		case 3:

			setAwayState(
					finalState,
					this.getResources().getDrawable(
							R.drawable.chat_bola_ausente_extendido), tvDesc,
					ivState);

			break;
		case 8:

			setOfflineState(finalState,
					this.getResources().getDrawable(R.drawable.chat_bola_roja),
					tvDesc, ivState);

			break;

		default:

			setOfflineState(finalState,
					this.getResources().getDrawable(R.drawable.chat_bola_roja),
					tvDesc, ivState);

			break;

		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		action = getIntent().getAction();
		getIntent().setAction(null);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (outState != null)
			super.onSaveInstanceState(outState);
		outState.putString(SAVED_ACTION, action);
		outState.putString(SAVED_ACTION_WITH_ACCOUNT, actionWithAccount);
		outState.putString(SAVED_ACTION_WITH_GROUP, actionWithGroup);
		outState.putString(SAVED_ACTION_WITH_USER, actionWithUser);
		outState.putString(SAVED_SEND_TEXT, sendText);
		outState.putString(SAVED_OPEN_DIALOG_USER, openDialogUser);
		outState.putString(SAVED_OPEN_DIALOG_TEXT, openDialogText);
	}

	/**
	 * Open chat with specified contact.
	 * 
	 * Show dialog to choose account if necessary.
	 * 
	 * @param user
	 * @param text
	 *            can be <code>null</code>.
	 */
	@SuppressWarnings("deprecation")
	private void openChat(String user, String text) {
		String bareAddress = Jid.getBareAddress(user);
		ArrayList<BaseEntity> entities = new ArrayList<BaseEntity>();
		for (AbstractChat check : MessageManager.getInstance().getChats())
			if (check.isActive() && check.getUser().equals(bareAddress))
				entities.add(check);
		if (entities.size() == 1) {
			openChat(entities.get(0), text);
			return;
		}
		entities.clear();
		for (RosterContact check : RosterManager.getInstance().getContacts())
			if (check.isEnabled() && check.getUser().equals(bareAddress))
				entities.add(check);
		if (entities.size() == 1) {
			openChat(entities.get(0), text);
			return;
		}
		Collection<String> accounts = AccountManager.getInstance()
				.getAccounts();
		if (accounts.isEmpty())
			return;
		if (accounts.size() == 1) {
			openChat(new BaseEntity(accounts.iterator().next(), bareAddress),
					text);
			return;
		}
		openDialogUser = bareAddress;
		openDialogText = text;
		showDialog(DIALOG_OPEN_WITH_ACCOUNT_ID);
	}

	/**
	 * Open chat with specified contact and enter text to be sent.
	 * 
	 * @param baseEntity
	 * @param text
	 *            can be <code>null</code>.
	 */
	private void openChat(BaseEntity baseEntity, String text) {
		if (text == null)

			startActivity(ChatViewer.createSendIntent(this,
					baseEntity.getAccount(), baseEntity.getUser(), null));
		else
			startActivity(ChatViewer.createSendIntent(this,
					baseEntity.getAccount(), baseEntity.getUser(), text));
		finish();

		// Intent i = ChatViewer.createSendIntent(this, baseEntity.getAccount(),
		// baseEntity.getUser(), text);
		//
		// View view = ChatGroup.chatGroup
		// .getLocalActivityManager()
		// .startActivity(nameId,
		// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
		// .getDecorView();
		//
		// // Again, replace the view
		// ChatGroup.chatGroup.replaceView(view);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		updateStatusBar(null);

		rebuildAccountToggler();

		AccountManager.getInstance().getAccounts();

		Application.getInstance().addUIListener(OnAccountChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnContactChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnChatChangedListener.class,
				this);
		contactListAdapter.onChange();

		if (ContactList.ACTION_ROOM_INVITE.equals(action)
				|| Intent.ACTION_SEND.equals(action)
				|| Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
			if (Intent.ACTION_SEND.equals(action))
				sendText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
			Toast.makeText(this, getString(R.string.select_contact),
					Toast.LENGTH_LONG).show();
		} else if (Intent.ACTION_VIEW.equals(action)) {
			action = null;
			Uri data = getIntent().getData();
			if (data != null && "xmpp".equals(data.getScheme())) {
				XMPPUri xmppUri;
				try {
					xmppUri = XMPPUri.parse(data);
				} catch (IllegalArgumentException e) {
					xmppUri = null;
				}
				if (xmppUri != null && "message".equals(xmppUri.getQueryType())) {
					ArrayList<String> texts = xmppUri.getValues("body");
					String text = null;
					if (texts != null && !texts.isEmpty())
						text = texts.get(0);
					openChat(xmppUri.getPath(), text);
				}
			}
		} else if (Intent.ACTION_SENDTO.equals(action)) {
			action = null;
			Uri data = getIntent().getData();
			if (data != null) {
				String path = data.getPath();
				if (path != null && path.startsWith("/"))
					openChat(path.substring(1), null);
			}
		} else if (ContactList.ACTION_FORWARD_MESSAGE.equals(action)) {
			messageItem = (MessageItem) getIntent().getExtras()
					.getSerializable(EXTRA_FORWARD_MESSAGE);

			Toast.makeText(this, R.string.select_user_to_send,
					Toast.LENGTH_SHORT).show();

		}

		// else if (ContactList.ACTION_IMPORT_DDBB.equals(action)) {
		//
		// actionLogOut();
		//
		// }

		if (Application.getInstance().doNotify()) {
			if (SettingsManager.bootCount() > 2
					&& !SettingsManager.connectionStartAtBoot()
					&& !SettingsManager.startAtBootSuggested())
				// showDialog(DIALOG_START_AT_BOOT_ID);
				if (!SettingsManager.contactIntegrationSuggested()
						&& Application.getInstance().isContactsSupported()) {
					if (AccountManager.getInstance().getAllAccounts().isEmpty())
						SettingsManager.setContactIntegrationSuggested();
					else
						showDialog(DIALOG_CONTACT_INTEGRATION_ID);
				}
		}

		if (imgSettings != null) {
			if (ContactList.ACTION_ROOM_INVITE.equals(action)
					|| Intent.ACTION_SEND.equals(action)
					|| Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
				imgSettings.setVisibility(View.INVISIBLE);
			} else {
				imgSettings.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterListeners();
	}

	private void unregisterListeners() {
		Application.getInstance().removeUIListener(
				OnAccountChangedListener.class, this);
		Application.getInstance().removeUIListener(
				OnContactChangedListener.class, this);
		Application.getInstance().removeUIListener(OnChatChangedListener.class,
				this);
		contactListAdapter.removeRefreshRequests();
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// super.onCreateOptionsMenu(menu);
	// menu.add(0, OPTION_MENU_ADD_CONTACT_ID, 0,
	// getText(R.string.contact_add)).setIcon(
	// R.drawable.ic_menu_invite);
	// menu.add(0, OPTION_MENU_CLOSE_CHATS_ID, 0,
	// getText(R.string.close_chats)).setIcon(
	// R.drawable.ic_menu_end_conversation);
	// menu.add(0, OPTION_MENU_PREFERENCE_EDITOR_ID, 0,
	// getResources().getText(R.string.preference_editor)).setIcon(
	// android.R.drawable.ic_menu_preferences);
	// menu.add(0, OPTION_MENU_STATUS_EDITOR_ID, 0,
	// getText(R.string.status_editor)).setIcon(
	// R.drawable.ic_menu_notifications);
	// menu.add(0, OPTION_MENU_EXIT_ID, 0, getText(R.string.exit)).setIcon(
	// android.R.drawable.ic_menu_close_clear_cancel);
	// menu.add(0, OPTION_MENU_JOIN_ROOM_ID, 0, getText(R.string.muc_add));
	// menu.add(0, OPTION_MENU_SEARCH_ID, 0,
	// getText(android.R.string.search_go));
	// menu.add(0, OPTION_MENU_CHAT_LIST_ID, 0, getText(R.string.chat_list))
	// .setIcon(R.drawable.ic_menu_friendslist);
	// return true;
	// }

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		IDialogAction iDialogAction;
		HashMap<String, Serializable> params;
		DialogGeneric dialog;
		switch (item.getItemId()) {

		// case R.id.action_see_perfil:
		// Intent i = new Intent(this, SettingEditarAct.class);
		// i.putExtra(Constantes.MODO_EDITAR, false);
		// this.startActivity(i);
		// break;
		// case R.id.action_edit_perfil:
		// Intent i2 = new Intent(this, SettingEditarAct.class);
		// i2.putExtra(Constantes.MODO_EDITAR, true);
		// this.startActivity(i2);
		// break;
		// case R.id.action_edit_pass:
		// Intent i3 = new Intent(this, PasswordEditarAct.class);
		// this.startActivity(i3);
		// break;
		case R.id.action_about_ne:

			Intent intents = new Intent(this, AcercadeAct.class);
			startActivity(intents);

			break;

		case R.id.action_log_out:

			params = new HashMap<String, Serializable>();
			params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
					getString(R.string.log_out));
			params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
					getString(R.string.sure_log_out));
			params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
					getString(R.string.aceptar));
			params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
					getString(R.string.cancelar));

			iDialogAction = new IDialogAction() {

				@Override
				public void onPositiveButton() {
					actionLogOut();
				}

				@Override
				public void onNegativeButton() {

				}
			};
			params.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
					iDialogAction);

			dialog = new DialogGeneric(ContactList.this, params);

			dialog.createDialog();
			dialog.showDialog();

			// finish();
			// startActivity(AccountAdd.createIntent(ContactList.this));

			break;

		case R.id.action_exit:

			params = new HashMap<String, Serializable>();
			params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
					getString(R.string.exit));
			params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
					getString(R.string.sure_exit));
			params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
					getString(R.string.aceptar));
			params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
					getString(R.string.cancelar));

			iDialogAction = new IDialogAction() {

				@Override
				public void onPositiveButton() {
					actionExit();
				}

				@Override
				public void onNegativeButton() {

				}
			};
			params.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
					iDialogAction);

			dialog = new DialogGeneric(ContactList.this, params);

			dialog.createDialog();
			dialog.showDialog();

			break;

		case R.id.action_add:
			startActivity(ContactAdd.createIntent(this));

			return true;

		case R.id.action_list_contacts:

			startActivity(PreferenceEditor.createIntent(this));

			break;

		/*case R.id.action_avatar_change:

			showFileChooser();

			break;*/
		case R.id.action_state:
			startActivity(StatusEditor.createIntent(this));
			return true;

		case R.id.action_close_chats:
			params = new HashMap<String, Serializable>();
			params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
					getString(R.string.close_chats));
			params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
					getString(R.string.sure_close_chats));
			params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
					getString(R.string.aceptar));
			params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
					getString(R.string.cancelar));

			iDialogAction = new IDialogAction() {

				@Override
				public void onPositiveButton() {
					actionCloseChats();
				}

				@Override
				public void onNegativeButton() {

				}
			};
			params.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
					iDialogAction);

			dialog = new DialogGeneric(ContactList.this, params);

			dialog.createDialog();
			dialog.showDialog();

			return true;

		case R.id.action_export_ddbb:

			DatabaseManager.getInstance().actionExportDatabase(this);
			// try {
			// DatabaseManager.getInstance().importDatabase(this,
			// "/sdcard/Sandetel/MensajeriaDDBB_1415260325172.mja");
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			return true;

		case R.id.action_join_conf:
			startActivity(MUCEditor.createIntent(this));
			return true;
			// case R.id.action_create_group:

			// return true;
		case OPTION_MENU_PREFERENCE_EDITOR_ID:
			startActivity(PreferenceEditor.createIntent(this));
			return true;
		case OPTION_MENU_CHAT_LIST_ID:
			startActivity(ChatList.createIntent(this));
			return true;
		case OPTION_MENU_JOIN_ROOM_ID:
			startActivity(MUCEditor.createIntent(this));
			return true;
		case OPTION_MENU_EXIT_ID:
			// Application.getInstance().requestToClose();
			// showDialog(DIALOG_CLOSE_APPLICATION_ID);
			// unregisterListeners();
			// new Handler().postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// // Close activity if application was not killed yet.
			// finish();
			//
			// }
			// }, CLOSE_ACTIVITY_AFTER_DELAY);
			return true;
		case OPTION_MENU_SEARCH_ID:
			search();
			return true;
		case OPTION_MENU_CLOSE_CHATS_ID:
			for (AbstractChat chat : MessageManager.getInstance()
					.getActiveChats()) {
				MessageManager.getInstance().closeChat(chat.getAccount(),
						chat.getUser());
				NotificationManager.getInstance().removeMessageNotification(
						chat.getAccount(), chat.getUser());
			}
			contactListAdapter.onChange();
			return true;
		}
		return false;
	}

	public void actionLogOut() {

		AccountManager.getInstance().setStatus(StatusMode.unavailable, "");

		AccountManager.getInstance().clearSavedStatuses();

		showDialog(DIALOG_CLOSE_APPLICATION_ID);

	}

	private void actionExit() {
		Application.getInstance().requestToClose();
		showDialog(DIALOG_CLOSE_APPLICATION_ID);
		unregisterListeners();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// Close activity if application was not killed yet.

				finish();
			}
		}, CLOSE_ACTIVITY_AFTER_DELAY);
	}

	private void actionCloseChat() {
		MessageManager.getInstance().closeChat(actionWithAccount,
				actionWithUser);
		NotificationManager.getInstance().removeMessageNotification(
				actionWithAccount, actionWithUser);
		contactListAdapter.onChange();

	}

	private void actionCloseChats() {

		for (AbstractChat chat : MessageManager.getInstance().getActiveChats()) {
			MessageManager.getInstance().closeChat(chat.getAccount(),
					chat.getUser());
			NotificationManager.getInstance().removeMessageNotification(
					chat.getAccount(), chat.getUser());
		}
		contactListAdapter.onChange();

	}

	public void exit() {

		// Application.getInstance().requestToCloseOwn();

		unregisterListeners();

		UtilPreferences.removePreference(this, UsuarioWS.KEY_USUARIO);
		UtilPreferences.setPreferences(this, UsuarioWS.KEY_USUARIO, null);

		// final String account = (String) UtilPreferences.getPreferences(this,
		// Constantes.ACCOUNT, String.class);

		AccountManager.getInstance().setiActionAfterLoad(
				new IActionAfterLoad() {

					@Override
					public void actionAlreadyLoad() {
						actionLogOutAfertLoad();
					}
				});
		AccountManager.getInstance().onLoad();

	}

	private void actionLogOutAfertLoad() {
		Collection<String> cuentas = AccountManager.getInstance().getAccounts();
		List<String> accountsToSetOff = new ArrayList<String>();
		if (cuentas != null && !cuentas.isEmpty()) {
			for (String cuenta : cuentas) {
				// AccountItem accountItem = AccountManager.getInstance()
				// .getAccount(cuenta);
				accountsToSetOff.add(cuenta);
			}
		}

		for (String account : accountsToSetOff) {
			// AccountManager.getInstance().removeAccount(account);
			if (account != null) {
				AccountManager.getInstance().setEnable(account, false, false);
			}
		}

		finish();

		Intent intent = new Intent(this, LoginAct.class);

		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		if (view == getListView()) {
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			BaseEntity baseEntity = (BaseEntity) getListView()
					.getItemAtPosition(info.position);
			if (baseEntity == null)
				// Account toggler

				return;
			if (baseEntity instanceof AbstractContact) {
				actionWithAccount = baseEntity.getAccount();
				actionWithGroup = null;
				actionWithUser = baseEntity.getUser();
				AbstractContact abstractContact = (AbstractContact) baseEntity;
				menu.setHeaderTitle(R.string.opciones);

				// menu.add(0, CONTEXT_MENU_VIEW_CHAT_ID, 0, getResources()
				// .getText(R.string.chat_viewer));

				// SALA DE CHAT
				if (MUCManager.getInstance().hasRoom(actionWithAccount,
						actionWithUser)) {

					// if (!MUCManager.getInstance().inUse(actionWithAccount,
					// actionWithUser))
					// menu.add(0, CONTEXT_MENU_EDIT_ROOM_ID, 0,
					// getResources().getText(R.string.muc_edit));

					List<Occupant> ocupants = new ArrayList<Occupant>();
					ocupants.addAll(MUCManager.getInstance().getOccupants(
							actionWithAccount, actionWithUser));
					Boolean isModerator = false;
					for (Occupant occupant : ocupants) {

						if (occupant.getJid().equals(actionWithAccount)
								&& occupant.getRole() == Role.moderator) {

							menu.add(
									0,
									CONTEXT_MENU_DELETE_CONTACT_ID,
									0,
									getResources().getText(
											R.string.muc_delete_owner));
							isModerator = true;
							break;

						}

					}
					if (!isModerator) {

						menu.add(0, CONTEXT_MENU_DELETE_CONTACT_ID, 0,
								getResources().getText(R.string.muc_delete));

					}

					if (MUCManager.getInstance().isDisabled(actionWithAccount,
							actionWithUser))
						menu.add(0, CONTEXT_MENU_JOIN_ROOM_ID, 0,
								getResources().getText(R.string.muc_join));
					else
						menu.add(0, CONTEXT_MENU_LEAVE_ROOM_ID, 0,
								getResources().getText(R.string.muc_leave));
				} else {

					// CONTACTO

					// menu.add(0, CONTEXT_MENU_VIEW_CONTACT_ID, 0,
					// getResources()
					// .getText(R.string.contact_viewer));
					menu.add(0, CONTEXT_MENU_EDIT_CONTACT_ID, 0, getResources()
							.getText(R.string.contact_editor));

					menu.add(0, CONTEXT_MENU_DELETE_CONTACT_ID, 0,
							getResources().getText(R.string.contact_delete));
					if (MessageManager.getInstance().hasActiveChat(
							actionWithAccount, actionWithUser)) {
						menu.add(0, CONTEXT_MENU_CLOSE_CHAT_ID, 0,
								getResources().getText(R.string.close_chat));
					}
					if (abstractContact.getStatusMode() == StatusMode.unsubscribed) {
						menu.add(0, CONTEXT_MENU_REQUEST_SUBSCRIPTION_ID, 0,
								getText(R.string.request_subscription));
					}

					List<String> phones = contactManager
							.getPhonesToPref(abstractContact.getUser());
					if (phones != null && phones.size() > 0) {

						if (phones.size() > 1) {
							menu.add(0, CONTEXT_MENU_PHONE, 0, getResources()
									.getText(R.string.contact_see_phones));
						} else {
							menu.add(0, CONTEXT_MENU_PHONE, 0, getResources()
									.getText(R.string.contact_call_phone));
						}
					}

					menu.add(0, CONTEXT_MENU_EMAIL, 0,
							getResources().getText(R.string.contact_send_email));

				}
				if (PresenceManager.getInstance().hasSubscriptionRequest(
						actionWithAccount, actionWithUser)) {
					menu.add(0, CONTEXT_MENU_ACCEPT_SUBSCRIPTION_ID, 0,
							getResources()
									.getText(R.string.accept_subscription));
					menu.add(0, CONTEXT_MENU_DISCARD_SUBSCRIPTION_ID, 0,
							getText(R.string.discard_subscription));
				}

				return;
			} else if (baseEntity instanceof GroupConfiguration) {
				// Group or account in contact list
				actionWithAccount = baseEntity.getAccount();
				actionWithGroup = baseEntity.getUser();
				actionWithUser = null;

				if (baseEntity instanceof AccountConfiguration) {
					actionWithGroup = null;
				} else {
					// Group
					menu.setHeaderTitle(GroupManager.getInstance()
							.getGroupName(actionWithAccount, actionWithGroup));
					if (actionWithGroup != GroupManager.ACTIVE_CHATS
							&& actionWithGroup != GroupManager.IS_ROOM) {
						menu.add(0, CONTEXT_MENU_GROUP_RENAME_ID, 0,
								getText(R.string.group_rename));
						if (actionWithGroup != GroupManager.NO_GROUP)
							menu.add(0, CONTEXT_MENU_GROUP_DELETE_ID, 0,
									getText(R.string.group_remove));
					}
				}
			} else {
				return;
			}
		} else {
			// Account panel
			actionWithAccount = (String) accountToggleAdapter
					.getItemForView(view);
			actionWithGroup = null;
			actionWithUser = null;
		}
		// Group or account

		if (actionWithGroup == null) {
			// Account
			menu.setHeaderTitle(AccountManager.getInstance().getVerboseName(
					actionWithAccount));
			AccountItem accountItem = AccountManager.getInstance().getAccount(
					actionWithAccount);
			ConnectionState state = accountItem.getState();
			if (state == ConnectionState.waiting)
				menu.add(0, CONTEXT_MENU_ACCOUNT_RECONNECT_ID, 0,
						getText(R.string.account_reconnect));
			menu.add(0, CONTEXT_MENU_ACCOUNT_STATUS_ID, 0,
					getText(R.string.status_editor));
			menu.add(0, CONTEXT_MENU_ACCOUNT_EDITOR_ID, 0,
					getText(R.string.account_editor));
			if (state.isConnected()) {
				menu.add(0, CONTEXT_MENU_ACCOUNT_VCARD_ID, 0,
						getText(R.string.contact_viewer));
				menu.add(0, CONTEXT_MENU_ACCOUNT_ADD_CONTACT_ID, 0,
						getText(R.string.contact_add));
			}
		}

		if (actionWithGroup != null || SettingsManager.contactsShowAccounts()) {
			SubMenu mapMode = menu.addSubMenu(getResources().getText(
					R.string.show_offline_settings));
			mapMode.setHeaderTitle(R.string.show_offline_settings);
			MenuItem always = mapMode.add(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID,
					CONTEXT_MENU_SHOW_OFFLINE_ALWAYS_ID, 0, getResources()
							.getText(R.string.show_offline_always));
			MenuItem normal = mapMode.add(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID,
					CONTEXT_MENU_SHOW_OFFLINE_NORMAL_ID, 0, getResources()
							.getText(R.string.show_offline_normal));
			MenuItem never = mapMode.add(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID,
					CONTEXT_MENU_SHOW_OFFLINE_NEVER_ID, 0, getResources()
							.getText(R.string.show_offline_never));
			mapMode.setGroupCheckable(CONTEXT_MENU_SHOW_OFFLINE_GROUP_ID, true,
					true);
			ShowOfflineMode showOfflineMode = GroupManager.getInstance()
					.getShowOfflineMode(
							actionWithAccount,
							actionWithGroup == null ? GroupManager.IS_ACCOUNT
									: actionWithGroup);
			if (showOfflineMode == ShowOfflineMode.always)
				always.setChecked(true);
			else if (showOfflineMode == ShowOfflineMode.normal)
				normal.setChecked(true);
			else if (showOfflineMode == ShowOfflineMode.never)
				never.setChecked(true);
			else
				throw new IllegalStateException();
		}
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		// Contact
		case CONTEXT_MENU_VIEW_CHAT_ID:
			MessageManager.getInstance().openChat(actionWithAccount,
					actionWithUser);
			startActivity(ChatViewer.createIntent(this, actionWithAccount,
					actionWithUser));

			return true;
		case CONTEXT_MENU_VIEW_CONTACT_ID:
			startActivity(ContactViewer.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_EDIT_CONTACT_ID:
			startActivity(ContactEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_DELETE_CONTACT_ID:
			showDialog(DIALOG_DELETE_CONTACT_ID);
			return true;
		case CONTEXT_MENU_EDIT_ROOM_ID:
			startActivity(MUCEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_JOIN_ROOM_ID:
			MUCManager.getInstance().joinRoom(actionWithAccount,
					actionWithUser, true);
			return true;
		case CONTEXT_MENU_LEAVE_ROOM_ID:

			// AlertDialog.Builder builderRemove = new
			// AlertDialog.Builder(this);
			// builderRemove.setMessage(getString(R.string.muc_leave_confirm,
			// actionWithUser.split("@")[0]));
			// builderRemove.setNegativeButton(R.string.cancelar,
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// // TODO Auto-generated method stub
			//
			// }
			// });
			// builderRemove.setPositiveButton(R.string.aceptar,
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {

			MUCManager.getInstance().leaveRoom(actionWithAccount,
					actionWithUser);
			MessageManager.getInstance().closeChat(actionWithAccount,
					actionWithUser);
			NotificationManager.getInstance().removeMessageNotification(
					actionWithAccount, actionWithUser);
			contactListAdapter.onChange();
			// }
			// });
			// builderRemove.show();

			return true;
		case CONTEXT_MENU_CLOSE_CHAT_ID:

			HashMap params = new HashMap<String, Serializable>();
			params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
					getString(R.string.close_chat));
			params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
					getString(R.string.sure_close_chat));
			params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
					getString(R.string.aceptar));
			params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
					getString(R.string.cancelar));

			IDialogAction iDialogAction = new IDialogAction() {

				@Override
				public void onPositiveButton() {
					actionCloseChat();
				}

				@Override
				public void onNegativeButton() {

				}
			};
			params.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
					iDialogAction);

			DialogGeneric dialog = new DialogGeneric(ContactList.this, params);

			dialog.createDialog();
			dialog.showDialog();

			return true;
		case CONTEXT_MENU_REQUEST_SUBSCRIPTION_ID:
			try {
				PresenceManager.getInstance().requestSubscription(
						actionWithAccount, actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;
		case CONTEXT_MENU_ACCEPT_SUBSCRIPTION_ID:
			try {
				PresenceManager.getInstance().acceptSubscription(
						actionWithAccount, actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			startActivity(ContactEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case CONTEXT_MENU_DISCARD_SUBSCRIPTION_ID:
			try {
				PresenceManager.getInstance().discardSubscription(
						actionWithAccount, actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;

			// Group
		case CONTEXT_MENU_GROUP_RENAME_ID:
			showDialog(DIALOG_RENAME_GROUP_ID);
			return true;
		case CONTEXT_MENU_GROUP_DELETE_ID:
			showDialog(DIALOG_DELETE_GROUP_ID);
			return true;

			// Account
		case CONTEXT_MENU_ACCOUNT_RECONNECT_ID:
			if (AccountManager.getInstance().getAccount(actionWithAccount)
					.updateConnection(true))
				AccountManager.getInstance()
						.onAccountChanged(actionWithAccount);
			return true;
		case CONTEXT_MENU_ACCOUNT_VCARD_ID:
			String user = AccountManager.getInstance()
					.getAccount(actionWithAccount).getRealJid();
			if (user == null)
				Application.getInstance().onError(R.string.NOT_CONNECTED);
			else {
				startActivity(ContactViewer.createIntent(this,
						actionWithAccount, user));
			}
			return true;
		case CONTEXT_MENU_ACCOUNT_EDITOR_ID:
			startActivity(AccountEditor.createIntent(this, actionWithAccount));
			return true;
		case CONTEXT_MENU_ACCOUNT_STATUS_ID:
			startActivity(StatusEditor.createIntent(this, actionWithAccount));
			return true;
		case CONTEXT_MENU_ACCOUNT_ADD_CONTACT_ID:
			startActivity(ContactAdd.createIntent(this, actionWithAccount));
			return true;

			// Groups or account
		case CONTEXT_MENU_SHOW_OFFLINE_ALWAYS_ID:
			GroupManager.getInstance().setShowOfflineMode(
					actionWithAccount,
					actionWithGroup == null ? GroupManager.IS_ACCOUNT
							: actionWithGroup, ShowOfflineMode.always);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_SHOW_OFFLINE_NORMAL_ID:
			GroupManager.getInstance().setShowOfflineMode(
					actionWithAccount,
					actionWithGroup == null ? GroupManager.IS_ACCOUNT
							: actionWithGroup, ShowOfflineMode.normal);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_SHOW_OFFLINE_NEVER_ID:
			GroupManager.getInstance().setShowOfflineMode(
					actionWithAccount,
					actionWithGroup == null ? GroupManager.IS_ACCOUNT
							: actionWithGroup, ShowOfflineMode.never);
			contactListAdapter.onChange();
			return true;
		case CONTEXT_MENU_PHONE:
			List<String> phones = contactManager
					.getPhonesToPref(actionWithUser);
			if (phones != null && phones.size() > 0) {

				if (phones.size() > 1) {
					contactManager.createDialogPhones(phones);
				} else {
					contactManager.callPhone(phones.get(0));
				}
			}

			return true;

		case CONTEXT_MENU_EMAIL:

			contactManager.sendEmail(actionWithUser);

			return true;
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_DELETE_CONTACT_ID:
			int resource = 0;
			if (MUCManager.getInstance().hasRoom(actionWithAccount,
					actionWithUser)) {

				List<Occupant> ocupants = new ArrayList<Occupant>();
				ocupants.addAll(MUCManager.getInstance().getOccupants(
						actionWithAccount, actionWithUser));
				for (Occupant occupant : ocupants) {

					if (occupant.getJid().equals(actionWithAccount)
							&& occupant.getRole() == Role.moderator) {

						resource = R.string.muc_remove_confirm;

						break;
					} else {
						resource = R.string.muc_delete_confirm;
					}

				}
				if (resource == 0) {
					resource = R.string.muc_remove_confirm;
				}

			} else {
				resource = R.string.contact_delete_confirm;
			}
			return new ConfirmDialogBuilder(this, DIALOG_DELETE_CONTACT_ID,
					this).setMessage(
					getString(
							resource,
							RosterManager.getInstance()
									.getName(actionWithAccount, actionWithUser)
									.split("@")[0],
							AccountManager.getInstance()
									.getVerboseName(actionWithAccount)
									.split("@")[0])).create();
		case DIALOG_DELETE_GROUP_ID:
			return new ConfirmDialogBuilder(this, DIALOG_DELETE_GROUP_ID, this)
					.setMessage(
							getString(R.string.group_remove_confirm,
									actionWithGroup)).create();
		case DIALOG_RENAME_GROUP_ID:
			return new GroupRenameDialogBuilder(this, DIALOG_RENAME_GROUP_ID,
					this, actionWithGroup == GroupManager.NO_GROUP ? ""
							: actionWithGroup).create();
		case DIALOG_START_AT_BOOT_ID:
			return new ConfirmDialogBuilder(this, DIALOG_START_AT_BOOT_ID, this)
					.setMessage(getString(R.string.start_at_boot_suggest))
					.create();
		case DIALOG_CONTACT_INTEGRATION_ID:
			return new ConfirmDialogBuilder(this,
					DIALOG_CONTACT_INTEGRATION_ID, this).setMessage(
					getString(R.string.contact_integration_suggest)).create();
		case DIALOG_OPEN_WITH_ACCOUNT_ID:
			return new AccountChooseDialogBuilder(this,
					DIALOG_OPEN_WITH_ACCOUNT_ID, this, openDialogUser).create();
		case DIALOG_CLOSE_APPLICATION_ID:

			boolean registerOnPush = (Boolean) UtilPreferences.getPreferences(
					getApplicationContext(), UtilNotification.KEY_PREF_NOTIF,
					Boolean.class);
			if (!registerOnPush) {
				exit();
			} else {

				UsuarioDTO user = (UsuarioDTO) UtilPreferences.getPreferences(
						this, UsuarioWS.KEY_USUARIO, UsuarioDTO.class);
				if (user != null && user.getNick() != null) {
					UtilNotification.unregister(this, user.getNick());
				}

				ProgressDialog progressDialog = new ProgressDialog(this);
				progressDialog
						.setMessage(getString(R.string.application_state_closing));
				progressDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
				progressDialog.setIndeterminate(true);
				return progressDialog;
			}
			return null;

		default:
			return null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_SEARCH:
			search();
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	private void searchAction(String txt) {

		if (contactListAdapter != null) {
			if (txt.equalsIgnoreCase("")) {
				txt = null;
			}

			contactListAdapter.setFilterString(txt);
			contactListAdapter.onChange();
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		case R.id.img_search:

			String txt = etSearch.getText().toString();

			searchAction(txt);

			break;
		case R.id.common_status_mode:
			startActivity(StatusEditor.createIntent(this));
			break;
		case R.id.button: // Hint button
			switch ((Integer) view.getTag()) {
			case R.string.application_action_no_online:
				SettingsManager.setContactsShowOffline(true);
				contactListAdapter.onChange();
				break;
			case R.string.application_action_no_contacts:
				startActivity(ContactAdd.createIntent(this));
				break;
			case R.string.application_action_waiting:
				ConnectionManager.getInstance().updateConnections(true);
				break;
			case R.string.application_action_offline:
				AccountManager.getInstance().setStatus(StatusMode.available,
						null);
				break;
			case R.string.application_action_disabled:
				startActivity(AccountList.createIntent(this));
				break;
			case R.string.application_action_empty:
				startActivity(AccountAdd.createIntent(this));
				break;
			default:
				break;
			}
			updateStatusBar(null);
			break;
		case R.id.back_button: // Xabber icon button
		case R.id.common_status_text:
		case android.R.id.title:
			scrollUp();
			break;
		case R.id.iv_options:
			openOptionsMenu();
			break;
		default:
			String account = (String) accountToggleAdapter.getItemForView(view);
			if (account == null) // Check for tap on account in the title
				break;
			ListView listView = getListView();
			if (!SettingsManager.contactsShowAccounts()) {
				if (AccountManager.getInstance().getAccounts().size() < 2) {
					scrollUp();
				} else {
					if (account.equals(AccountManager.getInstance()
							.getSelectedAccount()))
						SettingsManager.setContactsSelectedAccount("");
					else
						SettingsManager.setContactsSelectedAccount(account);
					rebuildAccountToggler();
					contactListAdapter.onChange();
					stopMovement();
				}
			} else {
				long count = listView.getCount();
				for (int position = 0; position < (int) count; position++) {
					BaseEntity baseEntity = (BaseEntity) listView
							.getItemAtPosition(position);
					if (baseEntity != null
							&& baseEntity instanceof AccountConfiguration
							&& baseEntity.getAccount().equals(account)) {
						listView.setSelection(position);
						stopMovement();
						break;
					}
				}
			}
			break;
		}
	}

	/**
	 * Stop fling scrolling.
	 */
	private void stopMovement() {
		getListView().onTouchEvent(
				MotionEvent.obtain(SystemClock.uptimeMillis(),
						SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL,
						0, 0, 0));
	}

	/**
	 * Scroll to the top of contact list.
	 */
	private void scrollUp() {
		ListView listView = getListView();
		if (listView.getCount() > 0)
			listView.setSelection(0);
		stopMovement();
	}

	@Override
	public boolean onLongClick(View view) {
		switch (view.getId()) {
		case R.id.common_status_text:
			startActivity(StatusEditor.createIntent(this));
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object object = parent.getAdapter().getItem(position);
		if (object == null) {
			// Account toggler
		} else if (object instanceof AbstractContact) {
			AbstractContact abstractContact = (AbstractContact) object;
			if (ACTION_ROOM_INVITE.equals(action)) {
				action = null;
				Intent intent = getIntent();
				String account = getRoomInviteAccount(intent);
				String user = getRoomInviteUser(intent);
				if (account != null && user != null)
					try {
						MUCManager.getInstance().invite(account, user,
								abstractContact.getUser());
					} catch (NetworkException e) {
						Application.getInstance().onError(e);
					}
				finish();
			} else if (Intent.ACTION_SEND.equals(action)) {
				action = null;

				action = null;
				startActivity(ChatViewer.createSendIntent(this,
						abstractContact.getAccount(),
						abstractContact.getUser(), sendText));
				finish();

			} else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
				Intent intent = new Intent();
				intent.putExtra(
						Intent.EXTRA_SHORTCUT_INTENT,
						ChatViewer.createClearTopIntent(this,
								abstractContact.getAccount(),
								abstractContact.getUser()));
				intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
						abstractContact.getName());
				Bitmap bitmap;
				if (MUCManager.getInstance()
						.hasRoom(abstractContact.getAccount(),
								abstractContact.getUser()))
					bitmap = AvatarManager.getInstance().getRoomBitmap(
							abstractContact.getUser());
				else
					bitmap = AvatarManager.getInstance().getUserBitmap(
							abstractContact.getUser());
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, AvatarManager
						.getInstance().createShortcutBitmap(bitmap));
				setResult(RESULT_OK, intent);
				finish();
			} else {

				Intent i = null;
				if (messageItem != null) {

					// if (!MUCManager.getInstance().hasRoom(
					// abstractContact.getAccount(),
					// abstractContact.getUser())) {

					MessageItem message = new MessageItem(messageItem);

					i = ChatViewer.createIntent(this,
							abstractContact.getAccount(),
							abstractContact.getUser(),
							abstractContact.getName(), message);

					messageItem = null;
					// } else {
					// Toast.makeText(ContactList.this,
					// R.string.send_file_error_sala,
					// Toast.LENGTH_SHORT).show();
					//
					// }

				} else {
					i = ChatViewer.createIntent(this,
							abstractContact.getAccount(),
							abstractContact.getUser(),
							abstractContact.getName());
				}
				if (i != null) {
					startActivity(i);
				}

			}
		} else if (object instanceof GroupConfiguration) {
			GroupConfiguration groupConfiguration = (GroupConfiguration) object;
			contactListAdapter.setExpanded(groupConfiguration.getAccount(),
					groupConfiguration.getUser(),
					!groupConfiguration.isExpanded());
		}
	}

	@Override
	public void onContactsChanged(Collection<BaseEntity> addresses) {
		contactListAdapter.refreshRequest();
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {

		accountToggleAdapter.onChange();
		contactListAdapter.refreshRequest();
	}

	@Override
	public void onChatChanged(String account, String user, boolean incoming) {
		if (incoming)
			contactListAdapter.refreshRequest();
	}

	@Override
	public void onAccept(DialogBuilder dialogBuilder) {
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_DELETE_CONTACT_ID:
			if (MUCManager.getInstance().hasRoom(actionWithAccount,
					actionWithUser)) {

				Boolean isModerator = false;
				List<Occupant> ocupants = new ArrayList<Occupant>();
				ocupants.addAll(MUCManager.getInstance().getOccupants(
						actionWithAccount, actionWithUser));
				for (Occupant occupant : ocupants) {

					if (occupant.getJid().equals(actionWithAccount)
							&& occupant.getRole() == Role.moderator) {

						isModerator = true;
						break;
					}
				}

				MUCManager.getInstance().removeRoom(actionWithAccount,
						actionWithUser);
				MessageManager.getInstance().closeChat(actionWithAccount,
						actionWithUser);
				NotificationManager.getInstance().removeMessageNotification(
						actionWithAccount, actionWithUser);

				if (isModerator) {

					MUCManager.getInstance().destroyRoom(actionWithAccount,
							actionWithUser);
				}

			} else
				try {

					try {
						MessageManager.getInstance().closeChat(
								actionWithAccount, actionWithUser);
						NotificationManager.getInstance()
								.removeMessageNotification(actionWithAccount,
										actionWithUser);
						contactListAdapter.onChange();
					} catch (Exception e) {
						e.printStackTrace();
					}

					RosterManager.getInstance().removeContact(
							actionWithAccount, actionWithUser);
				} catch (NetworkException e) {
					Application.getInstance().onError(e);
				}

			break;
		case DIALOG_DELETE_GROUP_ID:
			try {
				if (actionWithAccount == GroupManager.NO_ACCOUNT)
					RosterManager.getInstance().removeGroup(actionWithGroup);
				else
					RosterManager.getInstance().removeGroup(actionWithAccount,
							actionWithGroup);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			break;
		case DIALOG_RENAME_GROUP_ID:
			String name = ((GroupRenameDialogBuilder) dialogBuilder).getName();
			String source = actionWithGroup == GroupManager.NO_GROUP ? null
					: actionWithGroup;
			try {
				if (actionWithAccount == GroupManager.NO_ACCOUNT)
					RosterManager.getInstance().renameGroup(source, name);
				else
					RosterManager.getInstance().renameGroup(actionWithAccount,
							source, name);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			break;
		case DIALOG_START_AT_BOOT_ID:
			SettingsManager.setStartAtBootSuggested();
			SettingsManager.setConnectionStartAtBoot(true);
			break;
		case DIALOG_CONTACT_INTEGRATION_ID:
			SettingsManager.setContactIntegrationSuggested();
			for (String account : AccountManager.getInstance().getAllAccounts())
				AccountManager.getInstance().setSyncable(account, true);
			break;
		case DIALOG_OPEN_WITH_ACCOUNT_ID:
			BaseEntity baseEntity = new BaseEntity(
					((AccountChooseDialogBuilder) dialogBuilder).getSelected(),
					openDialogUser);
			openChat(baseEntity, openDialogText);
			break;
		}
	}

	@Override
	public void onDecline(DialogBuilder dialogBuilder) {
		switch (dialogBuilder.getDialogId()) {
		case DIALOG_START_AT_BOOT_ID:
			SettingsManager.setStartAtBootSuggested();
			break;
		case DIALOG_CONTACT_INTEGRATION_ID:
			SettingsManager.setContactIntegrationSuggested();
			break;
		}
	}

	@Override
	public void onCancel(DialogBuilder dialogBuilder) {
	}

	public void updateStatusBar(String state) {

		Log.e("STATE", state + "");

		String statusText = SettingsManager.statusText();
		StatusMode statusMode = SettingsManager.statusMode();
		if ((state == null || state.equals("")) && "".equals(statusText)) {
			statusText = getString(statusMode.getStringID());
		} else {
			statusText = state;
		}

		// TextView tv = null;
		// tv = (TextView) findViewById(R.id.tv_status_line);
		// if (tv != null) {
		// tv.setText(statusText);
		// }
		((TextView) findViewById(R.id.common_status_text)).setText(statusText);
		((ImageView) findViewById(R.id.common_status_mode))
				.setImageLevel(statusMode.getStatusLevel());

		setStatusUser(tvStatus, ivStatus, statusMode.getStatusLevel(),
				statusText);

	}

	private void rebuildAccountToggler() {
		updateStatusBar(null);
		accountToggleAdapter.rebuild();
		if (SettingsManager.contactsShowPanel()
				&& accountToggleAdapter.getCount() > 0)
			titleView.setVisibility(View.VISIBLE);
		else
			titleView.setVisibility(View.GONE);

		titleView.setVisibility(View.GONE);

	}

	/**
	 * Show search dialog.
	 */
	private void search() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null)
			inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
					0);
	}

	public static Intent createPersistentIntent(Context context) {
		Intent intent = new Intent(context, ContactList.class);
		intent.setAction("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.LAUNCHER");
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, ContactList.class);
	}

	public static Intent createRoomInviteIntent(Context context,
			String account, String room) {
		Intent intent = new EntityIntentBuilder(context, ContactList.class)
				.setAccount(account).setUser(room).build();
		intent.setAction(ACTION_ROOM_INVITE);
		return intent;
	}

	public static Intent createForwardIntent(Context context,
			MessageItem messageItem) {
		Intent intent = new EntityIntentBuilder(context, ContactList.class)
				.build();
		intent.putExtra(EXTRA_FORWARD_MESSAGE, messageItem);
		intent.setAction(ACTION_FORWARD_MESSAGE);
		return intent;
	}

	public static Intent createImportDDBBIntent(Context context) {
		Intent intent = new EntityIntentBuilder(context, ContactList.class)
				.build();
		intent.setAction(ACTION_IMPORT_DDBB);
		return intent;
	}

	private static String getRoomInviteAccount(Intent intent) {
		return EntityIntentBuilder.getAccount(intent);
	}

	private static String getRoomInviteUser(Intent intent) {
		return EntityIntentBuilder.getUser(intent);
	}

	// @Override
	// public void loadTopBar() {
	//
	// if (getParent() != null && getParent().getParent() != null) {
	//
	// loadTopBarChat(getParent().getParent(),
	// getString(R.string.chat_th), this);
	//
	// }
	//
	// }

	public void goToChatOnContact(BaseEntity baseEntity) {

		// if (baseEntity != null) {
		//
		// ChatGroup.chatGroup.deleteOne();
		//
		// Intent i = ChatViewer.createIntent(this, baseEntity.getAccount(),
		// baseEntity.getUser());
		//
		// View view2 = ChatGroup.chatGroup
		// .getLocalActivityManager()
		// .startActivity("Chat",
		// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
		// .getDecorView();
		//
		// // Again, replace the view
		// ChatGroup.chatGroup.replaceView(view2);
		//
		// }

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();

		if (connected) {
			getMenuInflater().inflate(R.menu.edit_contact_list, menu);
		} else {
			boolean registerOnPush = (Boolean) UtilPreferences.getPreferences(
					getApplicationContext(), UtilNotification.KEY_PREF_NOTIF,
					Boolean.class);

			if (!registerOnPush) {
				menu.add(0, R.id.action_log_out, 0,
						getResources().getText(R.string.log_out)).setIcon(
						R.drawable.menu_icono_sesion);

			}

			menu.add(0, R.id.action_state, 0,
					getResources().getText(R.string.status_editor)).setIcon(
					R.drawable.menu_icono_estado);
		}

		return true;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.edit_contact_list, menu);
	// return true;
	// }

	public static void loadTopBarChat(final Activity activity, String text,
			final Activity thisActivity) {

		// ImageView imgSend = (ImageView) activity.findViewById(R.id.img_send);
		// if (imgSend != null) {
		// imgSend.setOnClickListener(null);
		// imgSend.setVisibility(View.GONE);
		// }

		LinearLayout ll = (LinearLayout) activity.findViewById(R.id.ll_icons);
		if (ll != null) {
			ll.setVisibility(View.VISIBLE);
		}

		// LinearLayout llSearch = (LinearLayout) activity
		// .findViewById(R.id.ll_search);
		// if (llSearch != null) {
		//
		// llSearch.setVisibility(View.GONE);
		// }

		// TextView tvUnder = (TextView) activity
		// .findViewById(R.id.tv_status_line);
		// if (tvUnder != null) {
		// tvUnder.setVisibility(View.GONE);
		// }

		TextView tvTop = (TextView) activity.findViewById(R.id.tv_top_bar);
		if (tvTop != null) {
			tvTop.setText(text);
		}

		// ImageView imgSetting = (ImageView) activity
		// .findViewById(R.id.img_settings);
		// if (imgSetting != null) {
		// imgSetting.setVisibility(View.VISIBLE);
		// imgSetting.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// thisActivity.openOptionsMenu();
		//
		// }
		// });
		// }

	}

	private void showFileChooser() {

		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.putExtra("crop", "true");
		// indicate aspect of desired crop
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// indicate output X and Y
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		// retrieve data on return
		intent.putExtra("return-data", true);
		intent.setType("image/*");
		try {
			this.startActivityForResult(
					Intent.createChooser(intent,
							getResources().getText(R.string.choose_file)),
					FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			Toast.makeText(this,
					getResources().getString(R.string.install_manager_file),
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this,
					getResources().getString(R.string.install_manager_file),
					Toast.LENGTH_SHORT).show();
		}

		// cropImage();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {

			// Uri uri;
			// String path = "";
			switch (requestCode) {
			case FILE_SELECT_CODE:

				// // Get the Uri of the selected file
				// uri = data.getData();
				//
				// try {
				// path = getPath(this, uri);
				// } catch (URISyntaxException e) {
				// e.printStackTrace();
				// }

				if (data != null) {
					// get the returned data
					Bundle extras = data.getExtras();

					// get the cropped bitmap
					Bitmap selectedBitmap = extras.getParcelable("data");

					if (selectedBitmap != null) {
						new ChangeUserAvatar(selectedBitmap).execute();
					}
					if (selectedBitmap != null) {
						selectedBitmap.getWidth();
						selectedBitmap.getHeight();
					}

				}

				break;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(inContext.getContentResolver(),
				inImage, "Title", null);
		return Uri.parse(path);
	}

	public String getRealPathFromURI(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}

	public static String getPath(Context context, Uri uri)
			throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	class ChangeUserAvatar extends AsyncTask<Void, Void, Boolean> {
		private String path;
		private Bitmap bmAvatar;

		public ChangeUserAvatar(String path) {
			this.path = path;
		}

		public ChangeUserAvatar(Bitmap bmAvatar) {
			this.bmAvatar = bmAvatar;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			String accountItem = null;
			Collection<String> accounts = AccountManager.getInstance()
					.getAccounts();
			if (accounts != null && !accounts.isEmpty()) {
				for (String account : accounts) {
					accountItem = account;
				}
			}

			AccountItem account = AccountManager.getInstance().getAccount(
					accountItem);

			// File filePath = new File(path);
			Bitmap bitmap = null;
			if (bmAvatar != null) {
				bitmap = bmAvatar;
			} else {
				bitmap = BitmapFactory.decodeFile(path);
			}
			// mImageView.setImageBitmap(bitmap);
			String hash = BitMapToString(bitmap);
			String thisBareAddress = Jid.getBareAddress(accountItem);

			Application.getInstance().setvCardPending(true);
			VCardManager.getInstance().requestAvatar(account.getAccount(),
					thisBareAddress, hash);

			// org.jivesoftware.smackx.packet.VCard vCard = null;
			// if (accountItem != null && path != null) {
			// vCard = new org.jivesoftware.smackx.packet.VCard();
			// // vCard.setNickName(accountItem.split("@")[0] + "rgregrg");
			// vCard.setNickName("Quiero un erro leñes");
			// FileInputStream fis;
			// try {
			// fis = new FileInputStream(path);
			//
			// StringBuffer fileContent = new StringBuffer("");
			//
			// byte[] buffer = new byte[1024];
			// int n;
			// while ((n = fis.read(buffer)) != -1) {
			// fileContent.append(new String(buffer, 0, n));
			// }
			// vCard.setAvatar(buffer);
			// } catch (FileNotFoundException e) {
			//
			// vCard = null;
			// e.printStackTrace();
			// } catch (IOException e) {
			//
			// vCard = null;
			// e.printStackTrace();
			// }
			// }
			//
			// if (vCard != null && accountItem != null) {
			// Application.getInstance().setvCardPending(true);
			// XMPPConnection mXMPPConnection = AccountManager.getInstance()
			// .getAccount(accountItem).getConnectionThread()
			// .getXMPPConnection();
			// mXMPPConnection.sendPacket(vCard);
			// }

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// String resultText = null;
			// Toast.makeText(ContactList.this, resultText, Toast.LENGTH_SHORT)
			// .show();
		}

	}

	/**
	 * @param bitmap
	 * @return converting bitmap and return a string
	 */
	public String BitMapToString(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		String temp = Base64.encodeToString(b, Base64.DEFAULT);
		return temp;
	}

}
