/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
 ************************************************************************** */
package org.openecard.mobile.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class TermsOfUsageImplTest {

    @Test
    public void serializeRoundTrip() throws IOException, IOException, ClassNotFoundException {
	String inputMime = "mime/type";
	String inputData = "This is just some data";

	TermsOfUsageImpl sut = new TermsOfUsageImpl(inputMime, ByteBuffer.wrap(inputData.getBytes()));

	ByteArrayOutputStream targetOut = new ByteArrayOutputStream();
	ObjectOutputStream out = new ObjectOutputStream(targetOut);
	out.writeObject(sut);
	out.close();

	ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(targetOut.toByteArray()));
	TermsOfUsageImpl result = (TermsOfUsageImpl) in.readObject();

	Assert.assertEquals(result.getMimeType(), inputMime);
	Assert.assertEquals(result.getDataString(), inputData);
    }
}
