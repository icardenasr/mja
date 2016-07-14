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
package com.xabber.android.data.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DelayInformation;

import android.database.Cursor;

import com.xabber.android.data.Application;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.ArchiveMode;
import com.xabber.android.data.connection.ConnectionItem;
import com.xabber.android.data.connection.ConnectionManager;
import com.xabber.android.data.entity.BaseEntity;
import com.xabber.android.data.extension.archive.MessageArchiveManager;
import com.xabber.android.data.extension.cs.ChatStateManager;
import com.xabber.android.data.extension.otr.OTRManager;
import com.xabber.android.data.extension.otr.SecurityLevel;
import com.xabber.android.data.message.chat.ChatManager;
import com.xabber.android.data.notification.NotificationManager;
import com.xabber.xmpp.archive.SaveMode;

/**
 * Chat instance.
 * 
 * @author alexander.ivanov
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractChat extends BaseEntity implements Serializable {

	/**
	 * Message tag used when server side record is disable.
	 */
	private static final String NO_RECORD_TAG = "com.xabber.android.data.message.NO_RECORD_TAG";

	/**
	 * Number of messages from history to be shown for context purpose.
	 */
	private static final int PRELOADED_MESSAGES = 3;

	/**
	 * Current thread id.
	 */
	private String threadId;

	/**
	 * Whether chat is open and should be displayed as active chat.
	 */
	protected boolean active;

	/**
	 * Whether changes in status should be record.
	 */
	protected boolean trackStatus;

	/**
	 * Whether user never received notifications from this chat.
	 */
	protected boolean firstNotification;

	/**
	 * Last incoming message's text.
	 */
	protected String lastText;

	/**
	 * Last message's time.
	 */
	protected Date lastTime;

	/**
	 * Ids of messages not loaded in to the memory.
	 * 
	 * MUST BE ACCESSED FROM BACKGROUND THREAD ONLY.
	 */
	protected final Collection<Long> historyIds;

	/**
	 * Sorted list of messages in this chat.
	 */
	protected final List<MessageItem> messages;

	/**
	 * List of messages to be sent.
	 */
	protected final Collection<MessageItem> sendQuery;

	protected AbstractChat(final String account, final String user) {
		super(account, user);
		threadId = StringUtils.randomString(12);
		active = false;
		trackStatus = false;
		firstNotification = true;
		lastText = "";
		lastTime = null;
		historyIds = new ArrayList<Long>();
		messages = new ArrayList<MessageItem>();
		sendQuery = new ArrayList<MessageItem>();

		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				loadMessages();
			}
		});
	}

	/**
	 * Load recent messages from local history.
	 * 
	 * CALL THIS METHOD FROM BACKGROUND THREAD ONLY.
	 */
	private void loadMessages() {
		final ArrayList<MessageItem> messageItems = new ArrayList<MessageItem>();
		LinkedList<MessageItem> linkedList = new LinkedList<MessageItem>();
		boolean started = false;
		Cursor cursor = MessageTable.getInstance().list(account, user);
		try {
			if (cursor.moveToFirst()) {
				do {
					MessageItem messageItem = createMessageItem(cursor);
					if (started) {
						messageItems.add(messageItem);
						continue;
					}
					linkedList.addLast(messageItem);
					if (messageItem.isRead() && messageItem.isSent()) {
						if (linkedList.size() <= PRELOADED_MESSAGES)
							continue;
						messageItem = linkedList.removeFirst();
						historyIds.add(messageItem.getId());
						continue;
					}
					started = true;
					messageItems.addAll(linkedList);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		if (!started)
			messageItems.addAll(linkedList);
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (MessageItem messageItem : messageItems)
					updateSendQuery(messageItem);
				addMessages(messageItems);
			}
		});
	}

	/**
	 * Update existing message list with loaded.
	 * 
	 * @param messageItems
	 */
	private void addMessages(Collection<MessageItem> messageItems) {
		messages.addAll(messageItems);
		sort();
		MessageManager.getInstance().onChatChanged(account, user, false);
	}

	/**
	 * @param cursor
	 * @return New message item.
	 */
	private MessageItem createMessageItem(Cursor cursor) {

		Integer idConsigna = MessageTable.idConsigna(cursor);

		MessageItem messageItem = new MessageItem(this,
				MessageTable.getTag(cursor), MessageTable.getResource(cursor),
				MessageTable.getText(cursor), MessageTable.getAction(cursor),
				MessageTable.getTimeStamp(cursor),
				MessageTable.getDelayTimeStamp(cursor),
				MessageTable.isIncoming(cursor), MessageTable.isRead(cursor),
				MessageTable.isSent(cursor), MessageTable.hasError(cursor),
				true, false, false);
		messageItem.setId(MessageTable.getId(cursor));

		Consigna consigna = null;
		if (idConsigna != null && idConsigna > 0) {

			Cursor cursorConsigna = ConsignaTable.getInstance().list(account,
					user);

			try {
				if (cursorConsigna.moveToFirst()) {
					do {
						consigna = createConsignaItem(cursorConsigna);
						if (consigna != null
								&& consigna.getId().longValue() == idConsigna
										.intValue()) {
							break;
						} else {
							consigna = null;
						}
					} while (cursorConsigna.moveToNext());
				}
			} finally {
				cursorConsigna.close();
			}

		}
		if (consigna != null) {
			messageItem.setConsigna(consigna);
		}

		return messageItem;
	}

	private Consigna createConsignaItem(Cursor cursor) {
		Consigna consigna = new Consigna(ConsignaTable.getId(cursor),
				ConsignaTable.getName(cursor), ConsignaTable.getSize(cursor),
				ConsignaTable.getUrlConsigna(cursor),
				ConsignaTable.getUrlDownload(cursor),
				ConsignaTable.getUrlLocal(cursor),
				ConsignaTable.getTimeStamp(cursor));
		return consigna;
	}

	/**
	 * Load all message from local history.
	 * 
	 * CALL THIS METHOD FROM BACKGROUND THREAD ONLY.
	 */
	private void loadHistory() {
		if (historyIds.isEmpty())
			return;
		final ArrayList<MessageItem> messageItems = new ArrayList<MessageItem>();
		Cursor cursor = MessageTable.getInstance().list(account, user);
		try {
			if (cursor.moveToFirst()) {
				do {
					MessageItem messageItem = createMessageItem(cursor);
					if (historyIds.contains(messageItem.getId()))
						messageItems.add(messageItem);
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		historyIds.clear();
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				addMessages(messageItems);
			}
		});
	}

	/**
	 * Updates chat with messages received from server side message archive.
	 * Replaces local copies with the same tag and offline messages.
	 * 
	 * @param tag
	 * @param items
	 * @param replication
	 *            Whether all message without tag (not received from server
	 *            side) should be removed.
	 * @return Number of new messages.
	 */
	public int onMessageDownloaded(String tag, Collection<MessageItem> items,
			boolean replication) {
		int previous = messages.size();
		Iterator<MessageItem> iterator = messages.iterator();
		while (iterator.hasNext()) {
			MessageItem messageItem = iterator.next();
			if (messageItem.getAction() == null
					&& !messageItem.isError()
					&& messageItem.isSent()
					&& ((replication && messageItem.getTag() == null)
							|| messageItem.isOffline() || tag
								.equals(messageItem.getTag())))
				iterator.remove();
		}
		messages.addAll(items);
		sort();
		MessageManager.getInstance().onChatChanged(account, user, false);
		return Math.max(0, messages.size() - previous);
	}

	public boolean isActive() {
		return active;
	}

	void openChat() {
		active = true;
		trackStatus = true;
	}

	void closeChat() {
		active = false;
		firstNotification = true;
	}

	boolean isStatusTrackingEnabled() {
		return trackStatus;
	}

	/**
	 * @return Target address for sending message.
	 */
	public abstract String getTo();

	/**
	 * @return Message type to be assigned.
	 */
	public abstract Type getType();

	/**
	 * @return Whether user never received notifications from this chat. And
	 *         mark as received.
	 */
	public boolean getFirstNotification() {
		boolean result = firstNotification;
		firstNotification = false;
		return result;
	}

	void requestToLoadLocalHistory() {
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				loadHistory();
			}
		});
	}

	Collection<MessageItem> getMessages() {
		return Collections.unmodifiableCollection(messages);
	}

	/**
	 * @return Whether user should be notified about incoming messages in chat.
	 */
	protected boolean notifyAboutMessage() {
		return SettingsManager.eventsMessage() != SettingsManager.EventsMessage.none;
	}

	/**
	 * @param text
	 * @return New message instance.
	 */
	abstract protected MessageItem newMessage(String text);

	/**
	 * @param text
	 * @return New message instance.
	 */
	abstract protected MessageItem newMessage(String text, Consigna consigna);

	/**
	 * Creates new action.
	 * 
	 * @param resource
	 *            can be <code>null</code>.
	 * @param text
	 *            can be <code>null</code>.
	 * @param action
	 */
	public void newAction(String resource, String text, ChatAction action) {
		newMessage(resource, text, action, null, true, false, false, false,
				true, null);

	}

	/**
	 * Creates new message.
	 * 
	 * Any parameter can be <code>null</code> (except boolean values).
	 * 
	 * @param resource
	 *            Contact's resource or nick in conference.
	 * @param text
	 *            message.
	 * @param action
	 *            Informational message.
	 * @param delayTimestamp
	 *            Time when incoming message was sent or outgoing was created.
	 * @param incoming
	 *            Incoming message.
	 * @param notify
	 *            Notify user about this message when appropriated.
	 * @param unencrypted
	 *            Whether not encrypted message in OTR chat was received.
	 * @param offline
	 *            Whether message was received from server side offline storage.
	 * @param record
	 *            Whether record server side is enabled.
	 * @return
	 */
	protected MessageItem newMessage(String resource, String text,
			ChatAction action, Date delayTimestamp, boolean incoming,
			boolean notify, boolean unencrypted, boolean offline,
			boolean record, Consigna consigna) {
		return newMessage(resource, text, action, delayTimestamp, incoming,
				notify, unencrypted, offline, record, null, consigna);
	}

	/**
	 * Creates new message.
	 * 
	 * Any parameter can be <code>null</code> (except boolean values).
	 * 
	 * @param resource
	 *            Contact's resource or nick in conference.
	 * @param text
	 *            message.
	 * @param action
	 *            Informational message.
	 * @param delayTimestamp
	 *            Time when incoming message was sent or outgoing was created.
	 * @param incoming
	 *            Incoming message.
	 * @param notify
	 *            Notify user about this message when appropriated.
	 * @param unencrypted
	 *            Whether not encrypted message in OTR chat was received.
	 * @param offline
	 *            Whether message was received from server side offline storage.
	 * @param record
	 *            Whether record server side is enabled.
	 * @return
	 */
	protected MessageItem newMessage(String resource, String text,
			ChatAction action, Date delayTimestamp, boolean incoming,
			boolean notify, boolean unencrypted, boolean offline,
			boolean record, String tag, Consigna consigna) {
		boolean save;
		boolean visible = MessageManager.getInstance().isVisibleChat(this);
		boolean read = incoming ? visible : true;
		boolean send = incoming;
		if (action == null && text == null)
			throw new IllegalArgumentException();
		if (resource == null)
			resource = "";
		if (text == null)
			text = "";
		if (action != null) {
			read = true;
			send = true;
			save = false;
		} else {
			ArchiveMode archiveMode = AccountManager.getInstance()
					.getArchiveMode(account);
			if (archiveMode == ArchiveMode.dontStore)
				save = false;
			else
				save = archiveMode.saveLocally() || !send
						|| (!read && archiveMode == ArchiveMode.unreadOnly);
			if (save)
				save = ChatManager.getInstance().isSaveMessages(account, user);
		}
		if (save
				&& (unencrypted || (!SettingsManager.securityOtrHistory() && OTRManager
						.getInstance().getSecurityLevel(account, user) != SecurityLevel.plain)))
			save = false;
		Date timestamp = new Date();
		if (notify || !incoming)
			openChat();
		if (!incoming)
			notify = false;
		if (notify && !notifyAboutMessage())
			notify = false;
		if (action == null && !read)
			notify = true;

		boolean showTicker = notify;
		if (visible && showTicker) {
			notify = false;
			if (!ChatManager.getInstance().isNotifyVisible(account, user))
				showTicker = false;
		}
		MessageItem messageItem = new MessageItem(this, tag, resource, text,
				action, timestamp, delayTimestamp, incoming, read, send, false,
				incoming, unencrypted, offline);
		if (consigna != null) {
			messageItem.setConsigna(consigna);
		}
		messages.add(messageItem);
		updateSendQuery(messageItem);
		sort();
		if (save)

			requestToWriteMessage(messageItem, resource, text, action,
					timestamp, delayTimestamp, incoming, read, send);
		if (showTicker)
			NotificationManager.getInstance().onMessageNotification(
					messageItem, notify);
		MessageManager.getInstance().onChatChanged(account, user, incoming);
		return messageItem;
	}

	private void requestToWriteMessage(final MessageItem messageItem,
			final String resource, final String text, final ChatAction action,
			final Date timestamp, final Date delayTimestamp,
			final boolean incoming, final boolean read, final boolean sent) {
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {

				Long idConsigna = null;

				Consigna consigna = messageItem.getConsigna();

				if (consigna != null) {
					// Save Consigna object
					idConsigna = ConsignaTable.getInstance().add(account, user,
							consigna.getName(), consigna.getSize(),
							consigna.getUrlConsigna(),
							consigna.getUrlDownload(), consigna.getUrlLocal(),
							timestamp);
				}
				// Save message Object
				long id = MessageTable.getInstance().add(account, user,
						messageItem.getTag(), resource, text, action,
						timestamp, delayTimestamp, incoming, read, sent, false,
						idConsigna);
				messageItem.setId(id);
			}
		});
	}

	private void updateSendQuery(MessageItem messageItem) {
		if (!messageItem.isSent()) {

			sendQuery.add(messageItem);
		}
	}

	/**
	 * Sorts messages and update last text and time.
	 */
	private void sort() {
		Collections.sort(messages);
		for (int index = messages.size() - 1; index >= 0; index--) {
			MessageItem messageItem = messages.get(index);
			if (messageItem.getAction() == null) {
				lastText = messageItem.isIncoming() ? messageItem.getText()
						: "";
				lastTime = messageItem.getTimestamp();
				return;
			}
		}
	}

	void removeMessage(MessageItem messageItem) {
		messages.remove(messageItem);
		sendQuery.remove(messageItem);
		final ArrayList<MessageItem> messageItems = new ArrayList<MessageItem>();

		messageItems.add(messageItem);
		final ArrayList<Consigna> consignaItems = new ArrayList<Consigna>();
		if (messageItem != null && messageItem.getConsigna() != null) {
			consignaItems.add(messageItem.getConsigna());
		}
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				MessageTable.getInstance().removeMessages(
						MessageManager.getMessageIds(messageItems, true));

				Application.getInstance().runInBackground(new Runnable() {
					@Override
					public void run() {

						// Remove Consigna if have
						ConsignaTable.getInstance().removeConsignas(
								MessageManager.getConsignaIds(consignaItems,
										true));
					}
				});

			}
		});
	}

	void removeAllMessages() {
		final ArrayList<MessageItem> messageItems = new ArrayList<MessageItem>(
				messages);
		lastText = "";
		messages.clear();
		sendQuery.clear();
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				final ArrayList<Consigna> consignaItems = new ArrayList<Consigna>();
				for (MessageItem messageItem : messageItems) {
					if (messageItem != null
							&& messageItem.getConsigna() != null) {
						consignaItems.add(messageItem.getConsigna());
					}
				}
				ConsignaTable.getInstance().removeConsignas(
						MessageManager.getConsignaIds(consignaItems, true));
				Application.getInstance().runInBackground(new Runnable() {
					@Override
					public void run() {
						MessageTable.getInstance().removeMessages(
								MessageManager
										.getMessageIds(messageItems, true));
						MessageTable.getInstance().removeMessages(historyIds);
						historyIds.clear();
					}
				});
			}
		});
	}

	/**
	 * @param bareAddress
	 *            bareAddress of the user.
	 * @param user
	 *            full jid.
	 * @return Whether chat accepts packets from specified user.
	 */
	boolean accept(String bareAddress, String user) {
		return this.user.equals(bareAddress);
	}

	/**
	 * Requests to send all not sent messages.
	 */
	public void sendMessages() {
		sendQueue(null);
	}

	/**
	 * @return Whether chat can send messages.
	 */
	protected boolean canSendMessage() {
		return !sendQuery.isEmpty();
	}

	/**
	 * @return Last incoming message's text. Empty string if last message is
	 *         outgoing.
	 */
	protected String getLastText() {
		return lastText;
	}

	/**
	 * @return Time of last message in chat. Can be <code>null</code>.
	 */
	public Date getLastTime() {
		return lastTime;
	}

	/**
	 * @param body
	 * @return New message packet to be sent.
	 */
	public Message createMessagePacket(String body) {
		Message message = new Message();
		message.setTo(getTo());
		message.setType(getType());
		message.setBody(body);
		message.setThread(threadId);
		return message;
	}

	/**
	 * Prepare text to be send.
	 * 
	 * @param text
	 * @return <code>null</code> if text shouldn't be send.
	 */
	protected String prepareText(String text) {
		return text;
	}

	/**
	 * Requests to send messages from queue.
	 * 
	 * @param intent
	 *            can be <code>null</code>.
	 */
	protected void sendQueue(MessageItem intent) {
		if (!canSendMessage())
			return;
		final ArrayList<MessageItem> sentMessages = new ArrayList<MessageItem>();
		final ArrayList<MessageItem> removeMessages = new ArrayList<MessageItem>();
		for (final MessageItem messageItem : sendQuery) {

			if (messageItem.getConsigna() != null
					&& !messageItem.getConsigna().getAvailableToSend()) {
				removeMessages.add(messageItem);
				continue;
			}

			String text = prepareText(messageItem.getText());
			if (text == null) {
				messageItem.markAsError();
				Application.getInstance().runInBackground(new Runnable() {
					@Override
					public void run() {
						if (messageItem.getId() != null)
							MessageTable.getInstance().markAsError(
									messageItem.getId());
					}
				});
			} else {
				Message message = createMessagePacket(text);
				if (intent != null && intent.getConsigna() != null) {
					message.setConsigna(intent.getConsigna());
				}

				messageItem.setPacketID(message.getPacketID());
				ChatStateManager.getInstance().updateOutgoingMessage(this,
						message);
				ReceiptManager.getInstance().updateOutgoingMessage(this,
						message, messageItem);
				if (messageItem != intent)
					message.addExtension(new DelayInformation(messageItem
							.getTimestamp()));

				// CHECK CONSIGNA
				// if (intent.isConsigna()) {
				// message.setType(Type.consigna);
				// }
				try {
					ConnectionManager.getInstance()
							.sendPacket(account, message);
				} catch (NetworkException e) {
					break;
				}
			}
			if (MessageArchiveManager.getInstance().getSaveMode(account, user,
					threadId) == SaveMode.fls)
				messageItem.setTag(NO_RECORD_TAG);
			if (messageItem != intent) {
				messageItem.setSentTimeStamp(new Date());
				Collections.sort(messages);
			}
			messageItem.markAsSent();
			if (AccountManager.getInstance()
					.getArchiveMode(messageItem.getChat().getAccount())
					.saveLocally())
				sentMessages.add(messageItem);
			else
				removeMessages.add(messageItem);
		}
		sendQuery.removeAll(sentMessages);
		sendQuery.removeAll(removeMessages);
		MessageManager.getInstance().onChatChanged(account, user, false);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				Collection<Long> sentIds = MessageManager.getMessageIds(
						sentMessages, false);
				Collection<Long> removeIds = MessageManager.getMessageIds(
						removeMessages, true);
				MessageTable.getInstance().markAsSent(sentIds);
				MessageTable.getInstance().removeMessages(removeIds);
			}
		});

	}

	/**
	 * Requests to send messages from queue.
	 * 
	 * @param intent
	 *            can be <code>null</code>.
	 */
	protected void sendQueueFile(MessageItem intent) {
		if (!canSendMessage())
			return;
		final ArrayList<MessageItem> sentMessages = new ArrayList<MessageItem>();
		final ArrayList<MessageItem> removeMessages = new ArrayList<MessageItem>();
		for (final MessageItem messageItem : sendQuery) {
			String text = prepareText(messageItem.getText());
			if (text == null) {
				messageItem.markAsError();
				Application.getInstance().runInBackground(new Runnable() {
					@Override
					public void run() {
						if (messageItem.getId() != null)
							MessageTable.getInstance().markAsError(
									messageItem.getId());
					}
				});
			} else {
				Message message = createMessagePacket(text);
				messageItem.setPacketID(message.getPacketID());
				ChatStateManager.getInstance().updateOutgoingMessage(this,
						message);
				ReceiptManager.getInstance().updateOutgoingMessage(this,
						message, messageItem);
				if (messageItem != intent)
					message.addExtension(new DelayInformation(messageItem
							.getTimestamp()));
				// try {
				// ConnectionManager.getInstance()
				// .sendPacket(account, message);
				// } catch (NetworkException e) {
				// break;
				// }
			}
			if (MessageArchiveManager.getInstance().getSaveMode(account, user,
					threadId) == SaveMode.fls)
				messageItem.setTag(NO_RECORD_TAG);
			if (messageItem != intent) {
				messageItem.setSentTimeStamp(new Date());
				Collections.sort(messages);
			}
			messageItem.markAsSent();
			if (AccountManager.getInstance()
					.getArchiveMode(messageItem.getChat().getAccount())
					.saveLocally())
				sentMessages.add(messageItem);
			else
				removeMessages.add(messageItem);
		}
		sendQuery.removeAll(sentMessages);
		sendQuery.removeAll(removeMessages);
		MessageManager.getInstance().onChatChanged(account, user, false);
		Application.getInstance().runInBackground(new Runnable() {
			@Override
			public void run() {
				Collection<Long> sentIds = MessageManager.getMessageIds(
						sentMessages, false);
				Collection<Long> removeIds = MessageManager.getMessageIds(
						removeMessages, true);
				MessageTable.getInstance().markAsSent(sentIds);
				MessageTable.getInstance().removeMessages(removeIds);
			}
		});
	}

	public String getThreadId() {
		return threadId;
	}

	/**
	 * Update thread id with new value.
	 * 
	 * @param threadId
	 *            <code>null</code> if current value shouldn't be changed.
	 */
	protected void updateThreadId(String threadId) {
		if (threadId == null)
			return;
		this.threadId = threadId;
	}

	/**
	 * Returns number of messages to be loaded from server side archive to be
	 * displayed. Should be called before notification messages will be removed.
	 * 
	 * @return
	 */
	public int getRequiredMessageCount() {
		int count = PRELOADED_MESSAGES
				+ NotificationManager.getInstance()
						.getNotificationMessageCount(account, user);
		Iterator<MessageItem> iterator = messages.iterator();
		while (iterator.hasNext())
			if (iterator.next().isIncoming()) {
				count -= 1;
				if (count <= 0)
					return 0;
			}
		return count;
	}

	/**
	 * Processes incoming packet.
	 * 
	 * @param bareAddress
	 * @param packet
	 * @return Whether packet was directed to this chat.
	 */
	protected boolean onPacket(ConnectionItem connectionItem,
			String bareAddress, Packet packet) {
		return accept(bareAddress, packet.getFrom());
	}

	/**
	 * Connection complete.
	 */
	protected void onComplete() {
	}

	/**
	 * Disconnection occured.
	 */
	protected void onDisconnect() {
	}

}
