package org.openecard.client.ifd.scio.reader;

import org.openecard.client.common.util.Helper;
import java.io.ByteArrayOutputStream;


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
	byte[] dataLength_bytes = Helper.convertPosIntToByteArray(dataLength);
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
