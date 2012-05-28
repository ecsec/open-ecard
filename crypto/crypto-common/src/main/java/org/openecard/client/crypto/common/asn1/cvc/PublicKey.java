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

package org.openecard.client.crypto.common.asn1.cvc;

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;


/**
 * See BSI-TR-03110, version 2.10, part 3, section D.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class PublicKey {

    /**
     * Tag for object identifiers.
     */
    protected static final int OID_TAG = 0x06;

    /**
     * Creates a new public key.
     *
     * @param key Key
     * @return Public key
     * @throws Exception
     */
    public static PublicKey getInstance(byte[] key) throws Exception {
	return getInstance(TLV.fromBER(key));
    }

    /**
     * Creates a new public key.
     *
     * @param key Key
     * @return Public key
     * @throws Exception
     */
    public static PublicKey getInstance(TLV key) throws Exception {
	try {

	    String oid = ObjectIdentifierUtils.toString(key.findChildTags(OID_TAG).get(0).getValue());

	    if (oid.startsWith(TAObjectIdentifier.id_TA_ECDSA)) {
		return new ECPublicKey(key);
	    } else if (oid.startsWith(TAObjectIdentifier.id_TA_RSA)) {
		return new RSAPublicKey(key);
	    } else {
		throw new IllegalArgumentException("Cannot handle object identifier");
	    }
	} catch (Exception e) {
	    throw new IllegalArgumentException("Malformed public key: " + e.getMessage());
	}
    }

    /**
     * Compares the public key.
     *
     * @param pk PublicKey
     * @return True if they are equal, otherwise false
     */
    public boolean equals(PublicKey pk) {
	try {
	    return ByteUtils.compare(getTLVEncoded().toBER(), pk.getTLVEncoded().toBER());
	} catch (TLVException ignore) {
	    return false;
	}
    }

    /**
     * Returns the object identifier.
     *
     * @return Object identifier
     */
    public abstract String getObjectIdentifier();

    /**
     * Returns the TLV encoded key.
     *
     * @return TLV encoded key
     */
    public abstract TLV getTLVEncoded();

}
