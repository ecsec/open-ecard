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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.crypto.common.asn1.eac;

import java.security.spec.AlgorithmParameterSpec;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CADomainParameter {

    private AlgorithmParameterSpec domainParameter;
    private CASecurityInfos csi;
    private CAInfo ci;

    /**
     * Create new CADomainParameter.
     *
     * @param csi CASecurityInfos
     */
    public CADomainParameter(CASecurityInfos csi) {
	this.csi = csi;
	this.ci = csi.getCAInfo();

	loadParameters();
    }

    /**
     * Create new CADomainParameter.
     *
     * @param csi CASecurityInfos
     * @param domainParameter AlgorithmParameterSpec
     */
    public CADomainParameter(CASecurityInfos csi, AlgorithmParameterSpec domainParameter) {
	this.csi = csi;
	this.ci = csi.getCAInfo();
	this.domainParameter = domainParameter;
    }

    /**
     * Returns the domain parameter.
     *
     * @return Domain parameter
     */
    public AlgorithmParameterSpec getParameter() {
	return domainParameter;
    }

    /**
     * Sets the domain parameter.
     *
     * @param domainParameter Domain parameter
     */
    public void setParameter(AlgorithmParameterSpec domainParameter) {
	this.domainParameter = domainParameter;
    }

    /**
     * Checks if the protocol identifier indicates Diffie-Hellman.
     *
     * @return True if Diffie-Hellman is used, otherwise false
     */
    public boolean isDH() {
	return ci.isDH();
    }

    /**
     * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
     *
     * @return True if elliptic curve Diffie-Hellman is used, otherwise false
     */
    public boolean isECDH() {
	return ci.isECDH();
    }

    private void loadParameters() {

	CADomainParameterInfo cdp = csi.getCADomainParameterInfo();
	AlgorithmIdentifier ai = cdp.getDomainParameter();

	if (ai.getObjectIdentifier().equals(EACObjectIdentifier.standardized_Domain_Parameters)) {
	    int index = Integer.parseInt(ai.getParameters().toString());
	    domainParameter = new StandardizedDomainParameters(index).getParameter();
	} else {
	    domainParameter = new ExplicitDomainParameters(ai).getParameter();
	}

	if (domainParameter == null) {
	    throw new IllegalArgumentException("Cannot load domain parameter");
	}
    }

}
