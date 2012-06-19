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

import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.common.util.StringUtils;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EF_DIR {

    private final TLV tlv;

    private List<byte[]> applicationIdentifiers;
    private List<ApplicationTemplate> applicationTemplates;

    public EF_DIR(TLV tlv) throws TLVException {
	this.tlv = new TLV();
	this.tlv.setTagNumWithClass(Tag.SequenceTag.getTagNumWithClass()); // pretend to be a sequence
	this.tlv.setChild(tlv);

	Parser p = new Parser(this.tlv.getChild());
	applicationIdentifiers = new LinkedList<byte[]>();
	applicationTemplates = new LinkedList<ApplicationTemplate>();
	while (p.match(0x61) || p.match(0x4F)) {
	    if (p.match(0x61)) {
		applicationTemplates.add(new ApplicationTemplate(p.next(0)));
	    } else if (p.match(0x4F)) {
		applicationIdentifiers.add(p.next(0).getValue());
	    }
	}
	if (p.next(0) != null) {
	    throw new TLVException("Unrecognised element in EF.DIR.");
	}
    }

    public EF_DIR(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public static EF_DIR selectAndRead(IFD ifd, byte[] slotHandle) throws WSException, TLVException {
	// select EF.DIR and eval FCP
	Transmit t = CardCommands.Select.makeTransmit(slotHandle, CardCommands.Select.EF_FCP(StringUtils.toByteArray("2F00")));
	TransmitResponse tr = ifd.transmit(t);
	WSHelper.checkResult(tr);

	byte[] fcpData = tr.getOutputAPDU().get(0);
	fcpData = CardCommands.getDataFromResponse(fcpData);
	FCP fcp = new FCP(fcpData);

	if (fcp.getDataElements().isTransparent()) {
	    return readAsTransparent(ifd, slotHandle);
	} else if (fcp.getDataElements().isLinear()) {
	    return readAsRecords(ifd, slotHandle);
	} else {
	    return readAsAny(ifd, slotHandle);
	}
    }

    public static EF_DIR readAsAny(IFD ifd, byte[] slotHandle) throws WSException, TLVException {
	try {
	    return readAsTransparent(ifd, slotHandle);
	} catch (Exception ex) {
	    return readAsRecords(ifd, slotHandle);
	}
    }

    public static EF_DIR readAsRecords(IFD ifd, byte[] slotHandle) throws WSException, TLVException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();

	boolean done = false;
	byte i=1;
	while (!done) {
	    Transmit t = CardCommands.Read.makeTransmit(slotHandle, CardCommands.Read.recordNumber(i));
	    TransmitResponse tr = ifd.transmit(t);
	    try {
		WSHelper.checkResult(tr);
		try {
		    out.write(CardCommands.getDataFromResponse(tr.getOutputAPDU().get(0)));
		} catch (IOException ex) {
		    // what could possibly go wrong?!?
		}
	    } catch (WSException ex) {
		// check if end of records has been reached, or something bad happened
		if (!tr.getOutputAPDU().isEmpty() && Arrays.equals(new byte[] {(byte)0x6A, (byte)0x83}, tr.getOutputAPDU().get(0))) {
		    done = true;
		} else {
		    throw ex;
		}
	    }

	    i++; // at most 255 records in file
	    if (i == 0xFF) {
		done = true;
	    }
	}

	byte[] data = out.toByteArray();
	if (data.length == 0) {
	    // no data is most certainly wrong
	    throw new TLVException("No data found in EF.DIR.");
	}
	return new EF_DIR(data);
    }

    public static EF_DIR readAsTransparent(IFD ifd, byte[] slotHandle) throws WSException, TLVException {
	Transmit t = CardCommands.Read.makeTransmit(slotHandle, CardCommands.Read.binary());
	TransmitResponse tr = ifd.transmit(t);
	WSHelper.checkResult(tr);

	// TODO: what is the code for more data available?

	return new EF_DIR(CardCommands.getDataFromResponse(tr.getOutputAPDU().get(0)));
    }



    public List<byte[]> getApplicationIds() {
	return applicationIdentifiers;
    }

    public List<ApplicationTemplate> getApplicationTemplates() {
	return applicationTemplates;
    }

}
