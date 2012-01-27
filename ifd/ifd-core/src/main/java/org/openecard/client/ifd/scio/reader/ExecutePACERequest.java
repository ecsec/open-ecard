/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.ifd.scio.reader;

import java.io.ByteArrayOutputStream;
import org.openecard.client.common.util.ShortUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecutePACERequest {

    public enum Function {
	GetReaderPACECapabilities((byte)1),
	EstablishPACEChannel((byte)2),
	DestroyPACEChannel((byte)3);

	private byte code;

	private Function(byte code) {
	    this.code = code;
	}

	private byte getCode() {
	    return code;
	}
    }


    public ExecutePACERequest(Function f) {
	this.function = f;
    }

    public ExecutePACERequest(Function f, byte[] data) {
	this.function = f;
	this.dataLength = (short) data.length;
	this.data = data;
    }


    private Function function;
    private short dataLength = 0;
    private byte[] data;

    public byte[] toBytes() {
	ByteArrayOutputStream o = new ByteArrayOutputStream();
	o.write(function.getCode());
	// write data length
	byte[] dataLength_bytes = ShortUtils.toByteArray(dataLength);
	for (int i=dataLength_bytes.length-1; i>=0; i--) {
	    o.write(dataLength_bytes[i]);
	}
	// write missing bytes to length field
	for (int i=dataLength_bytes.length; i<2; i++) {
	    o.write(0);
	}
	// write data if there is a positive length
	if (dataLength > 0) {
	    o.write(data, 0, data.length);
	}

	return o.toByteArray();
    }

}
