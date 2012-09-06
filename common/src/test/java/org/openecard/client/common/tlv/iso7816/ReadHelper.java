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

package org.openecard.client.common.tlv.iso7816;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ReadHelper {

    public static TLV readCIAFile(String name) throws IOException, TLVException {
	String path = "/df.cia/" + name;
	InputStream ins = ReadHelper.class.getResourceAsStream(path);
	ByteArrayOutputStream outs = new ByteArrayOutputStream(ins.available());

	int next;
	while ((next = ins.read()) != -1) {
	    outs.write((byte)next);
	}

	byte[] resultBytes = outs.toByteArray();
	TLV result = TLV.fromBER(resultBytes);
	return result;
    }

}
