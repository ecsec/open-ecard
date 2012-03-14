/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.ifd;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Bluetooht-implementation for {@link SerialConnection}
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
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
