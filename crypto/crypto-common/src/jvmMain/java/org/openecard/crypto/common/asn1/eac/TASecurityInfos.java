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

package org.openecard.crypto.common.asn1.eac;

import java.util.ArrayList;
import java.util.List;
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier;


/**
 * Implements the SecurityInfos for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.3.
 *
 * @author Moritz Horsch
 */
public final class TASecurityInfos {

    private List<TAInfo> taiList;
    private int taiIndex;

    /**
     * Creates a new set of TASecurityInfos.
     */
    public TASecurityInfos() {
	taiList = new ArrayList<TAInfo>();
	taiIndex = 0;
    }

    /**
     * Returns the selected TAInfo.
     *
     * @return TAInfo
     */
    public TAInfo getTAInfo() {
	return taiList.get(taiIndex);
    }

    /**
     * Returns the TAInfos.
     *
     * @return TAInfos
     */
    public List<TAInfo> getTAInfos() {
	return taiList;
    }

    /**
     * Sets the TAInfos
     *
     * @param taInfos TAInfos
     */
    public void setTAInfos(List<TAInfo> taInfos) {
	this.taiList = taInfos;
    }

    /**
     * Adds a TAInfo.
     *
     * @param taInfo TAInfo
     */
    public void addTAInfo(TAInfo taInfo) {
	this.taiList.add(taInfo);
    }

    /**
     * Selects a TAInfo.
     *
     * @param index Index
     */
    public void selectTAInfo(int index) {
	if (index < 0 || index > taiList.size() - 1) {
	    throw new IllegalArgumentException("Index out of range.");
	}
	this.taiIndex = index;
    }

    /**
     * Compares the object identifier.
     *
     * @param oid Object identifier
     * @return true if o is a TA object identifier, else false.
     */
    public static boolean isObjectIdentifier(String oid) {
	if (oid.equals(TAObjectIdentifier.id_TA)) {
	    return true;
	}
	return false;
    }

}
