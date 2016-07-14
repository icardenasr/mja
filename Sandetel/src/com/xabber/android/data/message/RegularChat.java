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

import net.java.otr4j.OtrException;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.StreamInitiation;

import com.xabber.android.data.LogManager;
import com.xabber.android.data.NetworkException;
import com.xabber.android.data.SettingsManager;
import com.xabber.android.data.SettingsManager.SecurityOtrMode;
import com.xabber.android.data.connection.ConnectionItem;
import com.xabber.android.data.extension.archive.MessageArchiveManager;
import com.xabber.android.data.extension.otr.OTRManager;
import com.xabber.android.data.extension.otr.OTRUnencryptedException;
import com.xabber.android.data.extension.otr.SecurityLevel;
import com.xabber.xmpp.address.Jid;
import com.xabber.xmpp.archive.SaveMode;
import com.xabber.xmpp.delay.Delay;
import com.xabber.xmpp.muc.MUC;

/**
 * Represents normal chat.
 * 
 * @author alexander.ivanov
 * 
 */
public class RegularChat extends AbstractChat {

	/**
	 * Resource used for contact.
	 */
	private String resource;

	RegularChat(String account, String user) {
		super(account, user);
		resource = null;
	}

	public String getResource() {
		return resource;
	}

	@Override
	public String getTo() {
		if (resource == null)
			return user;
		else
			return user + "/" + resource;
	}

	@Override
	public Type getType() {
		return Type.chat;
	}

	@Override
	protected boolean canSendMessage() {
		if (super.canSendMessage()) {
			if (SettingsManager.securityOtrMode() != SecurityOtrMode.required)
				return true;
			SecurityLevel securityLevel = OTRManager.getInstance()
					.getSecurityLevel(account, user);
			if (securityLevel != SecurityLevel.plain)
				return true;
			try {
				OTRManager.getInstance().startSession(account, user);
			} catch (NetworkException e) {
			}
		}
		return false;
	}

	@Override
	protected String prepareText(String text) {
		text = super.prepareText(text);
		try {
			return OTRManager.getInstance().transformSending(account, user,
					text);
		} catch (OtrException e) {
			LogManager.exception(this, e);
			return null;
		}
	}

	@Override
	protected MessageItem newMessage(String text) {
		return newMessage(
				null,
				text,
				null,
				null,
				false,
				false,
				false,
				false,
				MessageArchiveManager.getInstance().getSaveMode(account, user,
						getThreadId()) != SaveMode.fls, null);
	}

	@Override
	protected MessageItem newMessage(String text, Consigna consigna) {
		return newMessage(
				null,
				text,
				null,
				null,
				false,
				false,
				false,
				false,
				MessageArchiveManager.getInstance().getSaveMode(account, user,
						getThreadId()) != SaveMode.fls, null, consigna);
	}

	@Override
	protected boolean onPacket(final ConnectionItem connectionItem,
			String bareAddress, Packet packet) {
		if (!super.onPacket(connectionItem, bareAddress, packet))
			return false;
		final String resource = Jid.getResource(packet.getFrom());
		if (packet instanceof Presence) {
			final Presence presence = (Presence) packet;
			if (this.resource != null
					&& presence.getType() == Presence.Type.unavailable
					&& this.resource.equals(resource))
				this.resource = null;
		} else if (packet instanceof Message) {

			final Message message = (Message) packet;
			if (message.getType() == Message.Type.error)
				return true;

			MUCUser mucUser = MUC.getMUCUserExtension(message);
			if (mucUser != null && mucUser.getInvite() != null)
				return true;

			String text = message.getBody();
			if (text == null) {
				return true;
			} else if (text.equals("")) {
				return true;
			}

			String thread = message.getThread();
			updateThreadId(thread);
			boolean unencrypted = false;
			try {
				text = OTRManager.getInstance().transformReceiving(account,
						user, text);
			} catch (OtrException e) {
				if (e.getCause() instanceof OTRUnencryptedException) {
					text = ((OTRUnencryptedException) e.getCause()).getText();
					unencrypted = true;
				} else {
					LogManager.exception(this, e);
					// Invalid message received.
					return true;
				}
			}

			// System message received.
			if (text == null)
				return true;
			if (!"".equals(resource))
				this.resource = resource;
			newMessage(
					resource,
					text,
					null,
					Delay.getDelay(message),
					true,
					true,
					unencrypted,
					Delay.isOfflineMessage(Jid.getServer(account), packet),
					MessageArchiveManager.getInstance().getSaveMode(account,
							user, getThreadId()) != SaveMode.fls, null,
					message.getConsigna());
		} else if (packet instanceof StreamInitiation) {
			// // // FileTransferManager manager = new FileTransferManager(
			// // // connectionItem.getConnectionThread().getXMPPConnection());
			// // // manager.addFileTransferListener(new FileTransferListener()
			// {
			// // // public void fileTransferRequest(
			// // // final FileTransferRequest request) {
			// // // new Thread() {
			// // // @Override
			// // // public void run() {
			// // FileTransferManager manager = new FileTransferManager(
			// // connectionItem.getConnectionThread().getXMPPConnection());
			// //
			// // FileTransferRequest request = new FileTransferRequest(manager,
			// // (StreamInitiation) packet);
			// // IncomingFileTransfer transfer = request.accept();
			// // File mf = Environment.getExternalStorageDirectory();
			// // File file = new File(mf.getAbsoluteFile() + "/DCIM/Camera/"
			// // + transfer.getFileName());
			// // try {
			// // transfer.recieveFile(file);
			// // while (!transfer.isDone()) {
			// // try {
			// // Thread.sleep(1000L);
			// // } catch (Exception e) {
			// // Log.e("", e.getMessage());
			// // }
			// // if (transfer.getStatus().equals(Status.error)) {
			// // Log.e("ERROR!!! ", transfer.getError() + "");
			// // }
			// // if (transfer.getException() != null) {
			// // transfer.getException().printStackTrace();
			// // }
			// // }
			// // } catch (Exception e) {
			// // Log.e("", e.getMessage());
			// // }
			// // // };
			// // // }.start();
			// // // }
			// // // });
			// // Thread thread = new Thread() {
			// // public void run() {
			//
			// // ServiceDiscoveryManager sdm = ServiceDiscoveryManager
			// // .getInstanceFor(connection);
			//
			// // if (sdm == null)
			// // sdm = new ServiceDiscoveryManager(connection);
			//
			// // sdm.addFeature("http://jabber.org/protocol/disco#info");
			// //
			// // sdm.addFeature("jabber:iq:privacy");
			//
			// // // Create the file transfer manager
			// // final FileTransferManager managerListner = new
			// // FileTransferManager(
			// // connection);
			//
			// // FileTransferNegotiator.setServiceEnabled(connection, true);
			//
			// // Log.i("File transfere manager", "created");
			//
			// // Create the listener
			// // managerListner
			// // .addFileTransferListener(new FileTransferListener() {
			// // public void fileTransferRequest(
			// // final FileTransferRequest request) {
			//
			// if (((StreamInitiation) packet).getType() ==
			// StreamInitiation.Type.ERROR) {
			// return true;
			// }
			//
			// if (((StreamInitiation) packet).getFile() == null) {
			//
			// // boolean unencrypted = false;
			// //
			// // newMessage(
			// // resource,
			// // "Archivo enviado correctamente",
			// // null,
			// // Delay.getDelay(packet),
			// // true,
			// // true,
			// // unencrypted,
			// // Delay.isOfflineMessage(Jid.getServer(account), packet),
			// // MessageArchiveManager.getInstance().getSaveMode(
			// // account, user, getThreadId()) != SaveMode.fls);
			//
			// return true;
			// }
			//
			// Connection connection = connectionItem.getConnectionThread()
			// .getXMPPConnection();
			//
			// FileTransferManager manager = new
			// FileTransferManager(connection);
			//
			// FileTransferRequest request = new FileTransferRequest(manager,
			// (StreamInitiation) packet);
			//
			// Log.i("Recieve File",
			// "new file transfere request  new file transfere request   new file transfere request");
			//
			// Log.i("file request", "from" + request.getRequestor());
			//
			// IncomingFileTransfer transfer = request.accept();
			//
			// Log.i("Recieve File alert dialog", "accepted");
			// try {
			// // IncomingFileTransfer transfer = request.accept();
			// transfer.recieveFile(new File(Environment
			// .getExternalStorageDirectory()
			// + "/"
			// + Constantes.NAME_FOLDER + "/", transfer.getFileName()));
			// String line;
			// BufferedReader br = new BufferedReader(new FileReader(new File(
			// Environment.getExternalStorageDirectory() + "/"
			// + Constantes.NAME_FOLDER + "/",
			// transfer.getFileName())));
			// while ((line = br.readLine()) != null) {
			// System.out.println(line);
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// try {
			//
			// File fd = new File(Environment.getExternalStorageDirectory()
			// + "/" + Constantes.NAME_FOLDER + "/");
			//
			// if (!fd.exists()) {
			// fd.mkdirs();
			// }
			//
			// File f = new File(Environment.getExternalStorageDirectory()
			// + "/" + Constantes.NAME_FOLDER + "/"
			// + request.getFileName());
			// transfer.recieveFile(f);
			//
			// while (!transfer.isDone() || (transfer.getProgress() < 1)) {
			//
			// Thread.sleep(1000);
			// Log.i("Recieve File alert dialog",
			// "still receiving : " + (transfer.getProgress())
			// + " status " + transfer.getStatus());
			//
			// if (transfer.getStatus().equals(Status.error)) {
			// // Log.i("Error file",
			// // transfer.getError().getMessage());
			// Log.i("Recieve File alert dialog",
			// "cancelling still receiving : "
			// + (transfer.getProgress()) + " status "
			// + transfer.getStatus());
			// transfer.cancel();
			//
			// break;
			// }
			//
			// }
			//
			// if (transfer.isDone()) {
			//
			// String path = f.getAbsolutePath();
			//
			// path = path
			// .replace(Environment.getExternalStorageDirectory()
			// + "/", "");
			//
			// newMessage(
			// resource,
			// "Archivo descargado: " + path,
			// null,
			// Delay.getDelay(packet),
			// true,
			// true,
			// false,
			// Delay.isOfflineMessage(Jid.getServer(account),
			// packet),
			// MessageArchiveManager.getInstance().getSaveMode(
			// account, user, getThreadId()) != SaveMode.fls,
			// f.getPath(), null);
			//
			// }
			//
			// } catch (XMPPException e) {
			//
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// // }
			// // });
			// //
			// // }
			// // };
			// // thread.start();

		}
		return true;
	}

	@Override
	protected void onComplete() {
		super.onComplete();
		sendMessages();
	}

}
