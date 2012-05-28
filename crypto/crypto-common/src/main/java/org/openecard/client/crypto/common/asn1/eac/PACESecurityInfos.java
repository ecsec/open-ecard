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
     * @param index Index
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
     * @param paceInfos PACEInfos
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
     *
     * @param index Index
     */
    public void selectPACEInfo(int index) {
	if (index < 0 || index > piList.size() - 1) {
	    throw new IllegalArgumentException("Index out of range.");
	}
	this.piIndex = index;
    }

}
