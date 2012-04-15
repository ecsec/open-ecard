/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
