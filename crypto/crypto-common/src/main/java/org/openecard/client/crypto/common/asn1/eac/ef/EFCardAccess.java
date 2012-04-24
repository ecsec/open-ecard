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
package org.openecard.client.crypto.common.asn1.eac.ef;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.bouncycastle.asn1.ASN1Sequence;
import org.openecard.bouncycastle.asn1.ASN1Set;
import org.openecard.client.crypto.common.asn1.eac.CADomainParameterInfo;
import org.openecard.client.crypto.common.asn1.eac.CAInfo;
import org.openecard.client.crypto.common.asn1.eac.CASecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.CardInfoLocator;
import org.openecard.client.crypto.common.asn1.eac.PACEDomainParameterInfo;
import org.openecard.client.crypto.common.asn1.eac.PACEInfo;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.PrivilegedTerminalInfo;
import org.openecard.client.crypto.common.asn1.eac.SecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.TAInfo;
import org.openecard.client.crypto.common.asn1.eac.TASecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;


/**
 * Implements a EF.CardAccess file.
 * See BSI-TR-03110, Version 2.05, Section A.1.2.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class EFCardAccess {

    private static final Logger logger = Logger.getLogger(EFCardAccess.class.getName());
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
		logger.log(Level.FINE, "Found PACEInfo object identifier");
		PACEInfo pi = new PACEInfo(securityInfo);
		psi.setPACEInfo(pi);
	    } // PACEDoaminParameterInfo (CONDITIONAL)
	    else if (PACEDomainParameterInfo.isPACEObjectIdentifer(oid)) {
		logger.log(Level.FINE, "Found PACEDomainParameterInfo object identifier");
		PACEDomainParameterInfo pdp = new PACEDomainParameterInfo(securityInfo);
		psi.setPACEDomainParameterInfo(pdp);
	    } // ChipAuthenticationInfo (CONDITIONAL)
	    else if (CAInfo.isObjectIdentifier(oid)) {
		logger.log(Level.FINE, "Found ChipAuthenticationInfo object identifier");
		CAInfo ci = new CAInfo(securityInfo);
		csi.setCAInfo(ci);
	    } // ChipAuthenticationDomainParameterInfo (CONDITIONAL)
	    else if (CADomainParameterInfo.isObjectIdentifier(oid)) {
		logger.log(Level.FINE, "Found ChipAuthenticationDomainParameterInfo object identifier");
		CADomainParameterInfo cdp = new CADomainParameterInfo(securityInfo);
		csi.setCADomainParameterInfo(cdp);
	    } // TerminalAuthenticationInfo (CONDITIONAL)
	    else if (EACObjectIdentifier.id_TA.equals(oid)) {
		logger.log(Level.FINE, "Found TerminalAuthenticationInfo object identifier");
		TAInfo ta = new TAInfo(securityInfo);
		tsi.setTAInfo(ta);
	    } // CardInfoLocator (RECOMMENDED)
	    else if (EACObjectIdentifier.id_CI.equals(oid)) {
		logger.log(Level.FINE, "Found CardInfoLocator object identifier");
		cil = CardInfoLocator.getInstance(securityInfo);
	    } // PrivilegedTerminalInfo (CONDITIONAL)
	    else if (EACObjectIdentifier.id_PT.equals(oid)) {
		logger.log(Level.FINE, "Found PrivilegedTerminalInfo object identifier");
		pti = PrivilegedTerminalInfo.getInstance(securityInfo);
	    } else {
		logger.log(Level.WARNING, "Found unknown object identifier: {0}", oid.toString());
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
