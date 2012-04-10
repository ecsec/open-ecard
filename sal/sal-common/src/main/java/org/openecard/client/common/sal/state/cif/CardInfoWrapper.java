/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard Client.
 *
 * GNU General Public License Usage
 *
 * Open eCard Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open eCard Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Other Usage
 *
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ****************************************************************************/

package org.openecard.client.common.sal.state.cif;

import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationStateType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoWrapper {

    private final CardInfoType cif;
    private byte[] currentCardApplication;
    private Set<DIDInfoType> authenticatedDIDs = new HashSet<DIDInfoType>();

    public CardInfoWrapper(CardInfoType cif) {
	this.cif = cif;
	this.currentCardApplication = cif.getApplicationCapabilities().getImplicitlySelectedApplication();
    }

    /**
     *
     * @return List with all available CardApplications
     */
    public List<CardApplicationType> getAllCardApplications() {
	return cif.getApplicationCapabilities().getCardApplication();
    }

    /**
     *
     * @param applicationIdentifier
     * @return CardApplication for the specified applicationIdentifier or null,
     *         if no application with this identifier exists.
     */
    public CardApplicationType getCardApplication(byte[] applicationIdentifier) {
	for (CardApplicationType cardApplication : getAllCardApplications()) {
	    if (ByteUtils.compare(cardApplication.getApplicationIdentifier(), applicationIdentifier)) {
		return cardApplication;
	    }
	}
	return null;
    }

    /**
     *
     * @param didName Name of the DID
     * @param didScope Scope of the DID
     * @return DIDStructure for the specified didName and didScope or null,
     *         if no did with this name and scope exists.
     */
    public DIDStructureType getDIDStructure(String didName, DIDScopeType didScope) {
	DIDInfoType didInfo = this.getDIDInfo(didName, didScope);
	if (didInfo == null) {
	    return null;
	}

	DIDStructureType didStructure = new DIDStructureType();
	didStructure.setDIDName(didInfo.getDifferentialIdentity().getDIDName());
	didStructure.setDIDScope(didInfo.getDifferentialIdentity().getDIDScope());
	DIDMarkerType didMarker = didInfo.getDifferentialIdentity().getDIDMarker();

	if (didMarker.getCAMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getCAMarker());
	} else if (didMarker.getCryptoMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getCryptoMarker());
	} else if (didMarker.getEACMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getEACMarker());
	} else if (didMarker.getMutualAuthMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getMutualAuthMarker());
	} else if (didMarker.getPACEMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getPACEMarker());
	} else if (didMarker.getPinCompareMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getPinCompareMarker());
	} else if (didMarker.getRIMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getRIMarker());
	} else if (didMarker.getRSAAuthMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getRSAAuthMarker());
	} else if (didMarker.getTAMarker() != null) {
	    didStructure.setDIDMarker(didMarker.getTAMarker());
	}

	didStructure.setDIDQualifier(didInfo.getDifferentialIdentity().getDIDQualifier());
	didStructure.setAuthenticated(this.isAuthenticated(didName, didScope));
	return didStructure;
    }

    public void addAuthenticated(String didName, DIDScopeType didScope) {
	this.authenticatedDIDs.add(getDIDInfo(didName, didScope));
    }

    public void removeAuthenticated(String didName, DIDScopeType didScope) {
	this.authenticatedDIDs.remove(getDIDInfo(didName, didScope));
    }

    public boolean isAuthenticated(String didName, DIDScopeType didScope) {
	if (authenticatedDIDs.contains(getDIDInfo(didName, didScope))) {
	    return true;
	} else {
	    return false;
	}
    }

    public DataSetNameListType getDataSetNameList() {
	DataSetNameListType dataSetNameList = new DataSetNameListType();
	for (DataSetInfoType dataSetInfo : getCardApplication(currentCardApplication).getDataSetInfo()) {
	    dataSetNameList.getDataSetName().add(dataSetInfo.getDataSetName());
	}
	return dataSetNameList;
    }

    public DataSetInfoType getDataSet(String dataSetName) {
	for (DataSetInfoType dataSetInfo : getCardApplication(currentCardApplication).getDataSetInfo()) {
	    if (dataSetInfo.getDataSetName().equals(dataSetName)) {
		return dataSetInfo;
	    }
	}
	return null;
    }

    public CardApplicationType getCurrentCardApplication() {
	return getCardApplication(currentCardApplication);
    }

    public void setCurrentCardApplication(byte[] currentCardApplication) {
	this.currentCardApplication = currentCardApplication;
    }

    public CardApplicationType getImplicitlySelectedApplication() {
	return getCardApplication(cif.getApplicationCapabilities().getImplicitlySelectedApplication());
    }


    public DIDInfoType getDIDInfo(String didName, DIDScopeType didScope) {
	List<DIDInfoType> didInfos = null;
	if (didScope==null || didScope.equals(DIDScopeType.GLOBAL)) {
	    didInfos = getImplicitlySelectedApplication().getDIDInfo();
	} else {
	    didInfos = getCardApplication(currentCardApplication).getDIDInfo();
	}
	for (DIDInfoType didInfo : didInfos) {
	    if (didInfo.getDifferentialIdentity().getDIDName().equals(didName)) {
		return didInfo;
	    }
	}
	return null;
    }

    private boolean checkSecurityCondition(SecurityConditionType securityCondition) {
	try {
	    if (securityCondition.isAlways()) {
		return true;
	    }
	} catch (NullPointerException e) {
	    /* ignore */
	}
	if (securityCondition.getDIDAuthentication() != null) {
	    DIDAuthenticationStateType didAuthenticationState = securityCondition.getDIDAuthentication();
	    // TODO: check what to do with didstate
	    didAuthenticationState.isDIDState();
	    return isAuthenticated(didAuthenticationState.getDIDName(), didAuthenticationState.getDIDScope());
	} else if (securityCondition.getOr() != null) {
	    for (SecurityConditionType securityConditionOR : securityCondition.getOr().getSecurityCondition()) {
		if (checkSecurityCondition(securityConditionOR)) {
		    return true;
		}
	    }
	    return false;
	} else if (securityCondition.getAnd() != null) {
	    for (SecurityConditionType securityConditionAND : securityCondition.getAnd().getSecurityCondition()) {
		if (!checkSecurityCondition(securityConditionAND)) {
		    return false;
		}
	    }
	    return true;
	} else if (securityCondition.getNot() != null) {
	    return ! checkSecurityCondition(securityCondition.getNot());
	}
    }

    public boolean checkSecurityCondition(String didName, DIDScopeType didScope, Enum<?> serviceAction) {
	return checkAccessRules(this.getDIDInfo(didName, didScope).getDIDACL().getAccessRule(), serviceAction);
    }

    public boolean checkSecurityCondition(CardApplicationType cardApplication, Enum<?> serviceAction) {
	return checkAccessRules(cardApplication.getCardApplicationACL().getAccessRule(), serviceAction);
    }

    public boolean checkSecurityCondition(DataSetInfoType dataSetInfo, Enum<?> serviceAction) {
	return checkAccessRules(dataSetInfo.getDataSetACL().getAccessRule(), serviceAction);
    }

    private boolean checkAccessRules(List<AccessRuleType> accessRules, Enum<?> serviceAction) {
	SecurityConditionType securityCondition = null;
	for (AccessRuleType accessRule : accessRules) {
	    if (accessRule.getAction().getConnectionServiceAction() != null
		&& accessRule.getAction().getConnectionServiceAction().equals(serviceAction)) {
		securityCondition = accessRule.getSecurityCondition();
	    } else if (accessRule.getAction().getAuthorizationServiceAction() != null
		       && accessRule.getAction().getAuthorizationServiceAction().equals(serviceAction)) {
		securityCondition = accessRule.getSecurityCondition();
	    } else if (accessRule.getAction().getDifferentialIdentityServiceAction() != null
		       && accessRule.getAction().getDifferentialIdentityServiceAction().equals(serviceAction)) {
		securityCondition = accessRule.getSecurityCondition();
	    } else if (accessRule.getAction().getNamedDataServiceAction() != null
		       && accessRule.getAction().getNamedDataServiceAction().equals(serviceAction)) {
		securityCondition = accessRule.getSecurityCondition();
	    } else if (accessRule.getAction().getCryptographicServiceAction() != null
		       && accessRule.getAction().getCryptographicServiceAction().equals(serviceAction)) {
		securityCondition = accessRule.getSecurityCondition();
	    } else if (accessRule.getAction().getCardApplicationServiceAction() != null
		       && accessRule.getAction().getCardApplicationServiceAction().equals(serviceAction)) {
		securityCondition = accessRule.getSecurityCondition();
	    }
	}
	if (securityCondition != null) {
	    return checkSecurityCondition(securityCondition);
	} else {
	    return false;
	}
    }

}
