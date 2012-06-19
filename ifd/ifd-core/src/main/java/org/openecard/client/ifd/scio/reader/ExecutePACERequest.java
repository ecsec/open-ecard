/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
