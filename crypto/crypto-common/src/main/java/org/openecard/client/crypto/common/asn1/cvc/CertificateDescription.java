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

import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.StringUtils;

/**
 * Represents the Certificate Description found in a CV-Certificate
 * 
 * @author Dirk Petrautzki <petrautz@hs-coburg.de>
 * 
 */
public class CertificateDescription {

	private final byte[] oid;
	private final descriptionType type;
	private final String issuerName;
	private final String issuerURL;
	private final String subjectName;
	private final String subjectURL;
	private final String termsOfUsage;
	private final byte[] oidPlainFormat = StringUtils.toByteArray("04007f00070301030101");
	public enum descriptionType {
		plainFormat, htmlFormat, pdfFormat
	}

	public CertificateDescription(TLV tlv) {
		if (ByteUtils.compare(oidPlainFormat, tlv.findChildTags(0x06).get(0).getValue())) {
			this.oid = oidPlainFormat;
			this.type = descriptionType.plainFormat;
		} else {
			throw new IllegalArgumentException("Description Type not yet supported");
		}
		issuerName = new String(tlv.findChildTags(161).get(0).findChildTags(12).get(0).getValue());
		issuerURL = new String(tlv.findChildTags(162).get(0).findChildTags(19).get(0).getValue());
		subjectName = new String(tlv.findChildTags(163).get(0).findChildTags(12).get(0).getValue());
		subjectURL = new String(tlv.findChildTags(164).get(0).findChildTags(19).get(0).getValue());
		termsOfUsage = new String(tlv.findChildTags(165).get(0).findChildTags(12).get(0).getValue());
	}

	public byte[] getOid() {
		return oid;
	}

	public descriptionType getType() {
		return type;
	}

	public String getIssuerName() {
		return issuerName;
	}

	public String getIssuerURL() {
		return issuerURL;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public String getSubjectURL() {
		return subjectURL;
	}

	public String getTermsOfUsage() {
		return termsOfUsage;
	}
}
