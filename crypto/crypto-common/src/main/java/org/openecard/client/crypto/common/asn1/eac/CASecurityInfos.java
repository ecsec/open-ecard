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
