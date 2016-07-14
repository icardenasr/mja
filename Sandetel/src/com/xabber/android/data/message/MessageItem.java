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
import java.util.Date;

import android.text.Spannable;
import android.text.util.Linkify;

import com.xabber.android.utils.Emoticons;
import com.xabber.xmpp.uri.XMPPUri;

/**
 * Message item.
 * 
 * @author alexander.ivanov
 * 
 */
@SuppressWarnings("serial")
public class MessageItem implements Comparable<MessageItem>, Serializable {

	// public static final String ID_FILE_SENT = "fileSent";
	// public static final String ID_FILE_RECEIVED = "fileReceived";

	private final AbstractChat chat;

	/**
	 * Tag used to identify collection in server side message archive. Equals to
	 * collection's start attribute.
	 */
	private String tag;

	/**
	 * Contact's resource.
	 */
	private final String resource;

	/**
	 * Text representation.
	 */
	private final String text;

	/**
	 * Cached text populated with smiles and link.
	 */
	private transient Spannable spannable;

	/**
	 * Optional action. If set message represent not an actual message but some
	 * action in the chat.
	 */
	private final ChatAction action;

	/**
	 * Time when message was received or sent by Xabber.
	 */
	private Date timestamp;

	/**
	 * Time when message was created.
	 */
	private Date delayTimestamp;
	private final boolean incoming;
	private final boolean unencypted;

	/**
	 * ID in database.
	 */
	private Long id;

	/**
	 * Error response received on send request.
	 */
	private boolean error;

	/**
	 * Receipt was received for sent message.
	 */
	private boolean delivered;

	/**
	 * Message was sent.
	 */
	private boolean sent;

	/**
	 * Message was shown to the user.
	 */
	private boolean read;

	/**
	 * Message was received from server side offline storage.
	 */
	private final boolean offline;

	/**
	 * Outgoing packet id.
	 */
	private String packetID;

	private Consigna consigna;

	public MessageItem(AbstractChat chat, String tag, String resource,
			String text, ChatAction action, Date timestamp,
			Date delayTimestamp, boolean incoming, boolean read, boolean sent,
			boolean error, boolean delivered, boolean unencypted,
			boolean offline) {
		this(chat, tag, resource, text, action, timestamp, delayTimestamp,
				incoming, read, sent, error, delivered, unencypted, offline,
				null);
	}

	public MessageItem(AbstractChat chat, String tag, String resource,
			String text, ChatAction action, Date timestamp,
			Date delayTimestamp, boolean incoming, boolean read, boolean sent,
			boolean error, boolean delivered, boolean unencypted,
			boolean offline, Consigna consigna) {
		this.chat = chat;
		this.tag = tag;
		this.resource = resource;
		this.text = text;
		this.action = action;
		this.timestamp = timestamp;
		this.delayTimestamp = delayTimestamp;
		this.incoming = incoming;
		this.read = read;
		this.sent = sent;
		this.error = error;
		this.delivered = delivered;
		this.unencypted = unencypted;
		this.offline = offline;
		this.id = null;
		this.packetID = null;
		this.consigna = consigna;
	}

	public MessageItem(MessageItem messageItem) {
		this(messageItem, true);
	}

	public MessageItem(MessageItem messageItem, boolean loadChat) {
		if (loadChat) {
			this.chat = messageItem.getChat();
		} else {
			this.chat = null;
		}
		this.tag = messageItem.getTag();
		this.resource = messageItem.getResource();
		this.text = messageItem.getText();
		this.action = messageItem.getAction();
		this.timestamp = messageItem.getTimestamp();
		this.delayTimestamp = messageItem.getDelayTimestamp();
		this.incoming = messageItem.isIncoming();
		this.read = messageItem.isRead();
		this.sent = messageItem.isSent();
		this.error = messageItem.isError();
		this.delivered = messageItem.isDelivered();
		this.unencypted = messageItem.isUnencypted();
		this.offline = messageItem.isOffline();
		this.id = null;
		this.packetID = null;
		this.consigna = messageItem.getConsigna();
	}

	public AbstractChat getChat() {
		return chat;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getResource() {
		return resource;
	}

	public String getText() {
		return text;
	}

	public Spannable getSpannable() {
		if (spannable == null) {
			spannable = Emoticons.newSpannable(text);
			Linkify.addLinks(this.spannable, Linkify.ALL);
			XMPPUri.addLinks(this.spannable);
		}
		return spannable;
	}

	public ChatAction getAction() {
		return action;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public Date getDelayTimestamp() {
		return delayTimestamp;
	}

	public boolean isIncoming() {
		return incoming;
	}

	public boolean isError() {
		return error;
	}

	public boolean isDelivered() {
		return delivered;
	}

	public boolean isUnencypted() {
		return unencypted;
	}

	public boolean isOffline() {
		return offline;
	}

	public boolean isSent() {
		return sent;
	}

	public boolean isRead() {
		return read;
	}

	public Long getId() {
		return id;
	}

	void setId(Long id) {
		this.id = id;
	}

	public String getPacketID() {
		return packetID;
	}

	public void setPacketID(String packetID) {
		this.packetID = packetID;
	}

	public Consigna getConsigna() {
		return consigna;
	}

	public void setConsigna(Consigna consigna) {
		this.consigna = consigna;
	}

	void markAsError() {
		error = true;
	}

	void markAsSent() {
		sent = true;
	}

	void setSentTimeStamp(Date timestamp) {
		this.delayTimestamp = this.timestamp;
		this.timestamp = timestamp;
	}

	void markAsRead() {
		read = true;
	}

	void markAsDelivered() {
		delivered = true;
	}

	@Override
	public int compareTo(MessageItem another) {
		return timestamp.compareTo(another.timestamp);
	}

}
