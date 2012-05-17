package org.openecard.client.crypto.common.asn1.cvc;

import org.openecard.client.common.util.ByteUtils;


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

    public PublicKeyReference(String reference) {
	this(reference.getBytes());
    }

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

    public String getCountryCode() {
	return countryCode;
    }

    public String getHolderMnemonic() {
	return holderMnemonic;
    }

    public String getSequenceNumber() {
	return sequenceNumber;
    }

    public byte[] toByteArray() {
	return reference;
    }

    public boolean equals(PublicKeyReference publicKeyReference) {
	return ByteUtils.compare(reference, publicKeyReference.toByteArray());
    }

    public String toHexString() {
	return ByteUtils.toHexString(reference, true);
    }

    @Override
    public String toString() {
	return new String(reference);
    }
}
