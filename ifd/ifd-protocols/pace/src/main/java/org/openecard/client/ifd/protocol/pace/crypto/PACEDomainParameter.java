/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace.crypto;

import java.security.spec.AlgorithmParameterSpec;
import org.openecard.client.crypto.common.asn1.eac.ExplicitDomainParameters;
import org.openecard.client.crypto.common.asn1.eac.PACEDomainParameterInfo;
import org.openecard.client.crypto.common.asn1.eac.PACEInfo;
import org.openecard.client.crypto.common.asn1.eac.PACESecurityInfos;
import org.openecard.client.crypto.common.asn1.eac.StandardizedDomainParameters;


/**
 *
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
