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

package org.openecard.crypto.common.asn1.eac;

import org.openecard.bouncycastle.asn1.ASN1OctetString;
import org.openecard.bouncycastle.asn1.ASN1Sequence;


/**
 *
 * @author Moritz Horsch
 */
public final class FileID {

    private byte[] fid;
    private byte[] sfid;

    /**
     * Instantiates a new file id.
     *
     * @param seq the ASN1 encoded sequence
     */
    public FileID(ASN1Sequence seq) {
	if (seq.size() == 1) {
	    fid = ASN1OctetString.getInstance(seq.getObjectAt(0)).getOctets();

	} else if (seq.size() == 2) {
	    fid = ASN1OctetString.getInstance(seq.getObjectAt(0)).getOctets();
	    sfid = ASN1OctetString.getInstance(seq.getObjectAt(1)).getOctets();
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for FileID");
	}
    }

    /**
     * Gets the single instance of FileID.
     *
     * @param obj
     * @return single instance of FileID
     */
    public static FileID getInstance(Object obj) {
	if (obj == null || obj instanceof FileID) {
	    return (FileID) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new FileID((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the file identifier (FID).
     *
     * @return the FID
     */
    public byte[] getFID() {
	return fid;
    }

    /**
     * Gets the short file identifier (SFID).
     *
     * @return the SFID
     */
    public byte[] getSFID() {
	return sfid;
    }

}
