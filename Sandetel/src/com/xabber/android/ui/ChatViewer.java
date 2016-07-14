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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import Constantes.Constantes;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.xabber.android.data.ActivityManager;
import com.xabber.android.data.Application;
import com.xabber.android.data.ContactManager;
import com.xabber.android.data.LogManager;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.SettingsManager.ChatsHideKeyboard;
import com.xabber.android.data.SettingsManager.SecurityOtrMode;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.OnAccountChangedListener;
import com.xabber.android.data.entity.BaseEntity;
import com.xabber.android.data.extension.archive.MessageArchiveManager;
import com.xabber.android.data.extension.attention.AttentionManager;
import com.xabber.android.data.extension.cs.ChatStateManager;
import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.extension.muc.Occupant;
import com.xabber.android.data.extension.muc.RoomChat;
import com.xabber.android.data.extension.muc.RoomState;
import com.xabber.android.data.extension.otr.OTRManager;
import com.xabber.android.data.extension.otr.SecurityLevel;
import com.xabber.android.data.intent.EntityIntentBuilder;
import com.xabber.android.data.message.AbstractChat;
import com.xabber.android.data.message.Consigna;
import com.xabber.android.data.message.MessageItem;
import com.xabber.android.data.message.MessageManager;
import com.xabber.android.data.message.OnChatChangedListener;
import com.xabber.android.data.message.RegularChat;
import com.xabber.android.data.message.chat.ChatManager;
import com.xabber.android.data.notification.NotificationManager;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.OnContactChangedListener;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.service.constantes.Urls;
import com.xabber.android.ui.adapter.ChatViewerAdapter;
import com.xabber.android.ui.adapter.ChatViewerAdapter.ChatViewHolder;
import com.xabber.android.ui.adapter.OnTextChangedListener;
import com.xabber.android.ui.helper.ManagedActivity;
import com.xabber.android.ui.widget.PageSwitcher;
import com.xabber.android.ui.widget.PageSwitcher.OnSelectListener;
import com.xabber.android.utils.dialogs.DialogGeneric;
import com.xabber.android.utils.dialogs.IDialogAction;
import com.xabber.android.utils.file.RealPathUtil;
import com.xabber.android.utils.rounded.RoundedUtil;
import com.xabber.android.utils.share.email.EmailManager;
import com.xabber.androiddev.async.AsyncTask;
import com.xabber.xmpp.muc.Role;

import es.juntadeandalucia.android.im.R;

/**
 * Chat activity.
 * 
 * Warning: {@link PageSwitcher} is to be removed and related implementation is
 * to be fixed.
 * 
 * @author alexander.ivanov
 * 
 */
public class ChatViewer extends ManagedActivity implements
		View.OnClickListener, View.OnKeyListener, OnSelectListener,
		OnChatChangedListener, OnContactChangedListener,
		OnAccountChangedListener, OnEditorActionListener,
		OnTextChangedListener, OnTouchListener {

	private String path;
	private String name;
	// private Bitmap fotoGuardar;
	private Calendar c;
	private int currentapiVersion;

	private MessageItem pendingMessageItem;
	private Boolean isRecording = false, initRecord = false;;

	/**
	 * Menu
	 */
	private Menu menu;

	/**
	 * Record audio
	 */

	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	// private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private MediaRecorder recorder = null;
	private int currentFormat = 0;
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
			MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_3GP };
	private String pathRecord = null;

	/**
	 * Pick Up File to send
	 */
	public static final int FILE_SELECT_CODE = 0;
	public static final int FILE_SELECT_CODE_CAMERA = 1;
	private static final int PICKFILE_RESULT_CODE = 2;

	/**
	 * CONFIGURE PHOTO
	 */
	// FOTO
	public static final Integer ALTURA = 480;
	public static final Integer ANCHURA = 640;

	/**
	 * Attention request.
	 */
	private static final String ACTION_ATTENTION = "com.xabber.android.data.ATTENTION";

	/**
	 * Minimum number of new messages to be requested from the server side
	 * archive.
	 */
	private static final int MINIMUM_MESSAGES_TO_LOAD = 10;

	private static final String SAVED_ACCOUNT = "com.xabber.android.ui.ChatViewer.SAVED_ACCOUNT";
	private static final String SAVED_USER = "com.xabber.android.ui.ChatViewer.SAVED_USER";
	private static final String SAVED_EXIT_ON_SEND = "com.xabber.android.ui.ChatViewer.EXIT_ON_SEND";

	private static final int OPTION_MENU_VIEW_CONTACT_ID = 0x01;
	private static final int OPTION_MENU_CHAT_LIST_ID = 0x02;
	private static final int OPTION_MENU_CLOSE_CHAT_ID = 0x03;
	private static final int OPTION_MENU_SHOW_HISTORY_ID = 0x04;
	private static final int OPTION_MENU_SETTINGS_ID = 0x05;
	private static final int OPTION_MENU_CLEAR_HISTORY_ID = 0x06;
	private static final int OPTION_MENU_CLEAR_MESSAGE_ID = 0x07;
	private static final int OPTION_MENU_EXPORT_CHAT_ID = 0x08;
	private static final int OPTION_MENU_CALL_ATTENTION_ID = 0x09;
	private static final int OPTION_MENU_SEND_FILE_ID = 0x0a;

	private static final int OPTION_MENU_LEAVE_ROOM_ID = 0x10;
	private static final int OPTION_MENU_JOIN_ROOM_ID = 0x11;
	private static final int OPTION_MENU_MUC_INVITE_ID = 0x12;
	private static final int OPTION_MENU_EDIT_ROOM_ID = 0x13;
	private static final int OPTION_MENU_OCCUPANT_LIST_ID = 0x14;

	private static final int OPTION_MENU_START_OTR_ID = 0x20;
	private static final int OPTION_MENU_END_OTR_ID = 0x21;
	private static final int OPTION_MENU_VERIFY_FINGERPRINT_ID = 0x22;
	private static final int OPTION_MENU_VERIFY_QUESTION_ID = 0x23;
	private static final int OPTION_MENU_VERIFY_SECRET_ID = 0x24;
	private static final int OPTION_MENU_REFRESH_OTR_ID = 0x25;

	private static final int DIALOG_DELETE_CONTACT_ID = 0x50;

	private static final int CONTEXT_MENU_QUOTE_ID = 0x100;
	private static final int CONTEXT_MENU_REPEAT_ID = 0x101;
	private static final int CONTEXT_MENU_COPY_ID = 0x102;
	private static final int CONTEXT_MENU_REMOVE_ID = 0x103;

	public static final int DIALOG_EXPORT_CHAT_ID = 0x200;
	public static final int DIALOG_SEND_CHAT_ID = 0x201;

	private ChatViewerAdapter chatViewerAdapter;
	private PageSwitcher pageSwitcher;

	private String actionWithAccount;
	private String actionWithUser;
	private View actionWithView;
	private MessageItem actionWithMessage;

	private boolean exitOnSend;

	private boolean isVisible;

	private TextView tvDesc;
	private ImageView ivState;
	private String userName;
	private AbstractContact ac;
	private Boolean isModerator;

	/**
	 * instance of contact manager
	 */
	private ContactManager contactManager;
	private List<String> phones;
	private Thread t;

	/**
	 * Cambia a offline el estado del usuario con el que se chatea en la barra
	 * superior de navegacion
	 */
	public void setOfflineState(String text, Drawable d) {

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
	public void setOnlineState(String text, Drawable d) {

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
	public void setAwayState(String text, Drawable d) {

		if (tvDesc != null && ivState != null) {
			tvDesc.setText(text);
			tvDesc.setTextColor(this.getResources().getColor(R.color.away));

			ivState.setImageDrawable(d);
		}

	}

	public void setStatusUser() {

		if (ac != null) {
			switch (ac.getStatusMode().getStatusLevel()) {

			case 0:

				setOnlineState(
						(this.getString(ac.getStatusMode().getStringID())),
						this.getResources().getDrawable(
								R.drawable.chat_bola_verde_listo));

				break;

			case 1:

				setOnlineState(
						(this.getString(ac.getStatusMode().getStringID())),
						this.getResources().getDrawable(
								R.drawable.chat_bola_verde));

				break;

			case 2:

				setAwayState(
						(this.getString(ac.getStatusMode().getStringID())),
						this.getResources().getDrawable(
								R.drawable.chat_bola_ausente));

				break;

			case 3:

				setAwayState(
						(this.getString(ac.getStatusMode().getStringID())),
						this.getResources().getDrawable(
								R.drawable.chat_bola_ausente_extendido));

				break;
			case 8:

				setOfflineState(
						(this.getString(ac.getStatusMode().getStringID())),
						this.getResources().getDrawable(
								R.drawable.chat_bola_roja));

				break;

			default:

				setOfflineState(
						(this.getString(ac.getStatusMode().getStringID())),
						this.getResources().getDrawable(
								R.drawable.chat_bola_roja));

				break;
			}
		} else {

			setOfflineState((this.getString(R.string.unsubscribed)), this
					.getResources().getDrawable(R.drawable.chat_bola_roja));

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isFinishing())
			return;

		currentapiVersion = android.os.Build.VERSION.SDK_INT;
		contactManager = new ContactManager(ChatViewer.this);

		Intent intent = getIntent();
		String account = getAccount(intent);
		String user = getUser(intent);
		pendingMessageItem = getMessageItem(intent);
		userName = getName(intent);

		if (PageSwitcher.LOG)
			LogManager.i(this, "Intent: " + account + ":" + user);
		if (account == null || user == null) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			finish();
			return;
		}
		if (hasAttention(intent)) {
			AttentionManager.getInstance().removeAccountNotifications(account,
					user);
		}
		actionWithAccount = null;
		actionWithUser = null;
		actionWithView = null;
		actionWithMessage = null;

		setContentView(R.layout.chat_viewer);
		chatViewerAdapter = new ChatViewerAdapter(this, account, user, userName);
		chatViewerAdapter.setOnClickListener(this);
		chatViewerAdapter.setOnKeyListener(this);
		chatViewerAdapter.setOnEditorActionListener(this);
		chatViewerAdapter.setOnCreateContextMenuListener(this);
		chatViewerAdapter.setOnTextChangedListener(this);
		chatViewerAdapter.setOnTouchListener(this);
		pageSwitcher = (PageSwitcher) findViewById(R.id.switcher);
		pageSwitcher.setAdapter(chatViewerAdapter);
		pageSwitcher.setOnSelectListener(this);

		if (savedInstanceState != null) {
			actionWithAccount = savedInstanceState.getString(SAVED_ACCOUNT);
			actionWithUser = savedInstanceState.getString(SAVED_USER);
			exitOnSend = savedInstanceState.getBoolean(SAVED_EXIT_ON_SEND);
		}
		if (actionWithAccount == null)
			actionWithAccount = account;
		if (actionWithUser == null) {
			actionWithUser = user;
		}

		selectChat(actionWithAccount, actionWithUser);

		// TextView tvUser = (TextView) findViewById(R.id.tv_user_titulo);
		// ac = new AbstractContact(actionWithAccount, actionWithUser);
		// if (tvUser != null && userName != null) {
		// tvUser.setText(userName);
		// }

		// AccountManager accountManager = AccountManager.getInstance();
		// AccountItem accountItem = accountManager.getAccount(actionWithUser);

		// tvDesc = (TextView) findViewById(R.id.status_text);
		// ivState = (ImageView) findViewById(R.id.iv_state);
		//
		// tvDesc.setText(this.getString(ac.getStatusMode().getStringID()));

	}

	@Override
	protected void onResume() {
		super.onResume();
		Application.getInstance().addUIListener(OnChatChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnContactChangedListener.class,
				this);
		Application.getInstance().addUIListener(OnAccountChangedListener.class,
				this);
		chatViewerAdapter.onChange();
		if (actionWithView != null)
			chatViewerAdapter.onChatChange(actionWithView, false);
		Intent intent = getIntent();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			String additional = intent.getStringExtra(Intent.EXTRA_TEXT);
			if (additional != null) {
				intent.removeExtra(Intent.EXTRA_TEXT);
				exitOnSend = true;
				if (actionWithView != null)
					insertText(additional);
			}
		}
		isVisible = true;

		new ForwardMessage().execute();

		// loadTopBar();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (PageSwitcher.LOG)
			LogManager.i(this, "onSave: " + actionWithAccount + ":"
					+ actionWithUser);
		outState.putString(SAVED_ACCOUNT, actionWithAccount);
		outState.putString(SAVED_USER, actionWithUser);
		outState.putBoolean(SAVED_EXIT_ON_SEND, exitOnSend);
	}

	@Override
	protected void onPause() {
		super.onPause();

		Application.getInstance().removeUIListener(OnChatChangedListener.class,
				this);
		Application.getInstance().removeUIListener(
				OnContactChangedListener.class, this);
		Application.getInstance().removeUIListener(
				OnAccountChangedListener.class, this);
		MessageManager.getInstance().removeVisibleChat();
		pageSwitcher.saveState();
		isVisible = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (isFinishing())
			return;

		String account = getAccount(intent);
		String user = getUser(intent);
		if (account == null || user == null) {
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
			return;
		}
		if (hasAttention(intent))
			AttentionManager.getInstance().removeAccountNotifications(account,
					user);

		chatViewerAdapter.onChange();
		if (!selectChat(account, user))
			Application.getInstance().onError(R.string.ENTRY_IS_NOT_FOUND);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.clear();
		this.menu = menu;
		AbstractChat abstractChat = MessageManager.getInstance().getChat(
				actionWithAccount, actionWithUser);
		if (abstractChat == null || !(abstractChat instanceof RoomChat)) {
			getMenuInflater().inflate(R.menu.action_bar, menu);

			// contactManager.reset();
			phones = contactManager.getPhonesToPref(actionWithUser);
			MenuItem menuItem = menu.getItem(5);
			if (menuItem != null
					&& menuItem.getItemId() == R.id.action_call_phones) {
				if (phones != null && phones.size() > 0) {

					if (phones.size() > 1) {
						menuItem.setTitle(R.string.contact_see_phones);

					} else {
						menuItem.setTitle(R.string.contact_call_phone);
					}
				} else {

					menu.removeItem(R.id.action_call_phones);
				}
			}

		} else {
			if (abstractChat != null && abstractChat instanceof RoomChat) {

				menu.add(0, R.id.action_send_file, 0,
						getResources().getText(R.string.enviar_archivo))
						.setIcon(R.drawable.menu_icono_enviar_archivo);

				if (((RoomChat) abstractChat).getState() == RoomState.unavailable)
					menu.add(0, OPTION_MENU_JOIN_ROOM_ID, 0,
							getResources().getText(R.string.muc_join)).setIcon(
							R.drawable.menu_icono_unirse_sala);
				else
					menu.add(0, OPTION_MENU_MUC_INVITE_ID, 0,
							getResources().getText(R.string.muc_invite))
							.setIcon(R.drawable.menu_icono_invitar_usuario);
			} else {
				// menu.add(0, OPTION_MENU_VIEW_CONTACT_ID, 0,
				// getResources().getText(R.string.contact_editor))
				// .setIcon(android.R.drawable.ic_menu_edit);
				// menu.add(0, OPTION_MENU_VIEW_CONTACT_ID, 0,
				// getResources().getText(R.string.contact_editor))
				// .setIcon(android.R.drawable.ic_secure);
			}
			// menu.add(0, OPTION_MENU_SEND_FILE_ID, 0,
			// getResources().getText(R.string.contact_send)).setIcon(
			// R.drawable.ic_menu_friendslist);
			// menu.add(0, OPTION_MENU_SETTINGS_ID, 0,
			// getText(R.string.chat_settings)).setIcon(
			// android.R.drawable.ic_menu_preferences);
			menu.add(0, OPTION_MENU_SHOW_HISTORY_ID, 0,
					getText(R.string.show_history)).setIcon(
					R.drawable.menu_icono_mostrar_historial);

			menu.add(0, OPTION_MENU_CLEAR_HISTORY_ID, 0,
					getText(R.string.clear_history)).setIcon(
					R.drawable.menu_icono_limpiar_historial);

			menu.add(0, R.id.action_send_con, 0,
					getResources().getText(R.string.send_chat)).setIcon(
					R.drawable.menu_icono_enviar_archivo);
			if (abstractChat != null
					&& abstractChat instanceof RoomChat
					&& ((RoomChat) abstractChat).getState() != RoomState.unavailable) {

				if (((RoomChat) abstractChat).getState() == RoomState.error) {
					// menu.add(0, OPTION_MENU_EDIT_ROOM_ID, 0,
					// getResources().getText(R.string.muc_edit)).setIcon(
					// android.R.drawable.ic_menu_edit);
				} else {
					menu.add(0, OPTION_MENU_LEAVE_ROOM_ID, 0,
							getResources().getText(R.string.muc_leave))
							.setIcon(R.drawable.menu_icono_abandonar_sala);

					int resource = 0;
					int img = 0;
					if (MUCManager.getInstance().hasRoom(actionWithAccount,
							actionWithUser)) {

						List<Occupant> ocupants = new ArrayList<Occupant>();
						ocupants.addAll(MUCManager.getInstance().getOccupants(
								actionWithAccount, actionWithUser));
						for (Occupant occupant : ocupants) {

							if (occupant.getJid().equals(actionWithAccount)
									&& occupant.getRole() == Role.moderator) {

								img = R.drawable.menu_icono_eliminar_sala;
								resource = R.string.muc_delete_owner;

								break;
							} else {
								img = R.drawable.menu_icono_bloquear_sala;
								resource = R.string.muc_delete;
							}

						}
						if (resource == 0) {
							img = R.drawable.menu_icono_bloquear_sala;
							resource = R.string.muc_delete;
						}

						menu.add(0, R.id.silence, 0,
								getResources().getText(R.string.enviar_archivo))
								.setIcon(R.drawable.menu_icono_config);

						menu.add(0, DIALOG_DELETE_CONTACT_ID, 0,
								getResources().getText(resource)).setIcon(img);

					}

				}

			} else {

				int resource = 0;
				int img = 0;
				if (MUCManager.getInstance().hasRoom(actionWithAccount,
						actionWithUser)) {

					List<Occupant> ocupants = new ArrayList<Occupant>();
					ocupants.addAll(MUCManager.getInstance().getOccupants(
							actionWithAccount, actionWithUser));
					for (Occupant occupant : ocupants) {

						if (occupant.getJid().equals(actionWithAccount)
								&& occupant.getRole() == Role.moderator) {

							img = R.drawable.menu_icono_eliminar_sala;
							resource = R.string.muc_delete_owner;

							break;
						} else {
							img = R.drawable.menu_icono_bloquear_sala;
							resource = R.string.muc_delete;
						}

					}
					if (resource == 0) {
						img = R.drawable.menu_icono_bloquear_sala;
						resource = R.string.muc_delete;
					}

					menu.add(0, OPTION_MENU_CLOSE_CHAT_ID, 0,
							getResources().getText(resource)).setIcon(img);

				}

			}
			// menu.add(0, OPTION_MENU_CLEAR_MESSAGE_ID, 0,
			// getResources().getText(R.string.clear_message)).setIcon(
			// R.drawable.ic_menu_stop);

			// menu.add(0, OPTION_MENU_EXPORT_CHAT_ID, 0,
			// getText(R.string.export_chat));
			if (abstractChat != null && abstractChat instanceof RegularChat) {
				menu.add(0, OPTION_MENU_CALL_ATTENTION_ID, 0,
						getText(R.string.call_attention));
				SecurityLevel securityLevel = OTRManager.getInstance()
						.getSecurityLevel(abstractChat.getAccount(),
								abstractChat.getUser());
				SubMenu otrMenu = menu
						.addSubMenu(getText(R.string.otr_encryption));
				otrMenu.setHeaderTitle(R.string.otr_encryption);
				if (securityLevel == SecurityLevel.plain)
					otrMenu.add(0, OPTION_MENU_START_OTR_ID, 0,
							getText(R.string.otr_start))
							.setEnabled(
									SettingsManager.securityOtrMode() != SecurityOtrMode.disabled);
				else
					otrMenu.add(0, OPTION_MENU_REFRESH_OTR_ID, 0,
							getText(R.string.otr_refresh));
				otrMenu.add(0, OPTION_MENU_END_OTR_ID, 0,
						getText(R.string.otr_end)).setEnabled(
						securityLevel != SecurityLevel.plain);
				otrMenu.add(0, OPTION_MENU_VERIFY_FINGERPRINT_ID, 0,
						getText(R.string.otr_verify_fingerprint)).setEnabled(
						securityLevel != SecurityLevel.plain);
				otrMenu.add(0, OPTION_MENU_VERIFY_QUESTION_ID, 0,
						getText(R.string.otr_verify_question)).setEnabled(
						securityLevel != SecurityLevel.plain);
				otrMenu.add(0, OPTION_MENU_VERIFY_SECRET_ID, 0,
						getText(R.string.otr_verify_secret)).setEnabled(
						securityLevel != SecurityLevel.plain);
			}
			// if (abstractChat != null
			// && abstractChat instanceof RoomChat
			// && ((RoomChat) abstractChat).getState() == RoomState.available) {
			//
			// // Solo muestra esta opcion si hay algún ocupante en la sala,
			// // además de él mismo.
			// List<Occupant> ocupants = new ArrayList<Occupant>();
			// ocupants.addAll(MUCManager.getInstance().getOccupants(
			// actionWithAccount, actionWithUser));
			// if (ocupants.size() > 1) {
			// // menu.add(0, OPTION_MENU_OCCUPANT_LIST_ID, 0, getResources()
			// // .getText(R.string.occupant_list));
			// }
			// }
		}

		updateMenuTitles();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		HashMap<String, Serializable> params;
		IDialogAction iDialogAction;
		DialogGeneric dialog;
		String fileName;
		switch (item.getItemId()) {
		// case R.id.action_setting:
		// startActivity(ContactEditor.createIntent(this, actionWithAccount,
		// actionWithUser));
		// break;
		// case R.id.action_setting_con:
		// startActivity(ChatEditor.createIntent(this, actionWithAccount,
		// actionWithUser));
		//
		// break;

		case DIALOG_DELETE_CONTACT_ID:
			if (MUCManager.getInstance().hasRoom(actionWithAccount,
					actionWithUser)) {

				isModerator = false;
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
				Integer resource = null;
				if (isModerator) {
					resource = R.string.muc_remove_confirm;
				} else {
					resource = R.string.muc_delete_confirm;
				}

				AlertDialog.Builder builderRemove = new AlertDialog.Builder(
						this);
				builderRemove.setMessage(getString(resource,
						actionWithUser.split("@")[0]));
				builderRemove.setNegativeButton(R.string.cancelar,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});
				builderRemove.setPositiveButton(R.string.aceptar,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// borrar conferencia

								MUCManager.getInstance().removeRoom(
										actionWithAccount, actionWithUser);
								MessageManager.getInstance().closeChat(
										actionWithAccount, actionWithUser);

								NotificationManager.getInstance()
										.removeMessageNotification(
												actionWithAccount,
												actionWithUser);

								if (isModerator) {

									// Destruir / eliminar conferencia
									MUCManager.getInstance().destroyRoom(
											actionWithAccount, actionWithUser);
								}

								close();
							}
						});
				builderRemove.show();

			}

		case R.id.action_show_history:
			MessageManager.getInstance().requestToLoadLocalHistory(
					actionWithAccount, actionWithUser);
			MessageArchiveManager.getInstance().requestHistory(
					actionWithAccount, actionWithUser,
					MINIMUM_MESSAGES_TO_LOAD, 0);
			chatViewerAdapter.onChange();
			chatViewerAdapter.onChatChange(actionWithView, false);
			break;
		case R.id.action_clear_history:

			params = new HashMap<String, Serializable>();

			params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
					getString(R.string.clear_history));
			params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
					getString(R.string.sure_clear_history));
			params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
					getString(R.string.aceptar));
			params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
					getString(R.string.cancelar));

			iDialogAction = new IDialogAction() {

				@Override
				public void onPositiveButton() {
					actionClearHistory();
				}

				@Override
				public void onNegativeButton() {

				}
			};
			params.put(DialogGeneric.PARAMS_DIALOG_ACTION_INTERFACE,
					iDialogAction);

			dialog = new DialogGeneric(ChatViewer.this, params);

			dialog.createDialog();
			dialog.showDialog();

			break;

		case R.id.action_edit_contact:
			startActivity(ContactEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			break;

		case R.id.action_send_file:

			// openDialogTakeFile();

			// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			// intent.setType("file/*");
			// startActivityForResult(intent, PICKFILE_RESULT_CODE);

			showFileChooser();

			// AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// builder.setTitle(getString(R.string.enviar_archivo));
			// builder.setMessage(R.string.proximamente);
			// builder.setPositiveButton(getString(R.string.aceptar),
			// new OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// dialog.dismiss();
			// }
			// });
			// builder.show();

			break;
		case OPTION_MENU_VIEW_CONTACT_ID:
			startActivity(ContactEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case OPTION_MENU_CHAT_LIST_ID:
			startActivity(ChatList.createIntent(this));
			return true;
		case R.id.close_chat:
			params = new HashMap<String, Serializable>();
			params.put(DialogGeneric.PARAMS_DIALOG_TITLE,
					getString(R.string.close_chat));
			params.put(DialogGeneric.PARAMS_DIALOG_MESSAGE,
					getString(R.string.sure_close_chat));
			params.put(DialogGeneric.PARAMS_DIALOG_POSITIVE_BUTTON,
					getString(R.string.aceptar));
			params.put(DialogGeneric.PARAMS_DIALOG_NEGATIVE_BUTTON,
					getString(R.string.cancelar));

			iDialogAction = new IDialogAction() {

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

			dialog = new DialogGeneric(ChatViewer.this, params);

			dialog.createDialog();
			dialog.showDialog();

			return true;

		case R.id.silence:

			Uri uri = ChatManager.getInstance().getSound(actionWithAccount,
					actionWithUser);

			if (uri != null) {
				ChatManager.getInstance().setSound(actionWithAccount,
						actionWithUser, null);
			} else {
				ChatManager
						.getInstance()
						.setSound(
								actionWithAccount,
								actionWithUser,
								RingtoneManager
										.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
			}

			updateMenuTitles();

			// startActivity(ChatEditor.createIntent(this, actionWithAccount,
			// actionWithUser));

			break;

		case OPTION_MENU_CLOSE_CHAT_ID:

			if (MUCManager.getInstance().hasRoom(actionWithAccount,
					actionWithUser)) {

				isModerator = false;
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
				Integer resource = null;
				if (isModerator) {
					resource = R.string.muc_remove_confirm;
				} else {
					resource = R.string.muc_delete_confirm;
				}

				AlertDialog.Builder builderRemove = new AlertDialog.Builder(
						this);
				builderRemove.setMessage(getString(resource,
						actionWithUser.split("@")[0]));
				builderRemove.setNegativeButton(R.string.cancelar,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});
				builderRemove.setPositiveButton(R.string.aceptar,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// borrar conferencia

								MUCManager.getInstance().removeRoom(
										actionWithAccount, actionWithUser);
								MessageManager.getInstance().closeChat(
										actionWithAccount, actionWithUser);

								NotificationManager.getInstance()
										.removeMessageNotification(
												actionWithAccount,
												actionWithUser);

								if (isModerator) {

									// Destruir / eliminar conferencia
									MUCManager.getInstance().destroyRoom(
											actionWithAccount, actionWithUser);
								}

								close();
							}
						});
				builderRemove.show();

			} else {
				MessageManager.getInstance().closeChat(actionWithAccount,
						actionWithUser);
				NotificationManager.getInstance().removeMessageNotification(
						actionWithAccount, actionWithUser);
				close();
			}

			return true;
		case OPTION_MENU_CLEAR_HISTORY_ID:
			MessageManager.getInstance().clearHistory(actionWithAccount,
					actionWithUser);
			chatViewerAdapter.onChatChange(actionWithView, false);
			return true;
		case OPTION_MENU_SHOW_HISTORY_ID:
			MessageManager.getInstance().requestToLoadLocalHistory(
					actionWithAccount, actionWithUser);
			MessageArchiveManager.getInstance().requestHistory(
					actionWithAccount, actionWithUser,
					MINIMUM_MESSAGES_TO_LOAD, 0);
			chatViewerAdapter.onChange();
			chatViewerAdapter.onChatChange(actionWithView, false);
			return true;

		case OPTION_MENU_SEND_FILE_ID:

			// Thread th1 = new Thread(new Runnable() {
			// @Override
			// public void run() {

			// final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
			// this);
			// alertDialog.setTitle(getResources().getText(R.string.contact_send));
			// alertDialog.setMessage(R.string.where_choose);
			//
			// alertDialog.setNegativeButton(
			// getResources().getText(R.string.camera),
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			// openCamera();
			// }
			// });
			// alertDialog.setPositiveButton(
			// getResources().getText(R.string.gallery),
			// new DialogInterface.OnClickListener() {
			// public void onClick(DialogInterface dialog,
			// int whichButton) {
			//
			// showFileChooser();
			// }
			// });
			// alertDialog.show();

			// }
			// });
			// th1.start();

			break;
		case OPTION_MENU_CLEAR_MESSAGE_ID:
			((EditText) actionWithView.findViewById(R.id.chat_input))
					.setText("");
			return true;
		case OPTION_MENU_EXPORT_CHAT_ID:
			// showDialog(DIALOG_EXPORT_CHAT_ID);

			fileName = getString(
					R.string.export_chat_mask,
					AccountManager.getInstance().getVerboseName(
							actionWithAccount.split("@")[0]),
					RosterManager.getInstance()
							.getName(actionWithAccount, actionWithUser)
							.split("@")[0]);

			exportChat(fileName);

			return true;
		case OPTION_MENU_CALL_ATTENTION_ID:
			try {
				AttentionManager.getInstance().sendAttention(actionWithAccount,
						actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;
		case OPTION_MENU_JOIN_ROOM_ID:
			MUCManager.getInstance().joinRoom(actionWithAccount,
					actionWithUser, true);
			return true;
		case OPTION_MENU_LEAVE_ROOM_ID:

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

			// ABANDONAR CONFERENCIA

			MUCManager.getInstance().leaveRoom(actionWithAccount,
					actionWithUser);
			MessageManager.getInstance().closeChat(actionWithAccount,
					actionWithUser);
			NotificationManager.getInstance().removeMessageNotification(
					actionWithAccount, actionWithUser);
			close();
			// }
			// });
			// builderRemove.show();

			return true;
		case OPTION_MENU_MUC_INVITE_ID:
			startActivity(ContactList.createRoomInviteIntent(this,
					actionWithAccount, actionWithUser));
			return true;
		case OPTION_MENU_EDIT_ROOM_ID:
			startActivity(MUCEditor.createIntent(this, actionWithAccount,
					actionWithUser));
			return true;
		case OPTION_MENU_OCCUPANT_LIST_ID:

			startActivity(OccupantList.createIntent(this, actionWithAccount,
					actionWithUser));

			return true;
		case OPTION_MENU_START_OTR_ID:
			try {
				OTRManager.getInstance().startSession(actionWithAccount,
						actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;
		case OPTION_MENU_REFRESH_OTR_ID:
			try {
				OTRManager.getInstance().refreshSession(actionWithAccount,
						actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;
		case OPTION_MENU_END_OTR_ID:
			try {
				OTRManager.getInstance().endSession(actionWithAccount,
						actionWithUser);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return true;
		case OPTION_MENU_VERIFY_FINGERPRINT_ID:
			startActivity(FingerprintViewer.createIntent(this,
					actionWithAccount, actionWithUser));
			return true;
		case OPTION_MENU_VERIFY_QUESTION_ID:
			startActivity(QuestionViewer.createIntent(this, actionWithAccount,
					actionWithUser, true, false, null));
			return true;
		case OPTION_MENU_VERIFY_SECRET_ID:
			startActivity(QuestionViewer.createIntent(this, actionWithAccount,
					actionWithUser, false, false, null));
			return true;

			// case R.id.action_send:
			//
			// openDialogTakeFile();
			//
			// break;

		case R.id.action_send_con:
			// showDialog(DIALOG_SEND_CHAT_ID);

			fileName = getString(
					R.string.export_chat_mask,
					AccountManager.getInstance()
							.getVerboseName(actionWithAccount).split("@")[0],
					RosterManager.getInstance()
							.getName(actionWithAccount, actionWithUser)
							.split("@")[0]);
			sendChat(fileName);
			break;

		case R.id.action_send_email:

			contactManager.sendEmail(actionWithUser);

			break;

		case R.id.action_call_phones:

			if (phones != null && phones.size() > 0) {

				if (phones.size() > 1) {
					contactManager.createDialogPhones(phones);
				} else {
					contactManager.callPhone(phones.get(0));
				}
			}

			break;
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	private void actionClearHistory() {
		MessageManager.getInstance().clearHistory(actionWithAccount,
				actionWithUser);
		chatViewerAdapter.onChatChange(actionWithView, false);
	}

	private void actionCloseChat() {

		MessageManager.getInstance().closeChat(actionWithAccount,
				actionWithUser);
		NotificationManager.getInstance().removeMessageNotification(
				actionWithAccount, actionWithUser);
		close();

	}

	// private void openDialogTakeFile() {
	//
	// final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	// alert.setTitle(getResources().getText(R.string.contact_send));
	// alert.setMessage(R.string.where_choose);
	//
	// alert.setNegativeButton(getResources().getText(R.string.camera),
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int whichButton) {
	// openCamera();
	// }
	// });
	// alert.setPositiveButton(getResources().getText(R.string.gallery),
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int whichButton) {
	//
	// showFileChooser();
	// }
	// });
	// alert.show();
	//
	// }

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// super.onOptionsItemSelected(item);
	// switch (item.getItemId()) {
	// case OPTION_MENU_VIEW_CONTACT_ID:
	// startActivity(ContactEditor.createIntent(this, actionWithAccount,
	// actionWithUser));
	// return true;
	// case OPTION_MENU_CHAT_LIST_ID:
	// startActivity(ChatList.createIntent(this));
	// return true;
	// case OPTION_MENU_CLOSE_CHAT_ID:
	// MessageManager.getInstance().closeChat(actionWithAccount,
	// actionWithUser);
	// NotificationManager.getInstance().removeMessageNotification(
	// actionWithAccount, actionWithUser);
	// close();
	// return true;
	// case OPTION_MENU_CLEAR_HISTORY_ID:
	// MessageManager.getInstance().clearHistory(actionWithAccount,
	// actionWithUser);
	// chatViewerAdapter.onChatChange(actionWithView, false);
	// return true;
	// case OPTION_MENU_SHOW_HISTORY_ID:
	// MessageManager.getInstance().requestToLoadLocalHistory(
	// actionWithAccount, actionWithUser);
	// MessageArchiveManager.getInstance().requestHistory(
	// actionWithAccount, actionWithUser,
	// MINIMUM_MESSAGES_TO_LOAD, 0);
	// chatViewerAdapter.onChange();
	// chatViewerAdapter.onChatChange(actionWithView, false);
	// return true;
	// case OPTION_MENU_SETTINGS_ID:
	// startActivity(ChatEditor.createIntent(this, actionWithAccount,
	// actionWithUser));
	// return true;
	//
	// case OPTION_MENU_SEND_FILE_ID:
	//
	// // Thread th1 = new Thread(new Runnable() {
	// // @Override
	// // public void run() {
	//
	// final AlertDialog.Builder alert = new AlertDialog.Builder(this);
	// alert.setTitle(getResources().getText(R.string.contact_send));
	// alert.setMessage(R.string.where_choose);
	//
	// alert.setNegativeButton(getResources().getText(R.string.camera),
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	// openCamera();
	// }
	// });
	// alert.setPositiveButton(getResources().getText(R.string.gallery),
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int whichButton) {
	//
	// showFileChooser();
	// }
	// });
	// alert.show();
	//
	// // }
	// // });
	// // th1.start();
	//
	// break;
	// case OPTION_MENU_CLEAR_MESSAGE_ID:
	// ((EditText) actionWithView.findViewById(R.id.chat_input))
	// .setText("");
	// return true;
	// case OPTION_MENU_EXPORT_CHAT_ID:
	// showDialog(DIALOG_EXPORT_CHAT_ID);
	// return true;
	// case OPTION_MENU_CALL_ATTENTION_ID:
	// try {
	// AttentionManager.getInstance().sendAttention(actionWithAccount,
	// actionWithUser);
	// } catch (NetworkException e) {
	// Application.getInstance().onError(e);
	// }
	// return true;
	// case OPTION_MENU_JOIN_ROOM_ID:
	// MUCManager.getInstance().joinRoom(actionWithAccount,
	// actionWithUser, true);
	// return true;
	// case OPTION_MENU_LEAVE_ROOM_ID:
	// MUCManager.getInstance().leaveRoom(actionWithAccount,
	// actionWithUser);
	// MessageManager.getInstance().closeChat(actionWithAccount,
	// actionWithUser);
	// NotificationManager.getInstance().removeMessageNotification(
	// actionWithAccount, actionWithUser);
	// close();
	// return true;
	// case OPTION_MENU_MUC_INVITE_ID:
	// startActivity(ContactList.createRoomInviteIntent(this,
	// actionWithAccount, actionWithUser));
	// return true;
	// case OPTION_MENU_EDIT_ROOM_ID:
	// startActivity(MUCEditor.createIntent(this, actionWithAccount,
	// actionWithUser));
	// return true;
	// case OPTION_MENU_OCCUPANT_LIST_ID:
	// startActivity(OccupantList.createIntent(this, actionWithAccount,
	// actionWithUser));
	// return true;
	// case OPTION_MENU_START_OTR_ID:
	// try {
	// OTRManager.getInstance().startSession(actionWithAccount,
	// actionWithUser);
	// } catch (NetworkException e) {
	// Application.getInstance().onError(e);
	// }
	// return true;
	// case OPTION_MENU_REFRESH_OTR_ID:
	// try {
	// OTRManager.getInstance().refreshSession(actionWithAccount,
	// actionWithUser);
	// } catch (NetworkException e) {
	// Application.getInstance().onError(e);
	// }
	// return true;
	// case OPTION_MENU_END_OTR_ID:
	// try {
	// OTRManager.getInstance().endSession(actionWithAccount,
	// actionWithUser);
	// } catch (NetworkException e) {
	// Application.getInstance().onError(e);
	// }
	// return true;
	// case OPTION_MENU_VERIFY_FINGERPRINT_ID:
	// startActivity(FingerprintViewer.createIntent(this,
	// actionWithAccount, actionWithUser));
	// return true;
	// case OPTION_MENU_VERIFY_QUESTION_ID:
	// startActivity(QuestionViewer.createIntent(this, actionWithAccount,
	// actionWithUser, true, false, null));
	// return true;
	// case OPTION_MENU_VERIFY_SECRET_ID:
	// startActivity(QuestionViewer.createIntent(this, actionWithAccount,
	// actionWithUser, false, false, null));
	// return true;
	// }
	// return false;
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.clear();
		AbstractChat abstractChat = MessageManager.getInstance().getChat(
				actionWithAccount, actionWithUser);
		if (abstractChat == null || !(abstractChat instanceof RoomChat)) {
			getMenuInflater().inflate(R.menu.action_bar, menu);
		} else {
			if (abstractChat != null && abstractChat instanceof RoomChat) {
				if (((RoomChat) abstractChat).getState() == RoomState.unavailable)
					menu.add(0, OPTION_MENU_JOIN_ROOM_ID, 0,
							getResources().getText(R.string.muc_join)).setIcon(
							R.drawable.menu_icono_unirse_sala);
				else
					menu.add(0, OPTION_MENU_MUC_INVITE_ID, 0,
							getResources().getText(R.string.muc_invite))
							.setIcon(R.drawable.menu_icono_invitar_usuario);
			}

			// else {
			// menu.add(0, OPTION_MENU_VIEW_CONTACT_ID, 0,
			// getResources().getText(R.string.contact_editor))
			// .setIcon(android.R.drawable.ic_menu_edit);
			// menu.add(0, OPTION_MENU_VIEW_CONTACT_ID, 0,
			// getResources().getText(R.string.contact_editor))
			// .setIcon(android.R.drawable.ic_secure);
			// }
			// menu.add(0, OPTION_MENU_SEND_FILE_ID, 0,
			// getResources().getText(R.string.contact_send)).setIcon(
			// R.drawable.ic_menu_friendslist);
			// menu.add(0, OPTION_MENU_SETTINGS_ID, 0,
			// getText(R.string.chat_settings)).setIcon(
			// android.R.drawable.ic_menu_preferences);
			menu.add(0, OPTION_MENU_SHOW_HISTORY_ID, 0,
					getText(R.string.show_history)).setIcon(
					R.drawable.menu_icono_mostrar_historial);
			if (abstractChat != null
					&& abstractChat instanceof RoomChat
					&& ((RoomChat) abstractChat).getState() != RoomState.unavailable) {
				if (((RoomChat) abstractChat).getState() == RoomState.error) {
					// menu.add(0, OPTION_MENU_EDIT_ROOM_ID, 0,
					// getResources().getText(R.string.muc_edit)).setIcon(
					// android.R.drawable.ic_menu_edit);
				} else
					menu.add(0, OPTION_MENU_LEAVE_ROOM_ID, 0,
							getResources().getText(R.string.muc_leave))
							.setIcon(R.drawable.menu_icono_abandonar_sala);
			} else {
				menu.add(0, OPTION_MENU_CLOSE_CHAT_ID, 0,
						getResources().getText(R.string.close_chat)).setIcon(
						R.drawable.menu_icono_eliminar_sala);
			}
			// menu.add(0, OPTION_MENU_CLEAR_MESSAGE_ID, 0,
			// getResources().getText(R.string.clear_message)).setIcon(
			// R.drawable.ic_menu_stop);
			menu.add(0, OPTION_MENU_CLEAR_HISTORY_ID, 0,
					getText(R.string.clear_history)).setIcon(
					R.drawable.menu_icono_limpiar_historial);
			// menu.add(0, OPTION_MENU_EXPORT_CHAT_ID, 0,
			// getText(R.string.export_chat));
			if (abstractChat != null && abstractChat instanceof RegularChat) {
				menu.add(0, OPTION_MENU_CALL_ATTENTION_ID, 0,
						getText(R.string.call_attention));
				SecurityLevel securityLevel = OTRManager.getInstance()
						.getSecurityLevel(abstractChat.getAccount(),
								abstractChat.getUser());
				SubMenu otrMenu = menu
						.addSubMenu(getText(R.string.otr_encryption));
				otrMenu.setHeaderTitle(R.string.otr_encryption);
				if (securityLevel == SecurityLevel.plain)
					otrMenu.add(0, OPTION_MENU_START_OTR_ID, 0,
							getText(R.string.otr_start))
							.setEnabled(
									SettingsManager.securityOtrMode() != SecurityOtrMode.disabled);
				else
					otrMenu.add(0, OPTION_MENU_REFRESH_OTR_ID, 0,
							getText(R.string.otr_refresh));
				otrMenu.add(0, OPTION_MENU_END_OTR_ID, 0,
						getText(R.string.otr_end)).setEnabled(
						securityLevel != SecurityLevel.plain);
				otrMenu.add(0, OPTION_MENU_VERIFY_FINGERPRINT_ID, 0,
						getText(R.string.otr_verify_fingerprint)).setEnabled(
						securityLevel != SecurityLevel.plain);
				otrMenu.add(0, OPTION_MENU_VERIFY_QUESTION_ID, 0,
						getText(R.string.otr_verify_question)).setEnabled(
						securityLevel != SecurityLevel.plain);
				otrMenu.add(0, OPTION_MENU_VERIFY_SECRET_ID, 0,
						getText(R.string.otr_verify_secret)).setEnabled(
						securityLevel != SecurityLevel.plain);
			}
			// if (abstractChat != null
			// && abstractChat instanceof RoomChat
			// && ((RoomChat) abstractChat).getState() == RoomState.available)
			// menu.add(0, OPTION_MENU_OCCUPANT_LIST_ID, 0, getResources()
			// .getText(R.string.occupant_list));
		}
		return true;
	}

	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View view,
	// ContextMenuInfo menuInfo) {
	// super.onCreateContextMenu(menu, view, menuInfo);
	// AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	// pageSwitcher.stopMovement();
	//
	// ListView listView = (ListView) actionWithView.findViewById(R.id.list);
	//
	// listView.setDivider(this.getResources().getDrawable(
	// android.R.color.transparent));
	// listView.setDividerHeight(20);
	//
	// actionWithMessage = (MessageItem) listView.getAdapter().getItem(
	// info.position);
	// if (actionWithMessage != null && actionWithMessage.getAction() != null)
	// actionWithMessage = null; // Skip action message
	// if (actionWithMessage == null)
	// return;
	// if (actionWithMessage.isError()) {
	// menu.add(0, CONTEXT_MENU_REPEAT_ID, 0,
	// getResources().getText(R.string.message_repeat));
	// }
	// menu.add(0, CONTEXT_MENU_QUOTE_ID, 0,
	// getResources().getText(R.string.message_quote));
	// menu.add(0, CONTEXT_MENU_COPY_ID, 0,
	// getResources().getText(R.string.message_copy));
	// menu.add(0, CONTEXT_MENU_REMOVE_ID, 0,
	// getResources().getText(R.string.message_remove));
	// }

	/**
	 * Insert additional text to the input.
	 * 
	 * @param additional
	 */
	private void insertText(String additional) {
		EditText editView = (EditText) actionWithView
				.findViewById(R.id.chat_input);
		String source = editView.getText().toString();
		int selection = editView.getSelectionEnd();
		if (selection == -1)
			selection = source.length();
		else if (selection > source.length())
			selection = source.length();
		String before = source.substring(0, selection);
		String after = source.substring(selection);
		if (before.length() > 0 && !before.endsWith("\n"))
			additional = "\n" + additional;
		editView.setText(before + additional + after);
		editView.setSelection(selection + additional.length());
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (actionWithMessage == null)
			return false;
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case CONTEXT_MENU_QUOTE_ID:
			insertText("> " + actionWithMessage.getText() + "\n");
			return true;
		case CONTEXT_MENU_REPEAT_ID:
			sendMessage(actionWithMessage.getText());
			return true;
		case CONTEXT_MENU_COPY_ID:
			((ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
					.setText(actionWithMessage.getSpannable());
			return true;
		case CONTEXT_MENU_REMOVE_ID:
			MessageManager.getInstance().removeMessage(actionWithMessage);
			chatViewerAdapter.onChatChange(actionWithView, false);
			return true;
		}
		return false;
	}

	// @Override
	// protected Dialog onCreateDialog(int id) {
	// super.onCreateDialog(id);
	// switch (id) {
	// case DIALOG_EXPORT_CHAT_ID:
	//
	// String fileName = getString(
	// R.string.export_chat_mask,
	// AccountManager.getInstance().getVerboseName(
	// actionWithAccount), RosterManager.getInstance()
	// .getName(actionWithAccount, actionWithUser));
	//
	// // return new ExportChatDialogBuilder(this, DIALOG_EXPORT_CHAT_ID,
	// // this, actionWithAccount.split("@")[0],
	// // actionWithUser.split("@")[0]).create();
	//
	// case DIALOG_SEND_CHAT_ID:
	//
	// String fileName = getString(
	// R.string.export_chat_mask,
	// AccountManager.getInstance().getVerboseName(
	// actionWithAccount), RosterManager.getInstance()
	// .getName(actionWithAccount, actionWithUser));
	//
	// // return new ExportChatDialogBuilder(this, DIALOG_SEND_CHAT_ID,
	// // this,
	// // actionWithAccount.split("@")[0],
	// // actionWithUser.split("@")[0]).create();
	//
	// default:
	// return null;
	// }
	// }

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.chat_send:
			if (!checkRecord()) {
				sendMessage();
			}
			break;
		case R.id.title:
			ListView listView = (ListView) actionWithView
					.findViewById(R.id.list);
			listView.setDivider(this.getResources().getDrawable(
					android.R.color.transparent));
			listView.setDividerHeight(20);
			int size = listView.getCount();
			if (size > 0)
				listView.setSelection(size - 1);

			break;
		// case R.id.img_send:
		// openDialogTakeFile();
		// break;
		// case R.id.img_settings:
		// openOptionsMenu();
		// break;
		default:
			break;
		}
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& keyCode == KeyEvent.KEYCODE_ENTER
				&& SettingsManager.chatsSendByEnter()) {
			sendMessage();
			return true;
		}
		return false;
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	//
	// ChatGroup.chatGroup.back();
	//
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	// private void close() {
	// // finish();
	// // if (!Intent.ACTION_SEND.equals(getIntent().getAction())) {
	// // ActivityManager.getInstance().clearStack(false);
	// // if (!ActivityManager.getInstance().hasContactList(this))
	// // startActivity(ContactList.createIntent(this));
	// // }
	//
	// ChatGroup.chatGroup.back();
	// }

	@Override
	public void onTextChanged(EditText editText, CharSequence text) {
		ChatStateManager.getInstance().onComposing(actionWithAccount,
				actionWithUser, text);
	}

	@Override
	public void onSelect() {
		try {
			BaseEntity contactItem = (BaseEntity) pageSwitcher
					.getSelectedItem();
			actionWithAccount = contactItem.getAccount();
			actionWithUser = contactItem.getUser();
			if (PageSwitcher.LOG)
				LogManager.i(this, "onSelect: " + actionWithAccount + ":"
						+ actionWithUser);
			actionWithView = pageSwitcher.getSelectedView();
			actionWithMessage = null;
			if (isVisible)
				MessageManager.getInstance().setVisibleChat(actionWithAccount,
						actionWithUser);
			MessageArchiveManager.getInstance().requestHistory(
					actionWithAccount,
					actionWithUser,
					0,
					MessageManager.getInstance()
							.getChat(actionWithAccount, actionWithUser)
							.getRequiredMessageCount());
			NotificationManager.getInstance().removeMessageNotification(
					actionWithAccount, actionWithUser);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUnselect() {
		actionWithAccount = null;
		actionWithUser = null;
		actionWithView = null;
		actionWithMessage = null;
		if (PageSwitcher.LOG)
			LogManager.i(this, "onUnselect");
	}

	private void sendMessage() {
		if (actionWithView == null)
			return;
		EditText editView = (EditText) actionWithView
				.findViewById(R.id.chat_input);
		String text = editView.getText().toString();
		int start = 0;
		int end = text.length();
		while (start < end
				&& (text.charAt(start) == ' ' || text.charAt(start) == '\n'))
			start += 1;
		while (start < end
				&& (text.charAt(end - 1) == ' ' || text.charAt(end - 1) == '\n'))
			end -= 1;
		text = text.substring(start, end);
		if ("".equals(text))
			return;
		chatViewerAdapter.setOnTextChangedListener(null);
		editView.setText("");
		chatViewerAdapter.setOnTextChangedListener(this);
		sendMessage(text);
		if (exitOnSend)
			close();
		if (SettingsManager.chatsHideKeyboard() == ChatsHideKeyboard.always
				|| (getResources().getBoolean(R.bool.landscape) && SettingsManager
						.chatsHideKeyboard() == ChatsHideKeyboard.landscape)) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editView.getWindowToken(), 0);
		}
	}

	private void sendMessage(String text) {
		MessageManager.getInstance().sendMessage(actionWithAccount,
				actionWithUser, text);
		chatViewerAdapter.onChatChange(actionWithView, false);
	}

	// private void sendMessageFile(String text, String pathFile) {
	// MessageManager.getInstance().sendMessageFile(actionWithAccount,
	// actionWithUser, text, pathFile);
	// chatViewerAdapter.onChatChange(actionWithView, false);
	// }osenFi

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			close();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void close() {
		finish();
		if (!Intent.ACTION_SEND.equals(getIntent().getAction())) {
			ActivityManager.getInstance().clearStack(false);
			if (!ActivityManager.getInstance().hasContactList(this))
				startActivity(ContactList.createIntent(this));
		}
	}

	@Override
	public void onChatChanged(final String account, final String user,
			final boolean incoming) {
		BaseEntity baseEntity;
		baseEntity = (BaseEntity) pageSwitcher.getSelectedItem();
		if (baseEntity != null && baseEntity.equals(account, user)) {
			chatViewerAdapter.onChatChange(pageSwitcher.getSelectedView(),
					incoming);
			return;
		}
		baseEntity = (BaseEntity) pageSwitcher.getVisibleItem();
		if (baseEntity != null && baseEntity.equals(account, user)) {
			chatViewerAdapter.onChatChange(pageSwitcher.getVisibleView(),
					incoming);
			return;
		}
		// Search for chat in adapter.
		final int count = chatViewerAdapter.getCount();
		for (int index = 0; index < count; index++)
			if (((BaseEntity) chatViewerAdapter.getItem(index)).equals(account,
					user))
				return;
		// New chat.
		chatViewerAdapter.onChange();
	}

	@Override
	public void onContactsChanged(Collection<BaseEntity> entities) {
		BaseEntity baseEntity;
		baseEntity = (BaseEntity) pageSwitcher.getSelectedItem();
		if (baseEntity != null && entities.contains(baseEntity)) {
			chatViewerAdapter.onChange();
			return;
		}
		baseEntity = (BaseEntity) pageSwitcher.getVisibleItem();
		if (baseEntity != null && entities.contains(baseEntity)) {
			chatViewerAdapter.onChange();
			return;
		}
	}

	@Override
	public void onAccountsChanged(Collection<String> accounts) {
		BaseEntity baseEntity;
		baseEntity = (BaseEntity) pageSwitcher.getSelectedItem();
		if (baseEntity != null && accounts.contains(baseEntity.getAccount())) {
			chatViewerAdapter.onChange();
			return;
		}
		baseEntity = (BaseEntity) pageSwitcher.getVisibleItem();
		if (baseEntity != null && accounts.contains(baseEntity.getAccount())) {
			chatViewerAdapter.onChange();
			return;
		}
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEND) {
			sendMessage();
			return true;
		}
		return false;
	}

	// @Override
	// public void onAccept(DialogBuilder dialogBuilder) {
	// ExportChatDialogBuilder builder = null;
	// switch (dialogBuilder.getDialogId()) {
	// case DIALOG_EXPORT_CHAT_ID:
	// builder = (ExportChatDialogBuilder) dialogBuilder;
	// exportChat(builder);
	// break;
	// case DIALOG_SEND_CHAT_ID:
	// // builder = (ExportChatDialogBuilder) dialogBuilder;
	// // sendChat(builder);
	// break;
	//
	// }
	// }

	private void sendChat(String fileName) {
		new ChatSendAsyncTask(fileName).execute();
	}

	private void exportChat(String fileName) {
		new ChatExportAsyncTask(fileName).execute();
	}

	private class ChatExportAsyncTask extends AsyncTask<Void, Void, File> {
		private String fileName;

		public ChatExportAsyncTask(String fileName) {
			this.fileName = fileName;
		}

		@Override
		protected File doInBackground(Void... params) {
			File file = null;
			try {
				file = MessageManager.getInstance().exportChat(
						actionWithAccount, actionWithUser, fileName);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return file;
		}

		@Override
		public void onPostExecute(File result) {
			if (result != null) {
				// if (builder.isSendChecked()) {
				// Intent intent = new Intent(
				// android.content.Intent.ACTION_SEND);
				// intent.setType("text/plain");
				// Uri uri = Uri.fromFile(result);
				// intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
				// startActivity(Intent.createChooser(intent,
				// getString(R.string.export_chat)));
				// } else {
				Toast.makeText(ChatViewer.this, R.string.export_chat_done,
						Toast.LENGTH_LONG).show();
				// }
			}
		}

	}

	private class ChatSendAsyncTask extends AsyncTask<Void, Void, File> {
		private String fileName;

		public ChatSendAsyncTask(String fileName) {
			this.fileName = fileName;
		}

		@Override
		protected File doInBackground(Void... params) {
			File file = null;
			try {
				file = MessageManager.getInstance().exportChat(
						actionWithAccount, actionWithUser, fileName);
			} catch (NetworkException e) {
				Application.getInstance().onError(e);
			}
			return file;
		}

		@Override
		public void onPostExecute(File result) {
			if (result != null) {
				// Intent intent = new
				// Intent(android.content.Intent.ACTION_SENDTO);
				// intent.setType("text/plain");
				// intent.putExtra(Intent.EXTRA_SUBJECT,
				// getString(R.string.subject_email));
				// intent.setData(Uri.parse("mailto:")); // only email apps
				// should
				// // handle this
				Uri uri = Uri.fromFile(result);
				// intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
				// startActivity(Intent.createChooser(intent,
				// getString(R.string.export_chat)));

				EmailManager.sendFile(getApplicationContext(),
						getString(R.string.subject_email), uri.getPath());
			}
		}
	}

	// @Override
	// public void onDecline(DialogBuilder dialogBuilder) {
	// }
	//
	// @Override
	// public void onCancel(DialogBuilder dialogBuilder) {
	// }

	private boolean selectChat(String account, String user) {
		for (int position = 0; position < chatViewerAdapter.getCount(); position++)
			if (((BaseEntity) chatViewerAdapter.getItem(position)).equals(
					account, user)) {
				if (PageSwitcher.LOG)
					LogManager.i(this, "setSelection: " + position + ", "
							+ account + ":" + user);
				pageSwitcher.setSelection(position);
				return true;
			}
		if (PageSwitcher.LOG)
			LogManager.i(this, "setSelection: not found, " + account + ":"
					+ user);
		return false;
	}

	public static Intent createIntent(Context context, String account,
			String user) {
		return new EntityIntentBuilder(context, ChatViewer.class)
				.setAccount(account).setUser(user).build();
	}

	public static Intent createIntent(Context context, String account,
			String user, String name) {

		return new EntityIntentBuilder(context, ChatViewer.class)
				.setAccount(account).setUser(user).setName(name).build();
	}

	public static Intent createIntent(Context context, String account,
			String user, String name, MessageItem messageItem) {

		Intent intent = new EntityIntentBuilder(context, ChatViewer.class)
				.setAccount(account).setUser(user).setName(name).build();

		intent.putExtra(ContactList.EXTRA_FORWARD_MESSAGE, messageItem);

		return intent;
	}

	// public static Intent createIntentNotification(Context context,
	// String account, String user) {
	// return new EntityIntentBuilder(context, TabHostAct.class)
	// .setAccount(account).setUser(user).build();
	// }

	public static Intent createClearTopIntent(Context context, String account,
			String user) {
		Intent intent = createIntent(context, account, user);
		intent.putExtra(Constantes.ACCOUNT, account);
		intent.putExtra(Constantes.USER, user);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return intent;
	}

	// public static Intent createClearTopIntentNotification(Context context,
	// String account, String user) {
	// Intent intent = createIntentNotification(context, account, user);
	// intent.putExtra(Constantes.ACCOUNT, account);
	// intent.putExtra(Constantes.USER, user);
	// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// return intent;
	// }

	/**
	 * Create intent to send message.
	 * 
	 * Contact list will not be shown on when chat will be closed.
	 * 
	 * @param context
	 * @param account
	 * @param user
	 * @param text
	 *            if <code>null</code> then user will be able to send a number
	 *            of messages. Else only one message can be send.
	 * @return
	 */
	public static Intent createSendIntent(Context context, String account,
			String user, String text) {
		Intent intent = ChatViewer.createIntent(context, account, user);
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		return intent;
	}

	public static Intent createAttentionRequestIntent(Context context,
			String account, String user) {
		Intent intent = ChatViewer.createClearTopIntent(context, account, user);
		intent.setAction(ACTION_ATTENTION);
		return intent;
	}

	private static String getAccount(Intent intent) {
		String value = EntityIntentBuilder.getAccount(intent);
		if (value != null)
			return value;
		// Backward compatibility.
		return intent.getStringExtra("com.xabber.android.data.account");
	}

	private static String getUser(Intent intent) {
		String value = EntityIntentBuilder.getUser(intent);
		if (value != null)
			return value;
		// Backward compatibility.
		return intent.getStringExtra("com.xabber.android.data.user");
	}

	private static String getName(Intent intent) {
		String value = EntityIntentBuilder.getName(intent);
		if (value != null)
			return value;
		// Backward compatibility.
		return intent.getStringExtra("com.xabber.android.data.user");
	}

	private static MessageItem getMessageItem(Intent intent) {
		// Backward compatibility.
		return (MessageItem) intent
				.getSerializableExtra(ContactList.EXTRA_FORWARD_MESSAGE);
	}

	private static boolean hasAttention(Intent intent) {
		return ACTION_ATTENTION.equals(intent.getAction());
	}

	// private void openCamera() {
	// Intent cameraIntent = new Intent(
	// android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	//
	// // Calendar cal = Calendar.getInstance();
	// // Date date = cal.getTime();
	// // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
	// // new Locale("es", "ES"));
	//
	// // String nombreArchivo = sdf.format(date);
	// String nombreArchivo = Calendar.getInstance().getTimeInMillis()
	// + ".jpg";
	//
	// path = Environment.getExternalStorageDirectory() + File.separator + ""
	// + Constantes.NAME_FOLDER + "" + File.separator;
	// File pathF = new File(Environment.getExternalStorageDirectory()
	// + File.separator + "" + Constantes.NAME_FOLDER + ""
	// + File.separator);
	// pathF.mkdirs();
	// name = nombreArchivo + ".jpg";
	// File image = new File(pathF, nombreArchivo + ".jpg");
	//
	// // if (hasImageCaptureBug()) {
	// cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
	//
	// // cameraIntent.putExtra(MediaStore.Images.Media.TITLE,
	// // name);
	// // } else {
	// // cameraIntent
	// // .putExtra(
	// // android.provider.MediaStore.EXTRA_OUTPUT,
	// // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	// // }
	//
	// // cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
	// // Uri.fromFile(image));
	//
	// this.startActivityForResult(cameraIntent, FILE_SELECT_CODE_CAMERA);
	//
	// // Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
	// // startActivityForResult(intent, FILE_SELECT_CODE_CAMERA);
	// }

	private void showFileChooser() {

		// in onCreate or any event where your want the user to
		// select a file
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);

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
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			
			Uri uri;
			switch (requestCode) {
			case PICKFILE_RESULT_CODE:

				// Get the Uri of the selected file
				uri = data.getData();
				try {
					path = getPath(this, uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				startSendFile(path);

				break;
			case FILE_SELECT_CODE:

				// Get the Uri of the selected file
				uri = data.getData();

				try {
					path = getPath(this, uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}

				// If path equals null, we get the path by other way
				if (path == null) {

					// SDK < API11
					if (Build.VERSION.SDK_INT < 11)
						path = RealPathUtil.getRealPathFromURI_BelowAPI11(this,
								data.getData());

					// SDK >= 11 && SDK < 19
					else if (Build.VERSION.SDK_INT < 19)
						path = RealPathUtil.getRealPathFromURI_API11to18(this,
								data.getData());

					// SDK > 19 (Android 4.4)
					else
						path = RealPathUtil.getRealPathFromURI_API19(this,
								data.getData());
				}

				startSendFile(path);

				break;
			case FILE_SELECT_CODE_CAMERA:

				Uri chosenImageUri = null;
				File f = new File(this.path, name);
				chosenImageUri = Uri.fromFile(f);

				Bitmap bm = null;
				File file = new File(this.getCacheDir(), name);
				if (chosenImageUri != null) {

					try {
						bm = decodeUri(chosenImageUri);

						if (bm != null) {

							// create a file to write bitmap data

							file.createNewFile();

							// Convert bitmap to byte array
							Bitmap bitmap = bm;
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							bitmap.compress(CompressFormat.PNG, 0 /*
																 * ignored for
																 * PNG
																 */, bos);
							byte[] bitmapdata = bos.toByteArray();

							// write the bytes in file
							FileOutputStream fos = new FileOutputStream(file);
							fos.write(bitmapdata);

							fos.flush();
							fos.close();
						}

					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					startSendFile(file.getAbsolutePath());

				}

				break;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
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

	// public class SendFile extends AsyncTask<String, Void, Boolean> {
	//
	// String filenameWithPath;
	//
	// @Override
	// protected Boolean doInBackground(String... params) {
	//
	// if (params.length > 0) {
	// filenameWithPath = params[0];
	// }
	//
	// return sendFile(filenameWithPath);
	// }
	//
	// @Override
	// protected void onPostExecute(Boolean result) {
	// super.onPostExecute(result);
	//
	// if (result) {
	//
	// sendMessageFile("Enviado correctamente", filenameWithPath);
	// } else {
	// sendMessageFile("No se ha podido enviar, inténtalo de nuevo",
	// null);
	// }
	//
	// }
	//
	// }

	// public class SendFileCamera extends AsyncTask<String, Void, Boolean> {
	//
	// String filenameWithPath;
	// String filenameWithPathReal;
	//
	// @Override
	// protected Boolean doInBackground(String... params) {
	//
	// if (params.length > 0) {
	// filenameWithPath = params[0];
	// }
	// if (params.length > 1) {
	// filenameWithPathReal = params[1];
	// }
	//
	// return sendFile(filenameWithPath);
	// }
	//
	// @Override
	// protected void onPostExecute(Boolean result) {
	// super.onPostExecute(result);
	//
	// if (result && filenameWithPathReal != null)
	//
	// {
	// sendMessageFile("Enviado correctamente", filenameWithPathReal);
	// } else {
	// sendMessageFile("No se ha podido enviar, inténtalo de nuevo",
	// null);
	// }
	//
	// }
	//
	// }

	// private Boolean sendFile(String filenameWithPath) {
	//
	// Boolean result = true;
	//
	// String verbose = null;
	// ResourceItem resourceItem = PresenceManager.getInstance()
	// .getResourceItem(actionWithAccount, actionWithUser);
	//
	// if (resourceItem != null) {
	// verbose = resourceItem.getVerbose();
	// }
	//
	// if (verbose != null) {
	// try {
	// result = ConnectionManager.getInstance().sendPacketFile(
	// actionWithAccount, actionWithUser + "/" + verbose,
	// filenameWithPath);
	// } catch (NetworkException e) {
	// e.printStackTrace();
	// result = false;
	// }
	// } else {
	// result = false;
	// }
	//
	// return result;
	// }

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

		int desiredWidth = ANCHURA;
		int desiredHeight = ALTURA;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null,
				options);

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
			Bitmap sampledSrcBitmap = BitmapFactory.decodeStream(
					getContentResolver().openInputStream(selectedImage), null,
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
			Bitmap sampledSrcBitmap = BitmapFactory.decodeStream(
					getContentResolver().openInputStream(selectedImage), null,
					options);

			Matrix matrix = new Matrix();
			matrix.postScale(desiredScale, desiredScale);

			return Bitmap.createBitmap(sampledSrcBitmap, 0, 0,
					sampledSrcBitmap.getWidth(), sampledSrcBitmap.getHeight(),
					matrix, true);

		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (v != null) {
			switch (v.getId()) {
			case R.id.chat_send:

				if (chatViewerAdapter != null) {

					if (checkRecord()) {

						switch (event.getAction()) {

						case MotionEvent.ACTION_DOWN:
							// if (chatViewerAdapter != null) {
							setRecordVisible();
							// }
							startRecording();

							break;
						case MotionEvent.ACTION_UP:
							// if (chatViewerAdapter != null) {
							setMessageVisible();
							// }
							stopRecording();
							break;
						}
					}
				}

				break;

			default:
				break;
			}
		}

		return false;
	}

	private void startRecording() {
		// if (currentapiVersion >=
		// android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
		// Do something for GINGERBREAD and above versions
		if (!isRecording) {
			isRecording = true;
			new StartRecord().execute();
		}
		// } else {
		// // do something for phones running an SDK before froyo
		// Toast.makeText(getApplicationContext(),
		// getString(R.string.no_version_android), Toast.LENGTH_SHORT)
		// .show();
		// }
	}

	class StartRecord extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			initRecord = true;
			if (pageSwitcher != null) {
				pageSwitcher.setNoSwipeableViewPager(true);
			}

			startTimer();

			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(output_formats[currentFormat]);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
			recorder.setOutputFile(getFilename());
			recorder.setOnErrorListener(errorListener);
			recorder.setOnInfoListener(infoListener);
			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				isRecording = false;
			} catch (IOException e) {
				e.printStackTrace();
				isRecording = false;
			}

			initRecord = false;

			return null;
		}

	}

	class StopRecord extends AsyncTask<Void, Void, Void> {

		Boolean errorTooFast = false;

		@Override
		protected Void doInBackground(Void... params) {

			while (initRecord) {

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			isRecording = false;

			stopTimer();
			if (null != recorder) {
				try {
					recorder.stop();
					recorder.reset();
					recorder.release();
					recorder = null;
				} catch (RuntimeException r) {
					r.printStackTrace();
					errorTooFast = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (pageSwitcher != null) {
				pageSwitcher.setNoSwipeableViewPager(false);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (errorTooFast) {
				Toast.makeText(ChatViewer.this,
						getString(R.string.push_to_record_pull_to_send),
						Toast.LENGTH_SHORT).show();

			} else if (pathRecord != null) {

				MediaPlayer mp = MediaPlayer.create(ChatViewer.this,
						Uri.parse(pathRecord));
				int duration = 0;
				if (mp != null) {
					duration = mp.getDuration();
				}

				if (duration < 1000) {
					Toast.makeText(ChatViewer.this,
							getString(R.string.push_to_record_pull_to_send),
							Toast.LENGTH_SHORT).show();
				} else {
					startSendFile(pathRecord);
				}

			}
			// sendFile(pathRecord);
			// new SendFile().execute(pathRecord);
			// sendMessageFile("Mensaje de voz", pathRecord);
			pathRecord = null;
		}

	}

	private void stopRecording() {

		new StopRecord().execute();
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		pathRecord = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);

		return pathRecord;
	}

	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			// Toast.makeText(ChatViewer.this, "Error: " + what + ", " + extra,
			// Toast.LENGTH_SHORT).show();
		}
	};

	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			// Toast.makeText(ChatViewer.this, "Warning: " + what + ", " +
			// extra,
			// Toast.LENGTH_SHORT).show();
		}
	};

	// @Override
	// public void loadTopBar() {
	// if (this != null && getParent().getParent() != null) {
	//
	// ImageView imgSend = (ImageView) getParent().getParent()
	// .findViewById(R.id.img_send);
	// if (imgSend != null) {
	// imgSend.setOnClickListener(this);
	// imgSend.setVisibility(View.VISIBLE);
	// }
	//
	// ImageView imgSettings = (ImageView) getParent().getParent()
	// .findViewById(R.id.img_settings);
	// if (imgSettings != null) {
	// imgSettings.setOnClickListener(this);
	// imgSettings.setVisibility(View.VISIBLE);
	// }
	//
	// LinearLayout ll = (LinearLayout) getParent().getParent()
	// .findViewById(R.id.ll_icons);
	// if (ll != null) {
	// ll.setVisibility(View.VISIBLE);
	// }
	//
	// LinearLayout llSearch = (LinearLayout) getParent().getParent()
	// .findViewById(R.id.ll_search);
	// if (llSearch != null) {
	//
	// llSearch.setVisibility(View.GONE);
	// }
	//
	// TextView tvUnder = (TextView) getParent().getParent().findViewById(
	// R.id.tv_status_line);
	// if (tvUnder != null) {
	// tvUnder.setVisibility(View.VISIBLE);
	// }
	//
	// TextView tvTop = (TextView) getParent().getParent().findViewById(
	// R.id.tv_top_bar);
	// if (tvTop != null) {
	// tvTop.setVisibility(View.VISIBLE);
	// tvTop.setText(getString(R.string.chat_th));
	// }
	//
	// // ImageView imgSetting = (ImageView) getParent().getParent()
	// // .findViewById(R.id.img_settings);
	// // if (imgSetting != null) {
	// // imgSetting.setVisibility(View.GONE);
	// // }
	// }
	// }

	private void updateMenuTitles() {

		if (menu != null) {

			MenuItem bedMenuItem = menu.findItem(R.id.silence);

			if (bedMenuItem != null) {

				Uri uri = ChatManager.getInstance().getSound(actionWithAccount,
						actionWithUser);

				if (uri != null) {
					bedMenuItem.setTitle(getString(R.string.silence));
				} else {
					bedMenuItem.setTitle(getString(R.string.no_silence));
				}
			}
		}
	}

	public void startSendFile(String path) {

		if (path != null) {

			// ((Application) getApplication())
			// .setChatViewerAdapter(chatViewerAdapter);

			String aux[] = path.split("/");

			File f = new File(path);

			Long size = 0L;
			Double kb = 0d, mb = 0d;
			String sSize = null;
			if (f.exists()) {
				size = f.length();
				mb = (double) (size / 1048576d);
				if (mb >= 1) {
					mb = RoundedUtil.round(mb, 2);
					sSize = mb + " Mb";
				} else {
					kb = (double) (size / 1024d);
					kb = RoundedUtil.round(kb, 2);
					sSize = kb + " kb";
				}
			}

			String fileName = aux[aux.length - 1];
			if (fileName.contains(Urls.LOCAL_CONSIGNA_PATH)) {
				fileName = fileName.split(Urls.LOCAL_CONSIGNA_PATH)[1];
			}

			Consigna consigna = new Consigna(fileName, sSize, null, path);

			consigna.setToUpLoad(true);

			sendMessage(consigna);
		}

		// Intent service = new Intent(this, SendFileService.class);
		// service.putExtra("from", actionWithAccount);
		// service.putExtra("to", actionWithUser);
		// service.putExtra("path", path);
		//
		// startService(service);

	}

	private void sendMessage(Consigna consigna) {

		ChatViewerAdapter.ChatViewHolder chatViewHolder = null;
		if (chatViewerAdapter != null)
			for (int i = 0; i < chatViewerAdapter.getCount(); i++) {
				if (chatViewerAdapter.getItem(i) instanceof RegularChat
						&& ((RegularChat) chatViewerAdapter.getItem(i))
								.getUser().equalsIgnoreCase(actionWithUser)) {
					View v = chatViewerAdapter.getView(i, null, null);
					if (v != null) {
						chatViewHolder = (ChatViewHolder) v.getTag();
					}
				}
			}

		MessageItem messageItem = MessageManager.getInstance()
				.createMessageLocaly(actionWithAccount, actionWithUser,
						consigna.getName() + " | " + consigna.getSize(),
						consigna);

		if (chatViewHolder != null && chatViewHolder.chatMessageAdapter != null) {
			chatViewHolder.chatMessageAdapter.addItem(messageItem);
		}

	}

	private void startTimer() {

		c = Calendar.getInstance();
		c.set(0, 0, 0, 0, 0, 0);

		t = new Thread() {

			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(1000);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								c.setTimeInMillis(c.getTimeInMillis() + 1000);
								if (chatViewerAdapter != null) {

									setTextTime(String.valueOf(RoundedUtil
											.roundInteger(
													c.get(Calendar.MINUTE), 2)
											+ ":"
											+ RoundedUtil.roundInteger(
													c.get(Calendar.SECOND), 2)));

								}
							}
						});
					}
				} catch (InterruptedException e) {
				}
			}
		};

		t.start();

	}

	private void stopTimer() {

		if (t != null) {
			t.interrupt();
		}

		setTextTime(String.valueOf("00:00"));

	}

	public String getTextWrite() {

		if (actionWithView != null) {

			return ((EditText) actionWithView.findViewById(R.id.chat_input))
					.getText().toString();
		} else {
			return "";
		}

	}

	public boolean checkRecord() {

		// AbstractChat abstractChat = MessageManager.getInstance().getChat(
		// actionWithAccount, actionWithUser);

		Boolean result = false;
		// if (!(abstractChat instanceof RoomChat)) {
		if (currentapiVersion > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			String txt = getTextWrite();
			if (txt == null || txt.equalsIgnoreCase("")) {
				result = true;
			}
		}
		// }

		return result;

	}

	public void paintButton() {

		if (actionWithView != null) {
			if (checkRecord()) {
				actionWithView.findViewById(R.id.img_micro).setVisibility(
						View.VISIBLE);
				actionWithView.findViewById(R.id.btn_send).setVisibility(
						View.INVISIBLE);

			} else {
				actionWithView.findViewById(R.id.img_micro).setVisibility(
						View.INVISIBLE);
				actionWithView.findViewById(R.id.btn_send).setVisibility(
						View.VISIBLE);
			}
		}

	}

	public void setTextTime(final String time) {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// if (chatViewHolder.tvTime != null) {
				((TextView) actionWithView.findViewById(R.id.tv_time))
						.setText(time);
				// }
			}
		});

	}

	public void setRecordVisible() {

		// setVisibilitySearch(chatViewHolder.llRecord,
		// chatViewHolder.llMessage);

		actionWithView.findViewById(R.id.ll_record).setVisibility(View.VISIBLE);
		actionWithView.findViewById(R.id.ll_message).setVisibility(
				View.INVISIBLE);
	}

	public void setMessageVisible() {

		// setVisibilitySearch(chatViewHolder.llMessage,
		// chatViewHolder.llRecord);

		actionWithView.findViewById(R.id.ll_record).setVisibility(
				View.INVISIBLE);
		actionWithView.findViewById(R.id.ll_message)
				.setVisibility(View.VISIBLE);
	}

	// private void setVisibilitySearch(final View vInvisible, View vVisible) {
	//
	// AnimatorSet set = new AnimatorSet();
	// set.playTogether(ObjectAnimator.ofFloat(vVisible, "alpha", 0, 1),
	// ObjectAnimator.ofFloat(vInvisible, "alpha", 1, 0));
	// set.setDuration(Constantes.fadeAnimationTime).start();
	//
	//
	// }

	class ForwardMessage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(0);
				if (pendingMessageItem != null) {
					if (pendingMessageItem.getConsigna() != null) {

						Consigna consigna = pendingMessageItem.getConsigna();
						consigna.setId(null);
						MessageManager.getInstance().sendMessage(
								actionWithAccount, actionWithUser,
								consigna.getUrlConsigna(), consigna);
					} else {
						sendMessage(pendingMessageItem.getText());
					}
					pendingMessageItem = null;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		}

	}
}
