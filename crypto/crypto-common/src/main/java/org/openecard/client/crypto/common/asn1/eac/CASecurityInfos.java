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
public class CASecurityInfos {

    private CADomainParameterInfo caDomainParameter;
    private CAInfo caInfo;

    /**
     * Returns the CADomainParameterInfo.
     *
     * @return CADomainParameterInfo
     */
    public CADomainParameterInfo getCADomainParameterInfo() {
	return caDomainParameter;
    }

    /**
     * Returns the CAInfo.
     *
     * @return CAInfo
     */
    public CAInfo getCAInfo() {
	return caInfo;
    }

    /**
     * Sets the CADomainParameterInfo.
     *
     * @param caDomainParameter
     */
    public void setCADomainParameterInfo(CADomainParameterInfo caDomainParameter) {
	this.caDomainParameter = caDomainParameter;
    }

    /**
     * Sets the CAInfo.
     *
     * @param caInfo CAInfo
     */
    public void setCAInfo(CAInfo caInfo) {
	this.caInfo = caInfo;
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
