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

package org.openecard.client.crypto.common.asn1.eac;

import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.DERIA5String;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CardInfoLocator {

    private String protocol;
    private String url;
    private FileID efCardInfo;

    /**
     * Instantiates a new card info locator.
     *
     * @param seq the ASN1 encoded sequence
     */
    public CardInfoLocator(ASN1Sequence seq) {
	if (seq.size() == 2) {
	    protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    url = DERIA5String.getInstance(seq.getObjectAt(1)).getString();

	} else if (seq.size() == 3) {
	    protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    url = DERIA5String.getInstance(seq.getObjectAt(1)).getString();
	    efCardInfo = FileID.getInstance(seq.getObjectAt(2));
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for CardInfoLocator");
	}
    }

    /**
     * Gets the single instance of CardInfoLocator.
     *
     * @param obj
     * @return single instance of CardInfoLocator
     */
    public static CardInfoLocator getInstance(Object obj) {
	if (obj == null || obj instanceof CardInfoLocator) {
	    return (CardInfoLocator) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new CardInfoLocator((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
	return protocol.toString();
    }

    /**
     * Gets the URL.
     *
     * @return the URL
     */
    public String getURL() {
	return url;
    }

    /**
     * Gets the EFCardInfo fileID.
     *
     * @return the EFCardInfo fileID
     */
    public FileID getEFCardInfo() {
	return efCardInfo;
    }

}
