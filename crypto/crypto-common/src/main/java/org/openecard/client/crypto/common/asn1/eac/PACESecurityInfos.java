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

import java.util.ArrayList;
import java.util.List;


/**
 * Implements the SecurityInfos for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class PACESecurityInfos {

    private List<PACEDomainParameterInfo> pdpiList;
    private List<PACEInfo> piList;
    private int pdpiIndex;
    private int piIndex;

    /**
     * Creates a new set of PACESecurityInfos.
     */
    public PACESecurityInfos() {
	pdpiList = new ArrayList<PACEDomainParameterInfo>();
	piList = new ArrayList<PACEInfo>();
	pdpiIndex = 0;
	piIndex = 0;
    }

    /**
     * Returns the selected PACEDomainParameterInfo.
     *
     * @return PACEDomainParameterInfo
     */
    public PACEDomainParameterInfo getPACEDomainParameterInfo() {
	return pdpiList.get(pdpiIndex);
    }

    /**
     * Returns the PACEDomainParameterInfos.
     *
     * @return PACEDomainParameterInfos
     */
    public List<PACEDomainParameterInfo> getPACEDomainParameterInfos() {
	return pdpiList;
    }

    /**
     * Sets the PACEDomainParameterInfos.
     *
     * @param paceDomainParameterInfos PACEDomainParameterInfos
     */
    public void setPACEDomainParameterInfos(List<PACEDomainParameterInfo> paceDomainParameterInfos) {
	this.pdpiList = paceDomainParameterInfos;
    }

    /**
     * Adds a PACEDomainParameterInfo.
     *
     * @param paceDomainParameterInfo PACEDomainParameterInfo
     */
    public void addPACEDomainParameterInfo(PACEDomainParameterInfo paceDomainParameterInfo) {
	this.pdpiList.add(paceDomainParameterInfo);
    }

    /**
     * Selects a PACEDomainParameterInfo.
     */
    public void selectPACEDomainParameterInfo(int index) {
	if (index < 0 || index > pdpiList.size() - 1) {
	    throw new IllegalArgumentException("Index out of range.");
	}
	this.pdpiIndex = index;
    }

    /**
     * Returns the selected PACEInfo.
     *
     * @return PACEInfo
     */
    public PACEInfo getPACEInfo() {
	return piList.get(piIndex);
    }

    /**
     * Returns the PACEInfos.
     *
     * @return PACEInfos
     */
    public List<PACEInfo> getPACEInfos() {
	return piList;
    }

    /**
     * Sets the PACEInfos.
     *
     * @param paceInfo PACEInfo
     */
    public void setPACEInfos(List<PACEInfo> paceInfos) {
	this.piList = paceInfos;
    }

    /**
     * Adds a PACEInfo.
     *
     * @param paceInfo PACEInfo
     */
    public void addPACEInfo(PACEInfo paceInfo) {
	this.piList.add(paceInfo);
    }

    /**
     * Selects a PACEInfo.
     */
    public void selectPACEInfo(int index) {
	if (index < 0 || index > piList.size() - 1) {
	    throw new IllegalArgumentException("Index out of range.");
	}
	this.piIndex = index;
    }
}
