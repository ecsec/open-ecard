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
import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;


/**
 * Implements the SecurityInfos for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.3.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
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
