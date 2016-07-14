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
package com.xabber.android.data.extension.vcard;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.xabber.android.data.Application;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.OnLoadListener;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.account.AccountItem;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.account.OnAccountRemovedListener;
import com.xabber.android.data.connection.ConnectionItem;
import com.xabber.android.data.connection.ConnectionManager;
import com.xabber.android.data.connection.OnDisconnectListener;
import com.xabber.android.data.connection.OnPacketListener;
import com.xabber.android.data.extension.avatar.AvatarManager;
import com.xabber.android.data.roster.OnRosterChangedListener;
import com.xabber.android.data.roster.OnRosterReceivedListener;
import com.xabber.android.data.roster.RosterContact;
import com.xabber.android.data.roster.RosterManager;
import com.xabber.android.data.roster.StructuredName;
import com.xabber.xmpp.address.Jid;
import com.xabber.xmpp.vcard.VCard;

import es.juntadeandalucia.android.im.R;

/**
 * Manage vCards and there requests.
 * 
 * @author alexander.ivanov
 * 
 */
public class VCardManager implements OnLoadListener, OnPacketListener,
		OnDisconnectListener, OnRosterReceivedListener,
		OnAccountRemovedListener {

	private static final StructuredName EMPTY_STRUCTURED_NAME = new StructuredName(
			null, null, null, null, null);

	/**
	 * Sent requests.
	 */
	private final Collection<VCardRequest> requests;

	/**
	 * List of invalid avatar's hashes (hashes without actual avatars).
	 */
	private final Set<String> invalidHashes;

	/**
	 * Nick and formatted names for the users.
	 */
	private final Map<String, StructuredName> names;

	/**
	 * List of accounts which requests its avatar in order to avoid subsequence
	 * requests.
	 */
	private final ArrayList<String> accountRequested;

	private final HashMap<String, String> avatarsEncode;

	private final static VCardManager instance;

	static {
		instance = new VCardManager();
		Application.getInstance().addManager(instance);
	}

	public static VCardManager getInstance() {
		return instance;
	}

	private VCardManager() {
		requests = new ArrayList<VCardRequest>();
		invalidHashes = new HashSet<String>();
		names = new HashMap<String, StructuredName>();
		accountRequested = new ArrayList<String>();
		avatarsEncode = new HashMap<String, String>();

	}

	@Override
	public void onLoad() {
		final Map<String, StructuredName> names = new HashMap<String, StructuredName>();
		Cursor cursor = VCardTable.getInstance().list();
		try {
			if (cursor.moveToFirst()) {
				do {
					names.put(
							VCardTable.getUser(cursor),
							new StructuredName(VCardTable.getNickName(cursor),
									VCardTable.getFormattedName(cursor),
									VCardTable.getFirstName(cursor), VCardTable
											.getMiddleName(cursor), VCardTable
											.getLastName(cursor)));
				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		Application.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onLoaded(names);
			}
		});
	}

	private void onLoaded(Map<String, StructuredName> names) {
		this.names.putAll(names);
	}

	@Override
	public void onRosterReceived(AccountItem accountItem) {
		String account = accountItem.getAccount();
		if (!accountRequested.contains(account)
				&& SettingsManager.connectionLoadVCard()) {
			String bareAddress = Jid.getBareAddress(accountItem.getRealJid());
			if (bareAddress != null) {
				request(account, bareAddress, null);
				accountRequested.add(account);
			}
		}

		// Request vCards for new contacts.
		for (RosterContact contact : RosterManager.getInstance().getContacts())
			if (account.equals(contact.getUser())
					&& !names.containsKey(contact.getUser()))
				request(account, contact.getUser(), null);
	}

	/**
	 * Update all contact for user, request the VCard info
	 * 
	 * @param accountItem
	 */
	public void updateAllContact(AccountItem accountItem) {
		if (accountItem != null) {
			String account = accountItem.getAccount();
			// Request vCards for new contacts.
			for (RosterContact contact : RosterManager.getInstance()
					.getContacts())
				if (account.equals(contact.getAccount()))
					request(account, contact.getUser(), null);
		}

	}

	@Override
	public void onDisconnect(ConnectionItem connection) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		Iterator<VCardRequest> iterator = requests.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getAccount().equals(account))
				iterator.remove();
		}
	}

	@Override
	public void onAccountRemoved(AccountItem accountItem) {
		accountRequested.remove(accountItem.getAccount());
	}

	/**
	 * Requests vCard.
	 * 
	 * @param account
	 * @param bareAddress
	 * @param hash
	 *            avatar's hash that was intent to request vCard. Can be
	 *            <code>null</code>.
	 */
	public void request(String account, String bareAddress, String hash) {
		if (hash != null && invalidHashes.contains(hash))
			return;
		// User can change avatar before first request will be completed.
		for (VCardRequest check : requests)
			if (check.getUser().equals(bareAddress)) {
				if (hash != null)
					check.addHash(hash);
				return;
			}
		VCard packet = new VCard();
		packet.setTo(bareAddress);
		packet.setType(Type.GET);
		VCardRequest request = new VCardRequest(account, bareAddress,
				packet.getPacketID());
		requests.add(request);
		if (hash != null)
			request.addHash(hash);
		try {
			ConnectionManager.getInstance().sendPacket(account, packet);
		} catch (NetworkException e) {
			requests.remove(request);
			onVCardFailed(account, bareAddress);
		}
	}

	/**
	 * Requests vCard.
	 * 
	 * @param account
	 * @param bareAddress
	 * @param hash
	 *            avatar's hash that was intent to request vCard. Can be
	 *            <code>null</code>.
	 */
	public void requestAvatar(String account, String bareAddress, String hash,
			String avatarEncode) {

		if (hash != null && invalidHashes.contains(hash))
			return;
		Application.getInstance().setvCardPending(true);
		VCard packet = new VCard();
		packet.setTo(bareAddress);
		packet.setType(Type.GET);
		VCardRequest request = new VCardRequest(account, bareAddress,
				packet.getPacketID());
		avatarsEncode.put(packet.getPacketID(), avatarEncode);
		requests.add(request);
		if (hash != null)
			request.addHash(hash);
		try {
			ConnectionManager.getInstance().sendPacket(account, packet);
		} catch (NetworkException e) {
			requests.remove(request);
			onVCardFailed(account, bareAddress);
		}

	}

	/**
	 * Get uses's nick name.
	 * 
	 * @param bareAddress
	 * @return first specified value:
	 *         <ul>
	 *         <li>nick name</li>
	 *         <li>formatted name</li>
	 *         <li>empty string</li>
	 *         </ul>
	 */
	public String getName(String bareAddress) {
		StructuredName name = names.get(bareAddress);
		if (name == null)
			return "";
		return name.getBestName();
	}

	/**
	 * Get uses's name information.
	 * 
	 * @param bareAddress
	 * @return <code>null</code> if there is no info.
	 */
	public StructuredName getStructucedName(String bareAddress) {
		return names.get(bareAddress);
	}

	private void onVCardReceived(final String account,
			final String bareAddress, final VCard vCard, String hash) {
		if (Application.getInstance().isvCardPending()) {
			Application.getInstance().setvCardPending(false);

			org.jivesoftware.smackx.packet.VCard packet = new org.jivesoftware.smackx.packet.VCard();
			packet.setType(Type.SET);

			vCard.setType(Type.SET);
			packet.setFrom(account);
			packet.setFirstName(vCard.getFirstName());
			packet.setLastName(vCard.getLastName());
			packet.setOrganization((vCard.getOrganizations() != null && vCard
					.getOrganizations().size() > 0) ? vCard.getOrganizations()
					.get(0).getName() : null);
			packet.setPacketID(vCard.getPacketID());

			if (avatarsEncode != null) {
				String imageEnconde = avatarsEncode.get(vCard.getPacketID());
				packet.setAvatar(imageEnconde);
			}

			try {
				ConnectionManager.getInstance().sendPacket(account, packet);
			} catch (NetworkException e) {
				e.printStackTrace();
			}

		}

		for (OnVCardListener listener : Application.getInstance()
				.getUIListeners(OnVCardListener.class))
			listener.onVCardReceived(account, bareAddress, vCard);
	}

	private void onVCardFailed(final String account, final String bareAddress) {

		if (Application.getInstance().isvCardPending()) {
			Application.getInstance().setvCardPending(false);
			Context context = Application.getInstance().getApplicationContext();
			String resultText = null;
			resultText = context.getString(R.string.change_error);

			Toast.makeText(context, resultText, Toast.LENGTH_SHORT).show();
		}

		for (OnVCardListener listener : Application.getInstance()
				.getUIListeners(OnVCardListener.class))
			listener.onVCardFailed(account, bareAddress);
	}

	@Override
	public void onPacket(ConnectionItem connection, final String bareAddress,
			Packet packet) {
		if (!(connection instanceof AccountItem))
			return;
		String account = ((AccountItem) connection).getAccount();
		if (packet instanceof Presence
				&& ((Presence) packet).getType() != Presence.Type.error) {
			if (bareAddress == null)
				return;
			// Request vCard for new users
			if (!names.containsKey(bareAddress))
				if (SettingsManager.connectionLoadVCard())
					request(account, bareAddress, null);
		}

		else if (packet instanceof IQ) {
			IQ iq = (IQ) packet;
			String packetId = iq.getPacketID();
			if (iq.getType() != Type.ERROR && !(packet instanceof VCard)) {

				String id = avatarsEncode.get(iq.getPacketID());
				if (id != null) {
					avatarsEncode.remove(packet.getPacketID());

					Context context = Application.getInstance()
							.getApplicationContext();
					String resultText = null;
					resultText = context.getString(R.string.change_succeful);

					Toast.makeText(context, resultText, Toast.LENGTH_SHORT)
							.show();

					Iterator<VCardRequest> iterator = requests.iterator();
					while (iterator.hasNext()) {
						if (iterator.next().getAccount().equals(account))
							iterator.remove();
					}
				}

				return;
			}

			VCardRequest request = null;
			Iterator<VCardRequest> iterator = requests.iterator();
			while (iterator.hasNext()) {
				VCardRequest check = iterator.next();
				if (check.getPacketId().equals(packetId)) {
					request = check;
					iterator.remove();
					break;
				}
			}
			if (request == null || !request.getUser().equals(bareAddress))
				return;
			final StructuredName name;
			if (iq.getType() == Type.ERROR) {
				onVCardFailed(account, bareAddress);
				invalidHashes.addAll(request.getHashes());
				if (names.containsKey(bareAddress))
					return;
				name = EMPTY_STRUCTURED_NAME;
			} else if (packet instanceof VCard) {

				VCard vCard = (VCard) packet;

				String hash = vCard.getAvatarHash();
				for (String check : request.getHashes()) {
					if (!check.equals(hash)) {
						invalidHashes.add(check);
					}
				}
				AvatarManager.getInstance().onAvatarReceived(bareAddress, hash,
						vCard.getAvatar());
				name = new StructuredName(vCard.getNickName(),
						vCard.getFormattedName(), vCard.getFirstName(),
						vCard.getMiddleName(), vCard.getLastName());

				onVCardReceived(account, bareAddress, vCard, hash);
			} else
				throw new IllegalStateException();
			names.put(bareAddress, name);
			for (RosterContact rosterContact : RosterManager.getInstance()
					.getContacts())
				if (rosterContact.getUser().equals(bareAddress))
					for (OnRosterChangedListener listener : Application
							.getInstance().getManagers(
									OnRosterChangedListener.class))
						listener.onContactStructuredInfoChanged(rosterContact,
								name);
			Application.getInstance().runInBackground(new Runnable() {
				@Override
				public void run() {
					VCardTable.getInstance().write(bareAddress, name);
				}
			});
			if (iq.getFrom() == null) { // account it self
				AccountManager.getInstance().onAccountChanged(account);
			} else {
				RosterManager.getInstance().onContactChanged(account,
						bareAddress);
			}
		}
	}
}
