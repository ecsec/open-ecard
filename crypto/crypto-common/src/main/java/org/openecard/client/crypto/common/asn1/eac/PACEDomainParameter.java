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

package org.openecard.client.crypto.common.asn1.eac;

import java.security.spec.AlgorithmParameterSpec;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PACEDomainParameter {

    private AlgorithmParameterSpec domainParameter;
    private PACESecurityInfos psi;
    private PACEInfo pi;

    /**
     * Create new PACEDomainParameter. Loads parameter as defined in the PACEInfo.
     *
     * @param psi PACESecurityInfos
     */
    public PACEDomainParameter(PACESecurityInfos psi) {
	this.psi = psi;
	this.pi = psi.getPACEInfo();

	loadParameters();
    }

    /**
     * Create new PACEDomainParameter.
     *
     * @param psi PACESecurityInfos
     * @param domainParameter AlgorithmParameterSpec
     */
    public PACEDomainParameter(PACESecurityInfos psi, AlgorithmParameterSpec domainParameter) {
	this.psi = psi;
	this.pi = psi.getPACEInfo();
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
	return pi.isDH();
    }

    /**
     * Checks if the protocol identifier indicates elliptic curve Diffie-Hellman.
     *
     * @return True if elliptic curve Diffie-Hellman is used, otherwise false
     */
    public boolean isECDH() {
	return pi.isECDH();
    }

    private void loadParameters() {
	// If PACEInfo parameterID is present use standardized domain parameters
	if (pi.getParameterID() != -1) {
	    int index = pi.getParameterID();
	    domainParameter = new StandardizedDomainParameters(index).getParameter();
	} // else load proprietary domain parameters from PACEDomainParameterInfo
	else {
	    PACEDomainParameterInfo pdp = psi.getPACEDomainParameterInfo();
	    domainParameter = new ExplicitDomainParameters(pdp.getDomainParameter()).getParameter();
	}

	if (domainParameter == null) {
	    throw new IllegalArgumentException("Cannot load domain parameter");
	}
    }

}
