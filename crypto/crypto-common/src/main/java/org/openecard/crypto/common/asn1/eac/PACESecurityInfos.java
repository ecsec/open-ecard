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

import java.util.ArrayList;
import java.util.List;


/**
 * Implements the SecurityInfos for PACE.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.1.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public final class PACESecurityInfos {

    private List<PACEDomainParameterInfo> pdpiList;
    private List<PACEInfo> piList;
    private List<PACESecurityInfoPair> pipList;

    /**
     * Creates a new set of PACESecurityInfos.
     */
    public PACESecurityInfos() {
	pdpiList = new ArrayList<>();
	piList = new ArrayList<>();
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
     * Gets the PACEInfo pairs that are contained in SecurityInfos object.
     *
     * @return List containing all PACEInfo pairs.
     */
    public List<PACESecurityInfoPair> getPACEInfoPairs() {
	if (pipList == null) {
	    pipList = createPACEInfoPairs();
	}
	return pipList;
    }

    public List<PACESecurityInfoPair> getPACEInfoPairs(List<String> supportedProtocols) {
	List<PACESecurityInfoPair> result = new ArrayList<>();
	for (PACESecurityInfoPair next : getPACEInfoPairs()) {
	    if (supportedProtocols.contains(next.getPACEInfo().getProtocol())) {
		result.add(next);
	    }
	}
	return result;
    }

    private List<PACESecurityInfoPair> createPACEInfoPairs() {
	ArrayList<PACESecurityInfoPair> result = new ArrayList<>();

	// special case when there is only one element
	// in that case the parameter id is optional because a binding of explicit Domain Parameters is implicit
	if (piList.size() == 1) {
	    if (pdpiList.isEmpty()) {
		result.add(new PACESecurityInfoPair(piList.get(0), null));
	    } else {
		result.add(new PACESecurityInfoPair(piList.get(0), pdpiList.get(0)));
	    }
	    return result;
	}

	for (PACEInfo pi : piList) {
	    int id = pi.getParameterID();
	    boolean found = false;
	    if (id != -1) {
		for (PACEDomainParameterInfo dpi : pdpiList) {
		    if (id == dpi.getParameterID()) {
			found = true;
			result.add(new PACESecurityInfoPair(pi, dpi));
			break;
		    }
		}
	    }
	    if (! found) {
		result.add(new PACESecurityInfoPair(pi, null));
	    }
	}
	return result;
    }

}
