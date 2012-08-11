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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CRLFInputStream extends FilterInputStream {

    private byte b1 = 0x00;
    private byte b2 = 0x00;

    /**
     * Creates a new CRLFInputStream.
     *
     * @param inputStream InputStream
     */
    public CRLFInputStream(InputStream inputStream) {
	super(inputStream);
    }

    @Override
    public int read() throws IOException {
	return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
	return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
	int res = super.read(b, off, len);

	if (res == -1) {
	    // Return -1 if the end of the stream is reached
	    return -1;
	}

	b2 = b1;
	b1 = b[b.length - 1];

	if (b1 == 0x0A) {
	    if (b2 == 0x0D) {
		// Return -2 if a CRLF is reached
		return -2;
	    }
	}

	return res;
    }

}
