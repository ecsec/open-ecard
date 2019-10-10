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

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich
 */
public class HandlePrinter {

    public static void printHandle(Writer w, ConnectionHandleType handle) throws IOException {
	ChannelHandleType channel = handle.getChannelHandle();
	String session = null;
	if (channel != null) {
	    session = channel.getSessionIdentifier();
	}
	byte[] ctx = handle.getContextHandle();
	String ifdname = handle.getIFDName();
	BigInteger slotIdx = handle.getSlotIndex();
	byte[] slotHandle = handle.getSlotHandle();
	ConnectionHandleType.RecognitionInfo rec = handle.getRecognitionInfo();
	String cardType = null;
	if (rec != null) {
	    cardType = rec.getCardType();
	}

	w.write("ConnectionHandle:");
	if (session != null) {
	    w.write("\n  Session: ");
	    w.write(session);
	}
	if (ctx != null) {
	    w.write("\n  ContextHandle: ");
	    w.write(ByteUtils.toHexString(ctx));
	}
	if (ifdname != null) {
	    w.write("\n  IFDName: ");
	    w.write(ifdname);
	    if (slotIdx != null) {
		w.write("  SlotIndex: ");
		w.write(slotIdx.toString());
	    }
	}
	if (slotHandle != null) {
	    w.write("\n  SlotHandle: ");
	    w.write(ByteUtils.toHexString(slotHandle));
	}
	if (cardType != null) {
	    w.write("\n  CardType: ");
	    w.write(cardType);
	}
    }

    public static String printHandle(ConnectionHandleType handle) {
	StringWriter w = new StringWriter(200);
	try {
	    printHandle(w, handle);
	} catch (IOException ex) {
	    // hokum, StringWriter has no IOException
	}
	return w.toString();
    }

}
