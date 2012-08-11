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

package org.openecard.client.connector.http.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openecard.client.connector.http.HTTPConstants;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class HTTPOutputStream {

    private ByteArrayOutputStream buffer;
    private OutputStream outputStream;

    /**
     * Creates a new HTTPOutputStream.
     *
     * @param outputStream OutputStream
     */
    public HTTPOutputStream(OutputStream outputStream) {
	this.outputStream = outputStream;
	this.buffer = new ByteArrayOutputStream();
    }

    /**
     * Writes the data to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void write(byte[] data) throws IOException {
	buffer.write(data);
    }

    /**
     * Writes the data to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void write(String data) throws IOException {
	write(data.getBytes(HTTPConstants.CHARSET));
    }

    /**
     * Writes the data to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void write(int data) throws IOException {
	write(String.valueOf(data));
    }

    /**
     * Writes a CRLF to the stream.
     *
     * @throws IOException
     */
    public void writeln() throws IOException {
	write(HTTPConstants.CRLF);
    }

    /**
     * Writes the data and appends a CRLF to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public synchronized void writeln(byte[] data) throws IOException {
	write(data);
	writeln();
    }

    /**
     * Writes the data and appends a CRLF to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void writeln(String data) throws IOException {
	writeln(data.getBytes(HTTPConstants.CHARSET));
    }

    /**
     * Writes the data and appends a CRLF to the stream.
     *
     * @param data Data
     * @throws IOException
     */
    public void writeln(int data) throws IOException {
	writeln(String.valueOf(data));
    }

    /**
     * Closes the stream.
     *
     * @throws IOException
     */
    public synchronized void close() throws IOException {
	outputStream.write(buffer.toByteArray());
	outputStream.flush();
	outputStream.close();
    }

}
