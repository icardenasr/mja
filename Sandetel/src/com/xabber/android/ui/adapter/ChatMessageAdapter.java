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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.TextAppearanceSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.SettingsManager.ChatsDivide;
import com.xabber.android.data.account.AccountItem;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.extension.avatar.AvatarManager;
import com.xabber.android.data.extension.muc.MUCManager;
import com.xabber.android.data.extension.muc.RoomContact;
import com.xabber.android.data.message.ChatAction;
import com.xabber.android.data.message.Consigna;
import com.xabber.android.data.message.ConsignaTable;
import com.xabber.android.data.message.MessageItem;
import com.xabber.android.data.message.MessageManager;
import com.xabber.android.data.roster.AbstractContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.service.ws_client.CallWSAsyn;
import com.xabber.android.service.ws_client.constantes.ConstantesWS;
import com.xabber.android.service.ws_client.interfaces.IDownloadFile;
import com.xabber.android.service.ws_client.interfaces.IUploadFile;
import com.xabber.android.service.ws_client.service_ws.FileWS;
import com.xabber.android.utils.Emoticons;
import com.xabber.android.utils.Fechas;
import com.xabber.android.utils.StringUtils;
import com.xabber.android.utils.file.FileManager;
import com.xabber.android.utils.share.ChatMessageManager;

import es.juntadeandalucia.android.im.R;

/**
 * Adapter for the list of messages in the chat.
 * 
 * @author alexander.ivanov
 * 
 */

public class ChatMessageAdapter extends BaseAdapter implements UpdatableAdapter {

	private static final int TYPE_MESSAGE = 0;
	private static final int TYPE_HINT = 1;
	private static final int TYPE_EMPTY = 2;

	private final Activity activity;
	private String account;
	private String user;
	private boolean isMUC;
	private List<MessageItem> messages;

	/**
	 * Message font appearance.
	 */
	private final int appearanceStyle;

	/**
	 * Divider between header and body.
	 */
	private final String divider;

	/**
	 * Text with extra information.
	 */
	private String hint;

	private ChatViewerAdapter cva;

	private String userName;

	private ListView lv;

	public ChatViewerAdapter getCva() {
		return cva;
	}

	public void setCva(ChatViewerAdapter cva) {
		this.cva = cva;
	}

	public ChatMessageAdapter(Activity activity, String userName) {
		this.activity = activity;

		messages = Collections.emptyList();
		account = null;
		user = null;
		hint = null;
		this.userName = userName;
		appearanceStyle = SettingsManager.chatsAppearanceStyle();
		ChatsDivide chatsDivide = SettingsManager.chatsDivide();
		if (chatsDivide == ChatsDivide.always
				|| (chatsDivide == ChatsDivide.portial && !activity
						.getResources().getBoolean(R.bool.landscape)))
			divider = "\n";
		else
			divider = " ";
	}

	@Override
	public int getCount() {
		return messages.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position < messages.size())
			return messages.get(position);
		else
			return null;
	}

	public void addItem(MessageItem messageItem) {

		if (messages == null) {
			messages = new ArrayList<MessageItem>();
		}

		messages.add(messageItem);

		notifyDataSetChanged();

		// if (lv != null) {
		// lv.setSelection(getCount() - 1);
		// }

	}

	public void setList(ListView lv) {
		this.lv = lv;
	}

	public void removeItem(int position) {

		if (messages == null) {
			messages = new ArrayList<MessageItem>();
		}

		messages.remove(position);

		// notifyDataSetChanged();

	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		if (position < messages.size())
			return TYPE_MESSAGE;
		else
			return hint == null ? TYPE_EMPTY : TYPE_HINT;
	}

	private void append(SpannableStringBuilder builder, CharSequence text,
			CharacterStyle span) {
		int start = builder.length();
		builder.append(text);
		builder.setSpan(span, start, start + text.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int type = getItemViewType(position);
		final View view;

		final MessageItem messageItem = (MessageItem) getItem(position);
		String name, account = null, user = null, resource = null;
		boolean incoming = false;
		if (messageItem != null) {
			account = messageItem.getChat().getAccount();
			user = messageItem.getChat().getUser();
			resource = messageItem.getResource();
			incoming = messageItem.isIncoming();
		}
		Consigna consigna = null;
		if (messageItem != null) {
			consigna = messageItem.getConsigna();

		}

		// if (convertView == null) {
		final int resource2;
		if (type == TYPE_MESSAGE)
			resource2 = selectRowMessage(consigna);
		else if (type == TYPE_HINT)
			resource2 = R.layout.chat_viewer_info;
		else if (type == TYPE_EMPTY)
			resource2 = R.layout.chat_viewer_empty;
		else
			throw new IllegalStateException();
		view = activity.getLayoutInflater().inflate(resource2, parent, false);
		// if (type == TYPE_MESSAGE)
		// if (((TextView) view.findViewById(R.id.text)) != null) {
		// ((TextView) view.findViewById(R.id.text)).setTextAppearance(
		// activity, appearanceStyle);
		// }
		// } else
		// view = convertView;

		if (type == TYPE_EMPTY)
			return view;

		if (type == TYPE_HINT) {
			TextView textView = ((TextView) view.findViewById(R.id.info));
			textView.setText(hint);
			textView.setTextAppearance(activity, R.style.ChatInfo_Warning);
			return view;
		}

		if (isMUC) {
			name = resource;
		} else {
			if (incoming)
				name = RosterManager.getInstance().getName(account, user);
			else
				name = AccountManager.getInstance().getNickName(account);
		}

		LinearLayout llBackGround = (LinearLayout) view
				.findViewById(R.id.background);
		Resources r = activity.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				35, r.getDisplayMetrics());
		int pxDefault = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 0, r.getDisplayMetrics());
		if (llBackGround != null && incoming) {
			// if (view.getBackground() == null)
			llBackGround
					.setBackgroundResource(R.drawable.selector_back_message_received);

			RelativeLayout.LayoutParams params = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			// params.addRule(RelativeLayout.LEFT_OF, R.id.tv_right);
			// params.addRule(RelativeLayout.RIGHT_OF, R.id.tv_left);
			//
			params.setMargins(pxDefault, pxDefault, px, pxDefault);
			llBackGround.setLayoutParams(params); // causes layout update

			// view.getBackground().setLevel(
			// AccountManager.getInstance().getColorLevel(account));
		} else if (llBackGround != null) {
			llBackGround
					.setBackgroundResource(R.drawable.selector_back_message_sent);
			RelativeLayout.LayoutParams params = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			// params.addRule(RelativeLayout.LEFT_OF, R.id.tv_right);
			// params.addRule(RelativeLayout.RIGHT_OF, R.id.tv_left);
			params.setMargins(px, pxDefault, pxDefault, pxDefault);

			llBackGround.setLayoutParams(params); // causes layout update

		}
		Spannable text = messageItem.getSpannable();

		if (consigna != null) {
			text = Emoticons.newSpannable(consigna.getName() + " | "
					+ consigna.getSize());
		}

		TextView tvLeft = (TextView) view.findViewById(R.id.tv_left);
		TextView tvRight = (TextView) view.findViewById(R.id.tv_right);

		TextView textViewCoUser = (TextView) view.findViewById(R.id.co_user);
		TextView textViewUser = (TextView) view.findViewById(R.id.user);
		TextView textViewTime = (TextView) view.findViewById(R.id.time);
		final TextView textView = (TextView) view.findViewById(R.id.text);
		ImageView avatarView = (ImageView) view.findViewById(R.id.avatar);
		LinearLayout llDivider = (LinearLayout) view
				.findViewById(R.id.ll_divider);

		MessageOnLongClickListener onLongClick = new MessageOnLongClickListener(
				activity, messageItem);

		// Tratamiento para los mensaje con consigna asociada

		traitementConsignaView(view, consigna, incoming, position, onLongClick);

		ChatAction action = messageItem.getAction();
		String time = StringUtils.getSmartTimeText(messageItem.getTimestamp());
		SpannableStringBuilder builder = new SpannableStringBuilder();
		Boolean isEvent = false;
		if (action == null) {
			int messageResource = R.drawable.ic_message_delivered;
			if (!incoming) {
				if (messageItem.isError())
					messageResource = R.drawable.ic_message_has_error;
				else if (!messageItem.isSent())
					messageResource = R.drawable.ic_message_not_sent;
				else if (!messageItem.isDelivered())
					messageResource = R.drawable.ic_message_not_delivered;
			}

			// append(builder, " ", new ImageSpan(activity, messageResource));
			// append(builder, " ", new TextAppearanceSpan(activity,
			// R.style.ChatHeader));
			// append(builder, time, new TextAppearanceSpan(activity,
			// R.style.ChatHeader_Time));
			// append(builder, " ", new TextAppearanceSpan(activity,
			// R.style.ChatHeader));
			// append(builder, name, new TextAppearanceSpan(activity,
			// R.style.ChatHeader));
			if (incoming) {
				textViewCoUser.setVisibility(View.GONE);
				if (name != null && !name.equals("")) {

					textViewUser.setText(name);
				} else if (userName != null && !userName.equals("")) {
					textViewUser.setText(userName.split("@")[0]);
				} else {
					textViewUser.setText(getUser());
				}
				llDivider.setVisibility(View.VISIBLE);

			} else {
				textViewCoUser.setVisibility(View.GONE);
				textViewUser.setText("");
				llDivider.setVisibility(View.GONE);

			}
			// append(builder, divider, new TextAppearanceSpan(activity,
			// R.style.ChatHeader));
			Date timeStamp = messageItem.getTimestamp();
			// if (time != null) {
			// Calendar c = Calendar.getInstance();
			//
			// String fecha;
			// if ((time.getDate() == c.get(Calendar.DATE))
			// && (timeStamp.getMonth() == c.get(Calendar.MONTH))
			// && (timeStamp.getYear() == c.get(Calendar.YEAR))) {
			// fecha = Fechas.formatTimeNoSeconds(time);
			// } else {
			// fecha = Fechas.formatFecha_dd_mm_yyyy(time, "/");
			// }
			//
			// textViewTime.setText(fecha);
			// }

			if (timeStamp != null) {
				Calendar c = Calendar.getInstance();

				String fecha;
				if ((timeStamp.getDate() == c.get(Calendar.DATE))
						&& (timeStamp.getMonth() == c.get(Calendar.MONTH))
						&& ((timeStamp.getYear() + 1900) == c
								.get(Calendar.YEAR))) {
					fecha = Fechas.formatTimeNoSeconds(timeStamp);
				} else {
					fecha = Fechas.formatFecha_dd_mm_yyyy(timeStamp, "/");
				}

				textViewTime.setText(fecha);
			}
			if (messageItem.isUnencypted()) {
				append(builder,
						activity.getString(R.string.otr_unencrypted_message),
						new TextAppearanceSpan(activity,
								R.style.ChatHeader_Delay));
				append(builder, divider, new TextAppearanceSpan(activity,
						R.style.ChatHeader));
			}
			Emoticons.getSmiledText(activity.getApplication(), text);
			if (messageItem.getTag() == null)
				builder.append(text);
			else
				append(builder, text, new TextAppearanceSpan(activity,
						R.style.ChatRead));
		} else {
			append(builder, time, new TextAppearanceSpan(activity,
					R.style.ChatReadEvent));
			append(builder, ": ", new TextAppearanceSpan(activity,
					R.style.ChatReadEvent));
			// append(builder, name, new TextAppearanceSpan(activity,
			// R.style.ChatReadEvent));

			if (name == null || name.equals("")) {
				name = activity.getString(R.string.administrator);
			}

			text = Emoticons.newSpannable(action.getText(activity, name,
					text.toString()));

			((LinearLayout) view.findViewById(R.id.ll_divider))
					.setVisibility(View.GONE);
			((LinearLayout) view.findViewById(R.id.ll_head_info))
					.setVisibility(View.GONE);
			llBackGround.setBackgroundResource(android.R.color.transparent);
			isEvent = true;
			if (consigna == null) {
				Emoticons.getSmiledText(activity.getApplication(), text);
			}
			append(builder, text, new TextAppearanceSpan(activity,
					R.style.ChatReadEvent));
		}
		if (textView != null) {
			textView.setText(builder);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		if (SettingsManager.chatsShowAvatars()) {
			avatarView.setVisibility(View.VISIBLE);
			if (!incoming
					|| (isMUC && MUCManager.getInstance()
							.getNickname(account, user)
							.equalsIgnoreCase(resource))) {
				avatarView.setImageDrawable(AvatarManager.getInstance()
						.getAccountAvatar(account));
			} else {
				if (isMUC) {
					if ("".equals(resource)) {
						avatarView.setImageDrawable(AvatarManager.getInstance()
								.getRoomAvatar(user));
					} else {
						avatarView.setImageDrawable(AvatarManager.getInstance()
								.getOccupantAvatar(user + "/" + resource));
					}
				} else {
					avatarView.setImageDrawable(AvatarManager.getInstance()
							.getUserAvatar(user));
				}
			}
			((RelativeLayout.LayoutParams) textView.getLayoutParams()).addRule(
					RelativeLayout.RIGHT_OF, R.id.avatar);
		} else {
			if (avatarView != null) {
				avatarView.setVisibility(View.GONE);
			}
			// ((LinearLayout.LayoutParams) textView.getLayoutParams()).addRule(
			// LinearLayout.RIGHT_OF, 0);
		}

		if (messageItem.getTag() != null) {
			avatarView.setVisibility(View.VISIBLE);

			avatarView.setTag(messageItem.getTag());
			avatarView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					String uri = (String) v.getTag();

					File f = new File(uri);

					if (f != null && f.exists()) {

						// String types = getMimeType(uri);
						// String type[] = null;
						// if (types != null) {
						// type = types.split("/");
						// }
						// if (type != null && type.length > 0) {
						// Intent intent = new Intent(Intent.ACTION_VIEW);
						// intent.setDataAndType(Uri.parse(uri), type[0]
						// + "/*");
						// activity.startActivity(intent);
						// }

						File file = new File(uri);
						MimeTypeMap map = MimeTypeMap.getSingleton();
						String ext = MimeTypeMap.getFileExtensionFromUrl(file
								.getName());
						String type = map.getMimeTypeFromExtension(ext);

						if (type == null)
							type = "*/*";

						Intent intent = new Intent(Intent.ACTION_VIEW);
						Uri data = Uri.fromFile(file);

						intent.setDataAndType(data, type);

						activity.startActivity(intent);

					}
				}
			});

			if (!incoming) {
				avatarView.setImageResource(R.drawable.dd_received);
			} else {
				avatarView.setImageResource(R.drawable.dd_sent);
			}
		} else {
			if (avatarView != null) {
				avatarView.setVisibility(View.GONE);
				avatarView.setOnClickListener(null);
			}
		}

		// android.widget.AbsListView.LayoutParams layoutParams = new
		// LayoutParams(
		// android.widget.AbsListView.LayoutParams.MATCH_PARENT,
		// android.widget.AbsListView.LayoutParams.WRAP_CONTENT);
		// layoutParams.setMargins(20, 20, 20, 20);
		// view.setLayoutParams(layoutParams);

		if (!isEvent) {

			if (incoming) {
				tvLeft.setVisibility(View.GONE);
				tvRight.setVisibility(View.VISIBLE);
			} else {
				tvLeft.setVisibility(View.VISIBLE);
				tvRight.setVisibility(View.GONE);
			}

		} else {

			tvLeft.setVisibility(View.GONE);
			tvRight.setVisibility(View.GONE);

		}

		view.setOnLongClickListener(onLongClick);
		if (textView != null) {
			textView.setOnLongClickListener(onLongClick);
		}

		return view;
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}

	public String getAccount() {
		return account;
	}

	public String getUser() {
		return user;
	}

	/**
	 * Changes managed chat.
	 * 
	 * @param account
	 * @param user
	 */
	public void setChat(String account, String user) {
		this.account = account;
		this.user = user;
		this.isMUC = MUCManager.getInstance().hasRoom(account, user);
		onChange();
	}

	@Override
	public void onChange() {
		messages = new ArrayList<MessageItem>(MessageManager.getInstance()
				.getMessages(account, user));

		// for (int i = messages.size()-1; i > 0; i--) {
		// if(messages.get(i).getConsigna()!= null && (messages.get(i).getId()
		// == null && !messages.get(i).getConsigna().getUplaoding()))
		// {
		// messages.remove(i);
		// }
		// }

		hint = getHint();
		notifyDataSetChanged();
	}

	/**
	 * @return New hint.
	 */
	private String getHint() {
		AccountItem accountItem = AccountManager.getInstance().getAccount(
				account);
		boolean online;
		if (accountItem == null)
			online = false;
		else
			online = accountItem.getState().isConnected();
		final AbstractContact abstractContact = RosterManager.getInstance()
				.getBestContact(account, user);
		if (!online) {
			if (abstractContact instanceof RoomContact)
				return activity.getString(R.string.muc_is_unavailable);
			else
				return activity.getString(R.string.account_is_offline);
		} else if (!abstractContact.getStatusMode().isOnline()) {
			if (abstractContact instanceof RoomContact)
				return activity.getString(R.string.muc_is_unavailable);
			else {

				return activity.getString(R.string.contact_is_offline,
						abstractContact.getName());

			}
		}
		return null;
	}

	/**
	 * Contact information has been changed. Renews hint and updates data if
	 * necessary.
	 */
	public void updateInfo() {
		String info = getHint();

		getCva().setUserConnectionStateOnParent(info);

		if (this.hint == info || (this.hint != null && this.hint.equals(info)))
			return;
		this.hint = info;
		notifyDataSetChanged();
	}

	private void traitementConsignaView(View view, final Consigna consigna,
			boolean incoming, final Integer position,
			OnLongClickListener onLongClickListener) {

		if (consigna != null) {

			// Recuperamos la vista para mostar visualmente el objeto de
			// consigna
			ImageView imgType = (ImageView) view
					.findViewById(R.id.type_consigna);
			final TextView tvState = (TextView) view
					.findViewById(R.id.tv_state_file);
			final View rlState = view.findViewById(R.id.rl_state_file);
			View llAction = view.findViewById(R.id.ll_action_file);
			final View rlIcon = view.findViewById(R.id.rl_download_view);
			ImageView imgConsigna = (ImageView) view
					.findViewById(R.id.img_consigna);

			// Si la imagen de consigna viene, consigna es un archivo de imagen
			// ya descargada y no se está enviando, pero si no tiene url de
			// consigna es que no se ha subido bien
			if (imgConsigna != null && consigna.getUrlConsigna() != null) {

				loadRowConsignaImage(consigna, imgConsigna, onLongClickListener);

			} else {

				// Si no es una imagen ya decargada y enviada o sin enviar, se
				// debe mostrar un icono representativo al tipo

				if (consigna.getName() != null) {

					if (consigna.getError() != null && consigna.getError()) {
						imgType.setImageResource(R.drawable.ic_adjunto_warning);

					} else {

						// si es un mensaje saliente, no está preparado para
						// subir, y la url de consigna es inexistente ha
						// ocurrido algún error
						if (!incoming
								&& (consigna.getToUpLoad() == null || !consigna
										.getToUpLoad())
								&& consigna.getUrlConsigna() == null) {
							imgType.setImageResource(R.drawable.ic_adjunto_warning);
							consigna.setError(true);
						} else {

							imgType.setImageResource(getImagefromMimeTypeFile(consigna
									.getName()));
						}
					}
				}

				// Montamos la vista del icono de consigna
				loadStateViewconsignaIcon(consigna, rlIcon, rlState, tvState);

				if (consigna.getError()) {

					// Si ha ocurrido un error al subir a consigna se le pone la
					// acción de reintentar
					actionToCliclkConsigna(consigna, llAction, rlIcon, position);

				} else {

					// Si el archivo es recibido y si el archivo no existe y si
					// hay
					// descarga
					// automática, comienza la descarga del archivo
					if (incoming && checkAutomaticDownload(consigna)
							&& !checkIfExistfile(consigna.getUrlLocal())) {

						downloadFile(consigna, position, rlIcon);

					} else if (incoming) {
						// Si no se está subiendo se le aplicará la acción click
						// de consigna
						actionToCliclkConsigna(consigna, llAction, rlIcon,
								position);
					} else if (!incoming) {

						// Si el archivo es enviado y se está subiendo
						// comenzará la
						// acción de subida

						if (consigna.getToUpLoad()) {

							prepareToSend(consigna, position, rlIcon, rlState,
									tvState);
						} else {
							// Si no se está subiendo se le aplicará la acción
							// click
							// de consigna
							actionToCliclkConsigna(consigna, llAction, rlIcon,
									position);
						}
					}
				}
			}
		}
	}

	private void prepareToSend(final Consigna consigna, final int position,
			final View rlIcon, final View rlState, final TextView tvState) {
		final MessageItem message = (MessageItem) getItem(position);
		if (message != null && message.getConsigna() != null) {
			message.getConsigna().setToUpLoad(false);
			message.getConsigna().setUplaoding(true);
		}

		setVisibleLoading(rlIcon);

		new com.xabber.android.async.SendFileAsync(activity, account, user,
				consigna.getUrlLocal(), new IUploadFile() {

					@Override
					public void onResultCorrectWS(Consigna consigna) {

						setInvisibleLoading(rlIcon);

						MessageManager.getInstance().removeMessage(message);

						removeItem(position);

						MessageManager.getInstance().sendMessage(account, user,
								consigna.getUrlConsigna(), consigna);

					}

					@Override
					public void onREsultFailedWS() {

						setInvisibleLoading(rlIcon);

						MessageItem mI = (MessageItem) getItem(position);
						if (mI != null && mI.getConsigna() != null) {
							mI.getConsigna().setError(true);
							mI.getConsigna().setAvailableToSend(false);
							mI.getConsigna().setUplaoding(false);
							mI.getConsigna().setDownloading(false);
							mI.getConsigna().setToUpLoad(false);

						}

						notifyDataSetChanged();

					}

				}).execute();
	}

	private void setVisibleLoading(View v) {
		if (v != null) {
			v.findViewById(R.id.type_consigna).setVisibility(View.INVISIBLE);
			v.findViewById(R.id.pb_download).setVisibility(View.VISIBLE);
		}
	}

	private void setInvisibleLoading(View v) {
		if (v != null) {
			v.findViewById(R.id.type_consigna).setVisibility(View.VISIBLE);
			v.findViewById(R.id.pb_download).setVisibility(View.INVISIBLE);
		}

	}

	private void downloadFile(Consigna consigna, final int position,
			final View rlIcon) {

		setVisibleLoading(rlIcon);

		HashMap<Integer, Object> paramsWS = new HashMap<Integer, Object>();
		paramsWS.put(CallWSAsyn.PARAMS_TYPE_CALL, CallWSAsyn.TYPE_DATA_GET);
		paramsWS.put(CallWSAsyn.PARAMS_MODE_SECURITY, ConstantesWS.MODE_NORMAL);
		paramsWS.put(CallWSAsyn.PARAMS_SHOW_TOAST, false);
		paramsWS.put(CallWSAsyn.PARAMS_SHOW_DIALOG, false);
		paramsWS.put(CallWSAsyn.PARAMS_FILE_NAME, consigna.getName());

		new FileWS(FileWS.DOWNLOAD, activity, null, paramsWS,
				consigna.getUrlDownload(), new IDownloadFile() {

					@Override
					public void onResultCorrectWS(String localPath) {
						MessageItem message = (MessageItem) getItem(position);
						if (message != null) {
							Consigna consigna = message.getConsigna();
							if (consigna != null) {

								// Actualizamos en la lista la url local
								// del archivo descargadao
								consigna.setUrlLocal(localPath);

								// Actualizamos en la DDBB la congigna
								// del archivo descargadao
								ConsignaTable.getInstance()
										.updateUrlLocalConsigna(
												consigna.getId(), localPath);

								setInvisibleLoading(rlIcon);

								notifyDataSetChanged();
								// if (lv != null) {
								// lv.setSelection(getCount() - 1);
								// }
							}
						}
					}
				});

	}

	private void openFile(String urlLocal) {
		try {
			FileManager.getInstance().openFile(activity, urlLocal);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private int getImagefromMimeTypeFile(String name) {

		int image = 0;

		String[] split = name.split("\\.");

		if (split.length > 1) {
			String mimeType = split[split.length - 1];
			if (mimeType != null) {
				if (mimeType.equalsIgnoreCase("aac")
						|| mimeType.equalsIgnoreCase("mp3")
						|| mimeType.equalsIgnoreCase("mp4")
						|| mimeType.equalsIgnoreCase("3gp")) {
					image = R.drawable.ic_adjunto_audio;
				} else if (mimeType.equalsIgnoreCase("jpg")
						|| mimeType.equalsIgnoreCase("jpeg")) {
					image = R.drawable.ic_adjunto_img;
				} else if (mimeType.equalsIgnoreCase("html")) {
					image = R.drawable.ic_adjunto_html;
				} else if (mimeType.equalsIgnoreCase("pdf")) {
					image = R.drawable.ic_adjunto_pdf;
				} else if (mimeType.equalsIgnoreCase("txt")) {
					image = R.drawable.ic_adjunto_txt;
				}
			} else {
				image = R.drawable.ic_adjunto_generico;
			}
		} else {
			image = R.drawable.ic_adjunto_generico;
		}

		return image;

	}

	private Boolean checkIfExistfile(String urlLocal) {
		Boolean result = false;

		if (urlLocal != null) {
			File f = new File(urlLocal);
			if (f.exists()) {
				result = true;
			}

		}
		return result;
	}

	/**
	 * Comprobamos con las preferencias del usuario si la descarga es automática
	 * Si la descarga es automática, se comprobará si cumple los requisitos de
	 * peso para comenzar a descargar
	 * 
	 * @param consigna
	 * @return
	 */
	private Boolean checkAutomaticDownload(Consigna consigna) {

		Boolean result = false;

		// Comprobamos descarga automatica
		Boolean automaticManager = SettingsManager.contactsAutomaticDownload();

		if (automaticManager != null && automaticManager) {
			Double mb = Double.valueOf(SettingsManager
					.contactsAutomaticDownloadMB());
			if (mb != null) {

				String size = consigna.getSize();

				size = size.toLowerCase();

				String aux[] = size.split(" ");
				if (aux != null && aux.length > 0) {
					String sizeSplit = aux[0];

					// String a = "\";

					sizeSplit = sizeSplit.replace(",", ".");

					if (!sizeSplit.toString().equals("")
							&& sizeSplit.toString().matches(
									"[0-9]+(\\.[1-9]{0,2})?")) {

						Double fileSize = Double.parseDouble(sizeSplit);

						if (size.contains("kb")) {
							if (fileSize < 1000) {
								result = true;
							}

							else {
								Double fileSizeMB = fileSize / 1000;

								if (fileSizeMB < mb) {
									result = true;
								}
							}
						} else {
							if (fileSize < mb) {
								result = true;
							}
						}

					}

				}
			}
		}

		return result;

	}

	private Boolean consignaFileIsImage(String nameFile) {

		Boolean result = false;

		if (nameFile != null) {

			nameFile = nameFile.toLowerCase();

			if (nameFile.contains(".jpg") || nameFile.contains(".jpeg")
					|| nameFile.contains(".png")) {
				result = true;
			}

		}

		return result;

	}

	public class MessageOnLongClickListener implements OnLongClickListener {

		private MessageItem messageItem;
		private Context context;

		public MessageOnLongClickListener(Context context,
				MessageItem messageItem) {
			this.messageItem = messageItem;
			this.context = context;
		}

		@Override
		public boolean onLongClick(View v) {

			ChatMessageManager.getInstance().createDialogOptions(context,
					messageItem);

			return false;
		}

	};

	private Integer selectRowMessage(Consigna consigna) {
		int resource2;
		if (consigna == null) {
			resource2 = R.layout.chat_viewer_message;
		} else {
			if (checkIfExistfile(consigna.getUrlLocal())
					&& consignaFileIsImage(consigna.getName())
					&& !consigna.getToUpLoad() && !consigna.getUplaoding()
					&& (consigna.getError() == null || !consigna.getError())
					&& consigna.getUrlConsigna() != null) {
				resource2 = R.layout.chat_viewer_message_consigna_image;
			} else {
				resource2 = R.layout.chat_viewer_message_consigna;
			}
		}

		return resource2;

	}

	private void loadRowConsignaImage(final Consigna consigna,
			ImageView imgConsigna, OnLongClickListener onLongClickListener) {

		Picasso.with(activity).load(new File(consigna.getUrlLocal()))
				.into(imgConsigna);

		if (onLongClickListener != null) {
			imgConsigna.setOnLongClickListener(onLongClickListener);
		}

		imgConsigna.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				openFile(consigna.getUrlLocal());

			}
		});

		// if (consigna.getUplaoding()) {
		//
		// prepareToSend(consigna, position, rlIcon, rlState, tvState);
		// }

	}

	/**
	 * Control de la vista de consigna,
	 * 
	 * @param consigna
	 * @param v
	 * @param rlState
	 * @param tvState
	 */
	private void loadStateViewconsignaIcon(Consigna consigna, View v,
			View rlState, TextView tvState) {
		Boolean exist = false;
		Boolean error = false;
		if (consigna.getError()) {
			error = true;
		} else {
			if (consigna.getUrlLocal() != null
					&& !consigna.getUrlLocal().equalsIgnoreCase("")
					&& checkIfExistfile(consigna.getUrlLocal())) {
				exist = true;
			}
		}
		// Si el mensaje viene con error, es que ha fallado algo en la subida
		// del archivo, se le pondrá la vista de reintentar
		if (error) {
			rlState.setBackgroundResource(R.drawable.back_message_state_consigna_re_send);
			tvState.setText(activity.getString(R.string.re_send));
		} else if (exist) {
			// Si el archivo existe y tiene url de consigna, se le pondrá la
			// vista de abrir archivo
			rlState.setBackgroundResource(R.drawable.back_message_state_consigna_open);
			tvState.setText(activity.getString(R.string.open));
		} else {

			// Si el archivo viene sin url de consigna quiere decir que no se ha
			// subido de manera correcta

			if (consigna.getUrlConsigna() == null) {
				rlState.setBackgroundResource(R.drawable.back_message_state_consigna_re_send);
				tvState.setText(activity.getString(R.string.re_send));
			} else {
				// Si el archivo no existe, se le pondrá la vista para descargar
				rlState.setBackgroundResource(R.drawable.back_message_state_consigna_download);
				tvState.setText(activity.getString(R.string.download));
			}
		}

		if (consigna.getUplaoding()) {
			setVisibleLoading(v);
		}

	}

	private void actionToCliclkConsigna(final Consigna consigna, View llAction,
			final View rlIcon, final Integer position) {

		llAction.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (consigna.getError()) {
					// ((ChatViewer)
					// activity).sendMessage(consigna);

					consigna.setError(false);
					consigna.setToUpLoad(true);
					// consigna.setUplaoding(true);

					notifyDataSetChanged();

				} else if (consigna.getUrlLocal() != null) {
					final File f = new File(consigna.getUrlLocal());
					if (f.exists()) {
						openFile(consigna.getUrlLocal());
					} else {
						downloadFile(consigna, position, rlIcon);
					}
				}

				else {
					downloadFile(consigna, position, rlIcon);
				}

			}

		});
	}
}
