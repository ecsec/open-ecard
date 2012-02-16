/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.crypto.common.asn1.cvc;

import java.util.Date;
import org.openecard.client.common.tlv.TLV;

/**
 * Represents a CV-Certificate
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class CardVerifiableCertificate {

	/** Authentication Template */
	private static final int TAG_AT = 0x67;
	/** CV Certificate */
	private static final int TAG_CVC = 0x7F21;
	/** Certificate Body */
	private static final int TAG_BODY = 0x7F4E;
	/** Certificate Profile Identifier */
	private static final int TAG_CPI = 0x5F29;
	/** Certification Authority Reference */
	private static final int TAG_CAR = 0x42;
	/** Public Key */
	private static final int TAG_PUK = 0x7F49;
	/** Prime Modulus */
	private static final int TAG_ECC_P = 0x81;
	/** First coefficient a */
	private static final int TAG_ECC_A = 0x82;
	/** Second coefficient b */
	private static final int TAG_ECC_B = 0x83;
	/** Base Point G */
	private static final int TAG_ECC_G = 0x84;
	/** Order of the base point */
	private static final int TAG_ECC_N = 0x85;
	/** Public Point y */
	private static final int TAG_ECC_Q = 0x86;
	/** Cofactor f */
	private static final int TAG_ECC_H = 0x87;
	/** Certificate Holder Reference */
	private static final int TAG_CHR = 0x5F20;
	/** Certificate Holder Authorisation Template */
	private static final int TAG_CHAT = 0x7F4C;
	/** Certificate Extension */
	private static final int TAG_EXTN = 0x65;
	/** Certificate Effective Date */
	private static final int TAG_CED = 0x5F25;
	/** Certificate Expiration Date */
	private static final int TAG_CXD = 0x5F24;
	/** Signature */
	private static final int TAG_SIG = 0x5F37;
	private byte version;
	private String CAR;

	private byte[] oid;
	private byte[] publicKey;
	private byte[] certificateHolderReference;
	private CHAT certificateHolderAuthorizationTemplate;
	private Date certificateEffectiveDate;
	private Date certificateExpirationDate;
	private byte[] certificateExtensions;
	private byte[] body;
	
	public byte getVersion() {
		return version;
	}

	public String getCAR() {
		return CAR;
	}

	public byte[] getOid() {
		return oid;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public byte[] getCertificateHolderReference() {
		return certificateHolderReference;
	}

	public CHAT getCertificateHolderAuthorizationTemplate() {
		return certificateHolderAuthorizationTemplate;
	}

	public Date getCertificateEffectiveDate() {
		return certificateEffectiveDate;
	}

	public Date getCertificateExpirationDate() {
		return certificateExpirationDate;
	}

	public byte[] getCertificateExtensions() {
		return certificateExtensions;
	}

	public byte[] getSignature() {
		return signature;
	}
	
	public byte[] getBody() {
		return body;
	}

	private byte[] signature;

	public CardVerifiableCertificate(TLV tlv) {
	    	body = tlv.getValue();
		version = tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CPI).get(0).getValue()[0];
		CAR = new String(tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CAR).get(0).getValue());
		publicKey = tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_PUK).get(0).getValue();
		certificateHolderReference = tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CHR).get(0).getValue();
		certificateHolderAuthorizationTemplate = new CHAT(tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CHAT).get(0));
		byte[] encDate = tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CED).get(0).getValue();
		int year = 100 + encDate[0] * 10 + encDate[1];
		int month = encDate[2] * 10 + encDate[3] - 1;
		int day = encDate[4]*10 + encDate[5];
		certificateEffectiveDate = new Date(year, month, day);
		encDate = tlv.findChildTags(TAG_BODY).get(0).findChildTags(TAG_CXD).get(0).getValue();
		year = 100 + encDate[0] * 10 + encDate[1];
		month = encDate[2] * 10 + encDate[3] - 1;
		day = encDate[4]*10 + encDate[5];
		certificateExpirationDate = new Date(year, month, day);
		/* ignore the remaining fields for now */
	}

}
