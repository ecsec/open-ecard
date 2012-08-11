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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPInputStreamTest {

    private static final byte[] CRLF = new byte[]{(byte) 0x0D, (byte) 0x0A};

    @Test
    public void testReadLine() throws IOException {

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	baos.write("GET /test.html HTTP/1.1".getBytes());
	baos.write(CRLF);
	baos.write("Allow: text/html".getBytes());
	baos.write(CRLF);
	baos.write(CRLF);

	ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	HTTPInputStream instance = new HTTPInputStream(bais);

	assertEquals("GET /test.html HTTP/1.1", instance.readLine());
	assertEquals("Allow: text/html", instance.readLine());
	assertEquals("", instance.readLine());
	assertNull(instance.readLine());
	assertNull(instance.readLine());
    }

}
