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
package org.openecard.client.crypto.common.asn1.eac;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PACESecurityInfos {

    private PACEDomainParameterInfo paceDomainParameterInfo;
    private PACEInfo paceInfo;

    /**
     * Returns the PACEDomainParameterInfo
     *
     * @return PACEDomainParameterInfo
     */
    public PACEDomainParameterInfo getPACEDomainParameterInfo() {
	return paceDomainParameterInfo;
    }

    /**
     * Sets the PACEDomainParameterInfo.
     *
     * @param paceDomainParameterInfo PACEDomainParameterInfo
     */
    public void setPACEDomainParameterInfo(PACEDomainParameterInfo paceDomainParameterInfo) {
	this.paceDomainParameterInfo = paceDomainParameterInfo;
    }

    /**
     * Returns the PACEInfo.
     *
     * @return paceInfo
     */
    public PACEInfo getPACEInfo() {
	return paceInfo;
    }

    /**
     * Sets the PACEInfo.
     *
     * @param paceInfo PACEInfo
     */
    public void setPACEInfo(PACEInfo paceInfo) {
	this.paceInfo = paceInfo;
    }

}
