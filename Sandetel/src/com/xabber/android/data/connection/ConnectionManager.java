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
package com.xabber.android.data.connection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.util.Log;

import com.xabber.android.data.Application;
import com.xabber.android.data.LogManager;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.OnCloseListener;
import com.xabber.android.data.OnInitializedListener;
import com.xabber.android.data.OnTimerListener;
import com.xabber.android.data.account.AccountItem;
import com.xabber.android.data.account.AccountManager;
import com.xabber.android.data.entity.NestedMap;
import com.xabber.xmpp.address.Jid;
import com.xabber.xmpp.wlm.XMessengerOAuth2;

import es.juntadeandalucia.android.im.R;

/**
 * Connection manager.
 * 
 * @author alexander.ivanov
 * 
 */
public class ConnectionManager implements OnInitializedListener,
		OnCloseListener, OnTimerListener {

	/**
	 * Timeout for receiving reply from server.
	 */
	public final static int PACKET_REPLY_TIMEOUT = 5000;

	/**
	 * Path to the trust store in this system.
	 */
	public final static String TRUST_STORE_PATH;

	private final static int RECONNECT_AFTER[] = new int[] { 2, 10, 30, 60,
			300, 600 };

	private int counter = 0;
	private int attemps = 0;

	/**
	 * List of managed connection. Only managed connections can notify
	 * registered listeners.
	 */
	private final Collection<ConnectionThread> managedConnections;

	/**
	 * Request holders for its packet id in accounts.
	 */
	private final NestedMap<RequestHolder> requests;

	private final static ConnectionManager instance;

	static {
		instance = new ConnectionManager();
		Application.getInstance().addManager(instance);

		SmackConfiguration.setPacketReplyTimeout(PACKET_REPLY_TIMEOUT);

		ServiceDiscoveryManager.setIdentityType("handheld");
		ServiceDiscoveryManager.setIdentityName(Application.getInstance()
				.getString(R.string.client_name));

		SASLAuthentication.registerSASLMechanism("X-MESSENGER-OAUTH2",
				XMessengerOAuth2.class);
		SASLAuthentication.supportSASLMechanism("X-MESSENGER-OAUTH2");

		String path = System.getProperty("javax.net.ssl.trustStore");
		if (path == null)
			TRUST_STORE_PATH = System.getProperty("java.home") + File.separator
					+ "etc" + File.separator + "security" + File.separator
					+ "cacerts.bks";
		else
			TRUST_STORE_PATH = path;

		Connection
				.addConnectionCreationListener(new ConnectionCreationListener() {
					@Override
					public void connectionCreated(final Connection connection) {
						ServiceDiscoveryManager.getInstanceFor(connection)
								.addFeature("sslc2s");
					}
				});
	}

	public static ConnectionManager getInstance() {
		return instance;
	}

	private ConnectionManager() {
		managedConnections = new ArrayList<ConnectionThread>();
		requests = new NestedMap<RequestHolder>();
	}

	@Override
	public void onInitialized() {
		updateConnections(false);
		AccountManager.getInstance().onAccountsChanged(
				new ArrayList<String>(AccountManager.getInstance()
						.getAllAccounts()));
	}

	@Override
	public void onClose() {
		ArrayList<ConnectionThread> connections = new ArrayList<ConnectionThread>(
				managedConnections);
		managedConnections.clear();
		for (ConnectionThread connectionThread : connections)
			connectionThread.getConnectionItem().disconnect(connectionThread);
	}

	/**
	 * Update connection state.
	 * 
	 * Start connections in waiting states and stop invalidated connections.
	 * 
	 * @param userRequest
	 */
	public void updateConnections(boolean userRequest) {
		AccountManager accountManager = AccountManager.getInstance();
		for (String account : accountManager.getAccounts()) {
			if (accountManager.getAccount(account)
					.updateConnection(userRequest))
				AccountManager.getInstance().onAccountChanged(account);
		}
	}

	/**
	 * Disconnect and connect using new network.
	 */
	public void forceReconnect() {
		AccountManager accountManager = AccountManager.getInstance();
		for (String account : accountManager.getAccounts()) {
			accountManager.getAccount(account).forceReconnect();
			AccountManager.getInstance().onAccountChanged(account);
		}
	}

	/**
	 * Send packet to authenticated connection.
	 * 
	 * @param account
	 * @param packet
	 */
	public void sendPacket(String account, Packet packet)
			throws NetworkException {
		ConnectionThread connectionThread = null;
		for (ConnectionThread check : managedConnections)
			if (check.getConnectionItem() instanceof AccountItem
					&& ((AccountItem) check.getConnectionItem()).getAccount()
							.equals(account)) {
				connectionThread = check;
				break;
			}
		if (connectionThread == null
				|| !connectionThread.getConnectionItem().getState()
						.isConnected())
			throw new NetworkException(R.string.NOT_CONNECTED);
		XMPPConnection xmppConnection = connectionThread.getXMPPConnection();
		try {
			xmppConnection.sendPacket(packet);
		} catch (IllegalStateException e) {
			throw new NetworkException(R.string.XMPP_EXCEPTION);
		}
	}

	// public void sendPack(String account, String nameFile) {
	// ConnectionThread connectionThread = null;
	// for (ConnectionThread check : managedConnections)
	// if (check.getConnectionItem() instanceof AccountItem
	// && ((AccountItem) check.getConnectionItem()).getAccount()
	// .equals(account)) {
	// connectionThread = check;
	// break;
	// }
	// if (connectionThread == null
	// || !connectionThread.getConnectionItem().getState()
	// .isConnected())
	// try {
	// throw new NetworkException(R.string.NOT_CONNECTED);
	// } catch (NetworkException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// final XMPPConnection xmppConnection = connectionThread
	// .getXMPPConnection();
	//
	// // Create the file transfer manager
	// FileTransferManager manager = new FileTransferManager(xmppConnection);
	//
	// // Create the outgoing file transfer
	// OutgoingFileTransfer transfer = manager
	// .createOutgoingFileTransfer("rafa@sdos.es");
	//
	// // Send the file
	// try {
	//
	// OutgoingFileTransfer.setResponseTimeout(30000);
	//
	// transfer.sendFile(new File(nameFile), "You won't believe this!");
	//
	// while (!transfer.isDone()) {
	//
	// try {
	// Thread.sleep(1000);
	// Log.i("transfere file",
	// "sending file status " + transfer.getStatus()
	// + "progress: " + transfer.getProgress());
	// if (transfer.getStatus() == Status.error) {
	// transfer.cancel();
	// break;
	// }
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// } catch (XMPPException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// Log.i("transfere file", "sending file done");
	// }

	// public void sendPacketFile2(String account, String receiver,
	// String filenameWithPath) {
	//
	// ConnectionThread connectionThread = null;
	// for (ConnectionThread check : managedConnections)
	// if (check.getConnectionItem() instanceof AccountItem
	// && ((AccountItem) check.getConnectionItem()).getAccount()
	// .equals(account)) {
	// connectionThread = check;
	// break;
	// }
	// if (connectionThread == null
	// || !connectionThread.getConnectionItem().getState()
	// .isConnected())
	// try {
	// throw new NetworkException(R.string.NOT_CONNECTED);
	// } catch (NetworkException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// final XMPPConnection xmppConnection = connectionThread
	// .getXMPPConnection();
	//
	// // Create the file transfer manager
	// FileTransferManager manager = new FileTransferManager(xmppConnection);
	// // FileTransferNegotiator
	// // .setServiceEnabledconnection, true);
	//
	// // Create the outgoing file transfer
	// OutgoingFileTransfer transfer = manager
	// .createOutgoingFileTransfer(receiver + "/Smack");
	// Log.i("transfere file", "outgoingfiletransfere is created");
	//
	// try {
	//
	// OutgoingFileTransfer.setResponseTimeout(30000);
	//
	// transfer.sendFile(new File(filenameWithPath), "Description");
	//
	// Log.i("transfere file", "sending file");
	//
	// while (!transfer.isDone()) {
	//
	// try {
	// Thread.sleep(1000);
	// Log.i("transfere file",
	// "sending file status " + transfer.getStatus()
	// + "progress: " + transfer.getProgress());
	// if (transfer.getStatus() == Status.error) {
	// transfer.cancel();
	// break;
	// }
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// } catch (XMPPException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// Log.i("transfere file", "sending file done");
	// }

	public XMPPConnection getXMPPConnection(String account) throws NetworkException {

		ConnectionThread connectionThread = null;
		for (ConnectionThread check : managedConnections)
			if (check.getConnectionItem() instanceof AccountItem
					&& ((AccountItem) check.getConnectionItem()).getAccount()
							.equals(account)) {
				connectionThread = check;
				break;
			}
		if (connectionThread == null
				|| !connectionThread.getConnectionItem().getState()
						.isConnected())
			throw new NetworkException(R.string.NOT_CONNECTED);
		final XMPPConnection xmppConnection = connectionThread
				.getXMPPConnection();
		
		return xmppConnection;

	}

	/**
	 * Send packet to authenticated connection.
	 * 
	 * @param account
	 * @param packet
	 */
	public Boolean sendPacketFile(String account, String to,
			String filenameWithPath) throws NetworkException {

		Boolean result = true;
		ConnectionThread connectionThread = null;
		for (ConnectionThread check : managedConnections)
			if (check.getConnectionItem() instanceof AccountItem
					&& ((AccountItem) check.getConnectionItem()).getAccount()
							.equals(account)) {
				connectionThread = check;
				break;
			}
		if (connectionThread == null
				|| !connectionThread.getConnectionItem().getState()
						.isConnected())
			throw new NetworkException(R.string.NOT_CONNECTED);
		final XMPPConnection xmppConnection = connectionThread
				.getXMPPConnection();

		FileTransferManager manager = new FileTransferManager(xmppConnection);
		OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(to);
		// OutgoingFileTransfer transfer =
		// manager.createOutgoingFileTransfer(to);
		File file = new File(filenameWithPath);
		try {
			transfer.sendFile(file, "test_file");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		while (!transfer.isDone()) {
			if (transfer.getStatus().equals(Status.error)) {
				System.out.println("ERROR!!! " + transfer.getError());
			} else if (transfer.getStatus().equals(Status.cancelled)
					|| transfer.getStatus().equals(Status.refused)) {
				System.out.println("Cancelled!!! " + transfer.getError());
			}
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (transfer.getStatus().equals(Status.refused)
				|| transfer.getStatus().equals(Status.error)
				|| transfer.getStatus().equals(Status.cancelled)) {
			System.out
					.println("refused cancelled error " + transfer.getError());
			result = false;
		} else {
			System.out.println("Success");
		}

		return result;
	}

	/**
	 * Send packet to authenticated connection.
	 * 
	 * @param account
	 * @param packet
	 */
	public Boolean searchUser(String account, String user)
			throws NetworkException {

		Boolean result = false;
		ConnectionThread connectionThread = null;
		for (ConnectionThread check : managedConnections)
			if (check.getConnectionItem() instanceof AccountItem
					&& ((AccountItem) check.getConnectionItem()).getAccount()
							.equals(account)) {
				connectionThread = check;
				break;
			}
		if (connectionThread == null
				|| !connectionThread.getConnectionItem().getState()
						.isConnected())
			throw new NetworkException(R.string.NOT_CONNECTED);
		final XMPPConnection xmppConnection = connectionThread
				.getXMPPConnection();

		try {
			UserSearchManager usm = new UserSearchManager(xmppConnection);
			Form searchForm = usm.getSearchForm("search."
					+ xmppConnection.getServiceName());
			Form answerForm = searchForm.createAnswerForm();
			answerForm.setAnswer("Username", true);
			answerForm.setAnswer("search", user);
			ReportedData data = usm.getSearchResults(answerForm, "search."
					+ xmppConnection.getServiceName());

			// xmppConnection.connect();

			// UserSearchManager search = new UserSearchManager(xmppConnection);
			//
			// Form searchForm = search.getSearchForm("search."
			// + xmppConnection.getServiceName());
			//
			// Form answerForm = searchForm.createAnswerForm();
			// answerForm.setAnswer("Username", true);
			//
			// String userParsed = user.split("@")[0];
			// answerForm.setAnswer("search", userParsed);
			//
			// org.jivesoftware.smackx.ReportedData data = search
			// .getSearchResults(answerForm,
			// "search." + xmppConnection.getServiceName());
			//
			// if (data.getRows() != null) {
			// Iterator<Row> it = data.getRows();
			// while (it.hasNext()) {
			// Row row = it.next();
			// Iterator iterator = row.getValues("jid");
			// if (iterator.hasNext()) {
			// String value = iterator.next().toString();
			// Log.i("Iteartor values......", " " + value);
			// }
			// // Log.i("Iteartor values......"," "+value);
			// }
			//
			// result = true;
			// }

		}

		catch (XMPPException e) {
			Log.e("HEHE NOOB", "Error connection to XMPP Server");
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Send packet to authenticated connection. And notify listener about
	 * acknowledgment.
	 * 
	 * @param account
	 * @param iq
	 * @param listener
	 * @throws NetworkException
	 */
	public void sendRequest(String account, IQ iq, OnResponseListener listener)
			throws NetworkException {
		String packetId = iq.getPacketID();
		RequestHolder holder = new RequestHolder(listener);
		sendPacket(account, iq);
		requests.put(account, packetId, holder);
	}

	public void onConnection(ConnectionThread connectionThread) {
		managedConnections.add(connectionThread);
		for (OnConnectionListener listener : Application.getInstance()
				.getManagers(OnConnectionListener.class))
			listener.onConnection(connectionThread.getConnectionItem());
	}

	public void onConnected(ConnectionThread connectionThread) {
		if (!managedConnections.contains(connectionThread))
			return;
		for (OnConnectedListener listener : Application.getInstance()
				.getManagers(OnConnectedListener.class))
			listener.onConnected(connectionThread.getConnectionItem());
	}

	public void onAuthorized(ConnectionThread connectionThread) {
		if (!managedConnections.contains(connectionThread))
			return;
		LogManager.i(this,
				"onAuthorized: " + connectionThread.getConnectionItem());
		for (OnAuthorizedListener listener : Application.getInstance()
				.getManagers(OnAuthorizedListener.class))
			listener.onAuthorized(connectionThread.getConnectionItem());
	}

	public void onDisconnect(ConnectionThread connectionThread) {
		if (!managedConnections.remove(connectionThread))
			return;
		ConnectionItem connectionItem = connectionThread.getConnectionItem();
		if (connectionItem instanceof AccountItem) {
			String account = ((AccountItem) connectionItem).getAccount();
			for (Entry<String, RequestHolder> entry : requests.getNested(
					account).entrySet())
				entry.getValue().getListener()
						.onDisconnect(account, entry.getKey());
			requests.clear(account);
		}
		for (OnDisconnectListener listener : Application.getInstance()
				.getManagers(OnDisconnectListener.class))
			listener.onDisconnect(connectionThread.getConnectionItem());
	}

	public void processPacket(ConnectionThread connectionThread, Packet packet) {
		if (!managedConnections.contains(connectionThread))
			return;
		ConnectionItem connectionItem = connectionThread.getConnectionItem();
		if (packet instanceof IQ && connectionItem instanceof AccountItem) {
			IQ iq = (IQ) packet;
			String packetId = iq.getPacketID();
			if (packetId != null
					&& (iq.getType() == Type.RESULT || iq.getType() == Type.ERROR)) {
				String account = ((AccountItem) connectionItem).getAccount();
				RequestHolder requestHolder = requests
						.remove(account, packetId);
				if (requestHolder != null) {
					if (iq.getType() == Type.RESULT)
						requestHolder.getListener().onReceived(account,
								packetId, iq);
					else
						requestHolder.getListener().onError(account, packetId,
								iq);
				}
			}
		}
		for (OnPacketListener listener : Application.getInstance().getManagers(
				OnPacketListener.class))
			listener.onPacket(connectionItem,
					Jid.getBareAddress(packet.getFrom()), packet);
	}

	@Override
	public void onTimer() {

		int reconnectAfter;
		if (attemps < RECONNECT_AFTER.length)
			reconnectAfter = RECONNECT_AFTER[attemps];
		else
			reconnectAfter = RECONNECT_AFTER[RECONNECT_AFTER.length - 1];

		if (counter >= reconnectAfter) {

			counter = 0;
			attemps++;
			if (NetworkManager.getInstance().getState() != NetworkState.suspended) {

				Collection<ConnectionItem> reconnect = new ArrayList<ConnectionItem>();
				for (ConnectionThread connectionThread : managedConnections)
					if (connectionThread.getConnectionItem().getState()
							.isConnected()
							// XMPPConnection can`t be null here
							&& !connectionThread.getXMPPConnection().isAlive()) {

						LogManager.i(connectionThread.getConnectionItem(),
								"forceReconnect on checkAlive");

						reconnect.add(connectionThread.getConnectionItem());
					}
				for (ConnectionItem connection : reconnect) {
					Log.e("connection manager", "force reconnect");
					connection.forceReconnect();
				}
			}
			long now = new Date().getTime();
			Iterator<NestedMap.Entry<RequestHolder>> iterator = requests
					.iterator();
			while (iterator.hasNext()) {
				NestedMap.Entry<RequestHolder> entry = iterator.next();
				if (entry.getValue().isExpired(now)) {
					entry.getValue().getListener()
							.onTimeout(entry.getFirst(), entry.getSecond());
					iterator.remove();
				}
			}

		} else {
			counter += 1;
		}

	}

}
