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

import java.util.ArrayList;
import java.util.List;


/**
 * Implements the SecurityInfos for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.2.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CASecurityInfos {

    private List<CADomainParameterInfo> cadpiList;
    private List<CAInfo> caiList;
    private int cadpiIndex;
    private int caiIndex;

    /**
     * Creates a new set of CASecurityInfos.
     */
    public CASecurityInfos() {
	cadpiList = new ArrayList<CADomainParameterInfo>();
	caiList = new ArrayList<CAInfo>();
	cadpiIndex = 0;
	caiIndex = 0;
    }

    /**
     * Returns the selected CADomainParameterInfo.
     *
     * @return CADomainParameterInfo
     */
    public CADomainParameterInfo getCADomainParameterInfo() {
	return cadpiList.get(cadpiIndex);
    }

    /**
     * Returns the CADomainParameterInfos.
     *
     * @return CADomainParameterInfos
     */
    public List<CADomainParameterInfo> getCADomainParameterInfos() {
	return cadpiList;
    }

    /**
     * Sets the CADomainParameterInfos.
     *
     * @param caDomainParameterInfos CADomainParameterInfos
     */
    public void setCADomainParameterInfos(List<CADomainParameterInfo> caDomainParameterInfos) {
	this.cadpiList = caDomainParameterInfos;
    }

    /**
     * Adds a CADomainParameterInfo.
     *
     * @param caDomainParameterInfo CADomainParameterInfo
     */
    public void addCADomainParameterInfo(CADomainParameterInfo caDomainParameterInfo) {
	this.cadpiList.add(caDomainParameterInfo);
    }

    /**
     * Selects a CADomainParameterInfo.
     *
     * @param index Index
     */
    public void selectCADomainParameterInfo(int index) {
	if (index < 0 || index > cadpiList.size() - 1) {
	    throw new IllegalArgumentException("Index out of range.");
	}
	this.cadpiIndex = index;
    }

    /**
     * Returns the selected CAInfo.
     *
     * @return CAInfos
     */
    public CAInfo getCAInfo() {
	return caiList.get(caiIndex);
    }

    /**
     * Returns the CAInfos.
     *
     * @return CAInfos
     */
    public List<CAInfo> getCAInfos() {
	return caiList;
    }

    /**
     * Sets the CAInfos.
     *
     * @param caInfos CAInfos
     */
    public void setCAInfos(List<CAInfo> caInfos) {
	this.caiList = caInfos;
    }

    /**
     * Adds a CAInfo.
     *
     * @param caInfo CAInfo
     */
    public void addCAInfo(CAInfo caInfo) {
	this.caiList.add(caInfo);
    }

    /**
     * Selects a CAInfo.
     *
     * @param index Index
     */
    public void selectCAInfo(int index) {
	if (index < 0 || index > caiList.size() - 1) {
	    throw new IllegalArgumentException("Index out of range.");
	}
	this.caiIndex = index;
    }

    /**
     * Checks if the object identifier is a CA object identifier.
     *
     * @param oid Object Identifier
     * @return true if the object identifier is a CA object identifier, otherwise false
     */
    public static boolean isObjectIdentifier(String oid) {
	if (CAInfo.isObjectIdentifier(oid)) {
	    return true;
	} else if (CADomainParameterInfo.isObjectIdentifier(oid)) {
	    return true;
	}
	return false;
    }

}
