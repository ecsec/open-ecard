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
package org.openecard.client.crypto.common.asn1.eac;

import java.security.spec.AlgorithmParameterSpec;
import org.openecard.client.crypto.common.asn1.eac.oid.EACObjectIdentifier;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CADomainParameter {

    private AlgorithmParameterSpec domainParameter;
    private CASecurityInfos csi;
    private CAInfo ci;

    /**
     * Create new PACEDomainParameter. Loads parameter as defined in the PACEInfo.
     *
     * @param psi PACESecurityInfos
     */
    public CADomainParameter(CASecurityInfos csi) {
	this.csi = csi;
	this.ci = csi.getCAInfo();

	loadParameters();
    }

    /**
     * Create new PACEDomainParameter.
     *
     * @param psi PACESecurityInfos
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
