/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class SecurityInfos {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityInfos.class);

    private final ASN1Set securityInfos;

    /**
     * Gets the single instance of SecurityInfos.
     *
     * @param obj
     * @return single instance of SecurityInfos
     */
    public static SecurityInfos getInstance(Object obj) {
	if (obj instanceof SecurityInfo) {
	    return (SecurityInfos) obj;
	} else if (obj instanceof ASN1Set) {
	    return new SecurityInfos((ASN1Set) obj);
	} else if (obj instanceof byte[]) {
	    return getInstance(new ByteArrayInputStream((byte[]) obj));
	} else if (obj instanceof InputStream) {
	    try {
		ASN1StreamParser sp = new ASN1StreamParser((InputStream) obj);
		ASN1Encodable enc = sp.readObject();
		return getInstance(enc.toASN1Primitive());
	    } catch (IOException e) {
		LOG.error("Cannot parse SecurityInfos", e);
	    }
	}
	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass());
    }

    /**
     * Instantiates a new set of SecurityInfos.
     *
     * @param seq the ASN1 encoded SecurityInfos set
     */
    private SecurityInfos(ASN1Set seq) {
	securityInfos = seq;
    }

    /**
     * Gets the SecurityInfos.
     *
     * @return the SecurityInfos
     */
    public ASN1Set getSecurityInfos() {
	return securityInfos;
    }

}
