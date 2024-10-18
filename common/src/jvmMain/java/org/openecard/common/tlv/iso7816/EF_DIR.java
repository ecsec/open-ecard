/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.common.tlv.iso7816;

import java.util.LinkedList;
import java.util.List;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.apdu.utils.CardUtils;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;


/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class EF_DIR {

    private static final byte[] EF_DIR_FID = new byte[]{0x2F, 0x00};

    private final TLV tlv;

    private List<byte[]> applicationIdentifiers;
    private List<ApplicationTemplate> applicationTemplates;

    public EF_DIR(TLV tlv) throws TLVException {
	this.tlv = new TLV();
	this.tlv.setTagNumWithClass(Tag.SEQUENCE_TAG.getTagNumWithClass()); // pretend to be a sequence
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

    public static EF_DIR selectAndRead(Dispatcher dispatcher, byte[] slotHandle) throws APDUException, TLVException {
	// Select and read EF.DIR
	byte[] data = CardUtils.selectReadFile(dispatcher, slotHandle, EF_DIR_FID);

	return new EF_DIR(data);
    }


    public List<byte[]> getApplicationIds() {
	return applicationIdentifiers;
    }

    public List<ApplicationTemplate> getApplicationTemplates() {
	return applicationTemplates;
    }

}
