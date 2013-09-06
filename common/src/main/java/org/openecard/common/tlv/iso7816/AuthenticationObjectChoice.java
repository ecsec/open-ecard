/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.Tag;
import org.openecard.common.tlv.TagClass;


/**
 * The class implements a data type which represents the ASN.1 notation of the AuthenticationObjectChoice in ISO7816-15.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AuthenticationObjectChoice extends TLVType {

    private GenericAuthenticationObject<PasswordAttributes> pwd; // PasswordAttributes
    private GenericAuthenticationObject<TLV> biometricTemplate; // BiometricAttributes
    private GenericAuthenticationObject<TLV> authKey; // AuthKeyAttributes
    private GenericAuthenticationObject<TLV> external; // ExternalAuthObjectAttributes
    private TLV ext;

    /**
     * The constructor parses the input {@link TLV} to set the element which is chosen.
     *
     * @param tlv The {@link TLV} which represents the AuthenticationObjectChoice.
     * @throws TLVException
     */
    public AuthenticationObjectChoice(TLV tlv) throws TLVException {
	super(tlv);

	Parser p = new Parser(tlv);

	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    pwd = new GenericAuthenticationObject<PasswordAttributes>(p.next(0), PasswordAttributes.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    biometricTemplate = new GenericAuthenticationObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 1))) {
	    authKey = new GenericAuthenticationObject<TLV>(p.next(0), TLV.class);
	} else if (p.match(new Tag(TagClass.CONTEXT, false, 2))) {
	    external = new GenericAuthenticationObject<TLV>(p.next(0), TLV.class);
	} else {
	    ext = p.next(0);
	}
    }

    public GenericAuthenticationObject<PasswordAttributes> getPasswordObject() {
	return pwd;
    }

    public GenericAuthenticationObject<TLV> getBiometricTemplate() {
	return biometricTemplate;
    }

    public GenericAuthenticationObject<TLV> getAuthenticationKey() {
	return authKey;
    }

    public GenericAuthenticationObject<TLV> getExternalAuthObject() {
	return external;
    }

    public TLV getFutureExtension() {
	return ext;
    }

}
