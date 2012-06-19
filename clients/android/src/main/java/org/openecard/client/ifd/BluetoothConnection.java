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

package org.openecard.client.ifd;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;


/**
 * Bluetooth-implementation for {@link SerialConnection}
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class BluetoothConnection implements SerialConnection {

    private final BluetoothSocket mmSocket;
    private final BufferedInputStream bis;
    private final BufferedOutputStream bos;
    byte[] buffer = new byte[8192];
    private static final UUID BLUETOOTH_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public BluetoothConnection(BluetoothDevice device) throws Throwable {
	mmSocket = device.createInsecureRfcommSocketToServiceRecord(BLUETOOTH_UUID);
	mmSocket.connect();
	bis = new BufferedInputStream(mmSocket.getInputStream());
	bos = new BufferedOutputStream(mmSocket.getOutputStream());
    }

    public byte[] transmit(byte[] b) throws IOException {
	bos.write(b);
	bos.write(new byte[] { '\r', '\n', '\r', '\n' });
	bos.flush();
	StringBuffer sb = new StringBuffer();
	do {
	    int bytes = bis.read(buffer);
	    sb.append(new String(Arrays.copyOf(buffer, bytes)));
	} while (!sb.toString().endsWith("\r\n\r\n"));
	return sb.toString().getBytes();
    }

}
