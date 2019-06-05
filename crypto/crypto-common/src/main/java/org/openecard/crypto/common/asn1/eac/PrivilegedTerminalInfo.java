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

package org.openecard.crypto.common.asn1.eac;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;


/**
 *
 * @author Moritz Horsch
 */
public final class PrivilegedTerminalInfo {

    private String protocol;
    private SecurityInfos privilegedTerminalInfo;

    /**
     * Instantiates a new privileged terminal info.
     *
     * @param seq ASN1 encoded sequence
     */
    public PrivilegedTerminalInfo(ASN1Sequence seq) {
	if (seq.size() == 2) {
	    protocol = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0)).toString();
	    privilegedTerminalInfo = SecurityInfos.getInstance((ASN1Set) seq.getObjectAt(1));
	} else {
	    throw new IllegalArgumentException("Sequence wrong size for PrivilegedTerminalInfo");
	}
    }

    /**
     * Gets the single instance of PrivilegedTerminalInfo.
     *
     * @param obj
     * @return single instance of PrivilegedTerminalInfo
     */
    public static PrivilegedTerminalInfo getInstance(Object obj) {
	if (obj == null || obj instanceof PrivilegedTerminalInfo) {
	    return (PrivilegedTerminalInfo) obj;
	} else if (obj instanceof ASN1Sequence) {
	    return new PrivilegedTerminalInfo((ASN1Sequence) obj);
	}

	throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
	return protocol;
    }

    /**
     * Gets the privileged terminal info.
     *
     * @return the privileged terminal info
     */
    public SecurityInfos getPrivilegedTerminalInfo() {
	return privilegedTerminalInfo;
    }

}
