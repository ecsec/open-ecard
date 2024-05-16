/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.security.spec.AlgorithmParameterSpec;


/**
 * Wrapper for {@link PACEDomainParameterInfo} with some convenience functions.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public final class PACEDomainParameter {

    private AlgorithmParameterSpec domainParameter;
    private final PACESecurityInfoPair pip;
    private final PACEInfo pi;

    /**
     * Create new PACEDomainParameter. Loads parameter as defined in the PACEInfo.
     *
     * @param pip PACESecurityInfoPair
     */
    public PACEDomainParameter(PACESecurityInfoPair pip) {
	this.pip = pip;
	this.pi = pip.getPACEInfo();

	loadParameters();
    }

    /**
     * Create new PACEDomainParameter.
     *
     * @param pip PACESecurityInfoPair
     * @param domainParameter AlgorithmParameterSpec
     */
    public PACEDomainParameter(PACESecurityInfoPair pip, AlgorithmParameterSpec domainParameter) {
	this.pip = pip;
	this.pi = pip.getPACEInfo();
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
	// see if this is a standardized parameter or not
	if (pip.isStandardizedParameter()) {
	    int index = pi.getParameterID();
	    domainParameter = new StandardizedDomainParameters(index).getParameter();
	} else {
	    // use explicit domain parameters
	    PACEDomainParameterInfo pdp = pip.getPACEDomainParameterInfo();
	    domainParameter = new ExplicitDomainParameters(pdp.getDomainParameter()).getParameter();
	}

	if (domainParameter == null) {
	    throw new IllegalArgumentException("Cannot load domain parameter");
	}
    }

}
