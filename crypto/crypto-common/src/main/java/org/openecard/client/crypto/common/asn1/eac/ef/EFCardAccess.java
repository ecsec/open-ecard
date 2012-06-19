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

package org.openecard.client.crypto.common.asn1.eac.ef;

import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.ASN1Set;
import org.openecard.client.crypto.common.asn1.eac.*;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a EF.CardAccess file.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.2.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class EFCardAccess {

    private static final Logger _logger = LoggerFactory.getLogger(EFCardAccess.class);

    private SecurityInfos sis;
    private PACESecurityInfos psi;
    private TASecurityInfos tsi;
    private CASecurityInfos csi;
    private CardInfoLocator cil;
    private PrivilegedTerminalInfo pti;

    /**
     * Creates a new EF.CardAccess.
     *
     * @param sis SecurityInfos
     */
    public EFCardAccess(SecurityInfos sis) {
	this.sis = sis;
	decodeSecurityInfos();
    }

    /**
     * Creates a new EF.CardAccess.
     *
     * @param sis SecurityInfos
     */
    public EFCardAccess(byte[] sis) {
	this.sis = SecurityInfos.getInstance(sis);
	decodeSecurityInfos();
    }

    /**
     * Decode the SecurityInfos.
     */
    private void decodeSecurityInfos() {
	final ASN1Set securityinfos = sis.getSecurityInfos();
	final int length = securityinfos.size();

	psi = new PACESecurityInfos();
	tsi = new TASecurityInfos();
	csi = new CASecurityInfos();

	for (int i = 0; i < length; i++) {
	    ASN1Sequence securityInfo = (ASN1Sequence) securityinfos.getObjectAt(i);
	    String oid = securityInfo.getObjectAt(0).toString();

	    // PACEInfo (REQUIRED)
	    if (PACEInfo.isPACEObjectIdentifer(oid)) {
		_logger.debug("Found PACEInfo object identifier");
		PACEInfo pi = new PACEInfo(securityInfo);
		psi.addPACEInfo(pi);
	    } // PACEDoaminParameterInfo (CONDITIONAL)
	    else if (PACEDomainParameterInfo.isPACEObjectIdentifer(oid)) {
		_logger.debug("Found PACEDomainParameterInfo object identifier");
		PACEDomainParameterInfo pdp = new PACEDomainParameterInfo(securityInfo);
		psi.addPACEDomainParameterInfo(pdp);
	    } // ChipAuthenticationInfo (CONDITIONAL)
	    else if (CAInfo.isObjectIdentifier(oid)) {
		_logger.debug("Found ChipAuthenticationInfo object identifier");
		CAInfo ci = new CAInfo(securityInfo);
		csi.addCAInfo(ci);
	    } // ChipAuthenticationDomainParameterInfo (CONDITIONAL)
	    else if (CADomainParameterInfo.isObjectIdentifier(oid)) {
		_logger.debug("Found ChipAuthenticationDomainParameterInfo object identifier");
		CADomainParameterInfo cdp = new CADomainParameterInfo(securityInfo);
		csi.addCADomainParameterInfo(cdp);
	    } // TerminalAuthenticationInfo (CONDITIONAL)
	    else if (EACObjectIdentifier.id_TA.equals(oid)) {
		_logger.debug("Found TerminalAuthenticationInfo object identifier");
		TAInfo ta = new TAInfo(securityInfo);
		tsi.addTAInfo(ta);
	    } // CardInfoLocator (RECOMMENDED)
	    else if (EACObjectIdentifier.id_CI.equals(oid)) {
		_logger.debug("Found CardInfoLocator object identifier");
		cil = CardInfoLocator.getInstance(securityInfo);
	    } // PrivilegedTerminalInfo (CONDITIONAL)
	    else if (EACObjectIdentifier.id_PT.equals(oid)) {
		_logger.debug("Found PrivilegedTerminalInfo object identifier");
		pti = PrivilegedTerminalInfo.getInstance(securityInfo);
	    } else {
		System.out.println(oid.toString());
		_logger.debug("Found unknown object identifier: {}", oid.toString());
	    }
	}
    }

    /**
     * Gets the PACESecurityInfos.
     *
     * @return PACESecurityInfos
     */
    public PACESecurityInfos getPACESecurityInfos() {
	return psi;
    }

    /**
     * Gets the TASecurityInfos.
     *
     * @return TASecurityInfos
     */
    public TASecurityInfos getTASecurityInfos() {
	return tsi;
    }

    /**
     * Gets the CASecurityInfos.
     *
     * @return CASecurityInfos
     */
    public CASecurityInfos getCASecurityInfos() {
	return csi;
    }

    /**
     * Gets the CardInfoLocator.
     *
     * @return CardInfoLocator
     */
    public CardInfoLocator getCardInfoLocator() {
	return cil;
    }

    /**
     * Gets the PrivilegedTerminalInfo.
     *
     * @return PrivilegedTerminalInfo
     */
    public PrivilegedTerminalInfo getPrivilegedTerminalInfo() {
	return pti;
    }

}
