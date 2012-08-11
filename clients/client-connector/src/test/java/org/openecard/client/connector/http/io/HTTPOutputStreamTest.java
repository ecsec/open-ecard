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
import org.openecard.client.common.util.ByteUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPOutputStreamTest {

    private static final byte[] CRLF = new byte[]{(byte) 0x0D, (byte) 0x0A};
    private String testData = "Test data";

    @Test
    public void testWrite() throws Exception {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	HTTPOutputStream instance = new HTTPOutputStream(baos);

	instance.write(testData);
	instance.close();
	Assert.assertEquals(testData.getBytes(), baos.toByteArray());
    }

    @Test
    public void testWriteln() throws Exception {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	HTTPOutputStream instance = new HTTPOutputStream(baos);

	instance.writeln(testData);
	instance.close();
	Assert.assertEquals(ByteUtils.concatenate(testData.getBytes(), CRLF), baos.toByteArray());
    }

}
