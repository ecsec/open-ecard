/*
 * Copyright 2012 Tobias Wich ecsec GmbH
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

package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.*;
import org.openecard.client.common.util.IntegerUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class X509CertificateAttribute extends TLVType {

    private GenericObjectValue<Certificate> value;
    private TLV subject;
    private TLV issuer;
    private Integer serialNumber;


    public X509CertificateAttribute(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv.getChild());

	// first value is validated by GenericObjectValue
	value = new GenericObjectValue<Certificate>(p.next(0), Certificate.class);

	if (p.match(Tag.SequenceTag)) {
	    subject = p.next(0);
	}
	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    issuer = p.next(0);
	}
	if (p.match(Tag.IntegerTag)) {
	    serialNumber = IntegerUtils.toInteger(p.next(0).getValue());
	}
    }


}
