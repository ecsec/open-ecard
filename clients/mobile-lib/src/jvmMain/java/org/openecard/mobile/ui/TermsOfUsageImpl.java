/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.openecard.mobile.activation.TermsOfUsage;

/**
 *
 * @author Tobias Wich
 */
class TermsOfUsageImpl implements TermsOfUsage {

    private transient ByteBuffer data;
    private final String mimeType;

    public TermsOfUsageImpl(String mimeType, ByteBuffer data) {
	this.data = data;
	this.mimeType = mimeType;
    }

    @Override
    public ByteBuffer getDataBytes() {
	return data;
    }

    @Override
    public String getDataString() {
	return new String(data.array(), StandardCharsets.UTF_8);
    }

    @Override
    public String getMimeType() {
	return mimeType;
    }

    @Override
    public boolean isHtml() {
	return "text/html".equals(mimeType);
    }

    @Override
    public boolean isPdf() {
	return "application/pdf".equals(mimeType);
    }

    @Override
    public boolean isText() {
	return "text/plain".equals(mimeType);
    }

    private void writeObject(ObjectOutputStream out)
	    throws IOException {

	out.defaultWriteObject();
	final ByteBuffer dataCopy = data.slice();
	final int capacity = dataCopy.capacity();

	out.writeInt(capacity);
	byte[] raw = new byte[capacity];
	dataCopy.get(raw);
	out.write(raw);
    }

    private void readObject(ObjectInputStream in)
	    throws IOException, ClassNotFoundException {
	in.defaultReadObject();

	//read buffer data and wrap with ByteBuffer
	int bufferSize = in.readInt();
	byte[] buffer = new byte[bufferSize];
	in.read(buffer, 0, bufferSize);
	this.data = ByteBuffer.wrap(buffer, 0, bufferSize);
    }
}
