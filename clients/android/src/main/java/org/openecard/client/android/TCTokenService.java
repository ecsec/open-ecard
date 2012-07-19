/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.android;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * This Service listens to connection on localhost:24727 and sends an intent to
 * start the open ecard app when an incoming request receives.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class TCTokenService extends Service implements Runnable {

    private static final int port = 24727;
    private final IBinder mBinder = new MyBinder();
    private static ServerSocket serverSocket;
    private static Socket socket = null;
    private static DataInputStream dataInputStream = null;
    private static DataOutputStream dataOutputStream = null;
    
    public void run() {
	try {
	    serverSocket = new ServerSocket(port);
	    serverSocket.setSoTimeout(0);
	    while (true) {
		socket = serverSocket.accept();
		SocketAddress remote = socket.getRemoteSocketAddress();
		// TODO check that only local connections get accepted
		System.out.println("remote address: " + remote.toString());
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		byte[] buffer = new byte[70000];

		int length = bis.read(buffer);

		try {
		    if (length > 0) {
			// build uri for intent
			String url = new String(Arrays.copyOf(buffer, length));
			url = url.substring(url.indexOf("/eID"), url.indexOf("HTTP") - 1);
			String prefix = "http://localhost:24727";
			Uri uri = Uri.parse(prefix + url);

			// build intent to start open ecard app
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			intent.addCategory("android.intent.category.BROWSABLE");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
		    }
		} catch (Exception e) {
		    // TODO
		    e.printStackTrace();
		} finally {
		    socket.close();
		}

	    }
	} catch (IOException e) {
	    // TODO
	    e.printStackTrace();
	} finally {
	    try {
		if (socket != null)
		    socket.close();
	    } catch (IOException e) {
	    }
	    try {
		if (dataInputStream != null)
		    dataInputStream.close();
	    } catch (IOException e) {
	    }
	    try {
		if (dataOutputStream != null)
		    dataOutputStream.close();
	    } catch (IOException e) {
	    }
	}

    }

    // for pre-2.0 devices
    @Override
    public void onStart(Intent intent, int startId) {
	Thread t = new Thread(this);
	t.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Thread t = new Thread(this);
	t.start();
	return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    public class MyBinder extends Binder {
	TCTokenService getService() {
	    return TCTokenService.this;
	}
    }

}
