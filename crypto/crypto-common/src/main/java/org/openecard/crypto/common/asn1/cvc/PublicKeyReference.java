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

package org.openecard.crypto.common.asn1.cvc;

import org.openecard.common.util.ByteUtils;


/**
 * See BSI-TR-03110, version 2.10, part 3, section A.6.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PublicKeyReference {

    private byte[] reference;
    // Country Code; Encoding: ISO 3166-1 ALPHA-2; Length: 2F
    private String countryCode;
    // Sequence Number; Encoding: ISO/IEC 8859-1; Length: 9V
    private String holderMnemonic;
    // Sequence Number; Encoding: ISO/IEC 8859-1; Length: 5F
    private String sequenceNumber;

    /**
     * Creates a new public key reference.
     *
     * @param reference Public key reference
     */
    public PublicKeyReference(String reference) {
	this(reference.getBytes());
    }

    /**
     * Creates a new public key reference.
     *
     * @param reference Public key reference
     */
    public PublicKeyReference(byte[] reference) {
	this.reference = reference;
	parse();
    }

    private void parse() {
	final int length = reference.length;

	countryCode = new String(ByteUtils.copy(reference, 0, 2));
	holderMnemonic = new String(ByteUtils.copy(reference, 2, length - 7));
	sequenceNumber = new String(ByteUtils.copy(reference, length - 5, 5));
    }

    /**
     * Returns the country code.
     *
     * @return Country code
     */
    public String getCountryCode() {
	return countryCode;
    }

    /**
     * Returns the holder mnemonic.
     *
     * @return Holder mnemonice
     */
    public String getHolderMnemonic() {
	return holderMnemonic;
    }

    /**
     * Returns the sequence number.
     *
     * @return Sequence number.
     */
    public String getSequenceNumber() {
	return sequenceNumber;
    }

    /**
     * Returns the public key reference as a byte array.
     *
     * @return Byte array
     */
    public byte[] toByteArray() {
	return reference;
    }

    /**
     * Compares the public key reference.
     *
     * @param publicKeyReference PublicKeyReference
     * @return True if they are equal, otherwise false
     */
    public boolean compare(PublicKeyReference publicKeyReference) {
	return compare(publicKeyReference.toByteArray());
    }

    /**
     * Compares the public key reference.
     *
     * @param publicKeyReference PublicKeyReference
     * @return True if they are equal, otherwise false
     */
    public boolean compare(byte[] publicKeyReference) {
	return ByteUtils.compare(reference, publicKeyReference);
    }

    /**
     * Returns the public key reference as a hex string.
     *
     * @return Hex string
     */
    public String toHexString() {
	return ByteUtils.toHexString(reference, true);
    }

    @Override
    public String toString() {
	return new String(reference);
    }

}
