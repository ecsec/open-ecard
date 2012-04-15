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

import org.openecard.client.crypto.common.asn1.eac.oid.TAObjectIdentifier;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class TASecurityInfos {

    private TAInfo taInfo;

    /**
     * Returns the TerminalAuthenticationInfo.
     *
     * @return TAInfo
     */
    public TAInfo getTAInfo() {
	return taInfo;
    }

    /**
     * Sets the TAInfo
     *
     * @param taInfo
     */
    public void setTAInfo(TAInfo taInfo) {
	this.taInfo = taInfo;
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
