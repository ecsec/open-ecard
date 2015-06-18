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

package org.openecard.common.tlv.iso7816;

import java.io.UnsupportedEncodingException;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;


/**
 * The class models a FCI data type (see also ISO 7816-4).
 *
 * @author Hans-Martin Haase
 */
public class FCI {

    /**
     * The TLV representing the FCI.
     */
    private final TLV tlv;

    /**
     * The FCP data contained in the FCI.
     */
    private FCP fcpData;

    /**
     * The FMD data contained in the FCI.
     */
    private FMD fmdData;

    /**
     * The constructor extracts the FMD and FCP data from the FCI and stores them in the global variables.
     *
     * @param fciTLV A TLV representing the FCI.
     * @throws TLVException Thrown if the creation of the FCP or FMD object failed.
     * @throws UnsupportedEncodingException Thrown if the URL contained in the FMD can't be encoded.
     */
    public FCI(TLV fciTLV) throws TLVException, UnsupportedEncodingException {
	tlv = fciTLV;

	// there are two possibilities for the structure of this object
	// 1. explicite FCI template which contains FCP and/or FMD templates.
	if (fciTLV.getTagNumWithClass() == 0x6F) {
	    TLV child = fciTLV.getChild();
	    if (child.getTagNumWithClass() == 0x62) {
		fcpData = new FCP(child);
		child = child.getNext();
	    } else {
		fcpData = null;
	    }

	    if (child.getTagNumWithClass() == 0x64) {
		fmdData = new FMD(child);
	    } else {
		fmdData = null;
	    }
	} else if (fciTLV.getTagNumWithClass() == 0x62) {
	    // 2. no FCI template just FCP and FMD templates
	    fcpData = new FCP(fciTLV);

	    if (fciTLV.hasNext()) {
		TLV next = fciTLV.getNext();
		if (next.getTagNumWithClass() == 0x64) {
		    fmdData = new FMD(next);
		}
	    }
	} else if (fciTLV.getTagNumWithClass() == 0x64) {
	    fmdData = new FMD(fciTLV);
	    fcpData = null;
	} else {
	    String msg = "Unknown FCI tag discovered.";
	    throw new TLVException(msg);
	}
    }

    /**
     * Get the FCP data contained in the FCI if available.
     * If no FCP data is available null is returned.
     *
     * @return FCP if contained in the FCI or null.
     */
    public FCP getFCPData() {
	return fcpData;
    }

    /**
     * Get the FMD data contained in the FCI if available.
     * If no FMD data is available null is returned.
     *
     * @return FMD if contained in the FCI or null.
     */
    public FMD getFMDData() {
	return fmdData;
    }
}
