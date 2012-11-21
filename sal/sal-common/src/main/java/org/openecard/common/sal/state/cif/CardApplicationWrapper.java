/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.common.sal.state.cif;

import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
import iso.std.iso_iec._24727.tech.schema.AccessRuleType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import iso.std.iso_iec._24727.tech.schema.SecurityConditionType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class wraps a single card application of a card info in order to make the access to attributes more efficient
 * and more user friendly.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardApplicationWrapper {

    private Map<Enum<?>, SecurityConditionType> securityConditions = new HashMap<Enum<?>, SecurityConditionType>();
    private HashMap<String, DIDInfoWrapper> didInfos = new HashMap<String, DIDInfoWrapper>();
    private final CardApplicationType cardApplication;
    private DataSetNameListType dataSetNameList = new DataSetNameListType();
    private HashMap<String, DataSetInfoWrapper> dataSetInfos = new HashMap<String, DataSetInfoWrapper>();

    /**
     *
     * @param cardApplication the CardApplication that should be wrapped
     */
    public CardApplicationWrapper(CardApplicationType cardApplication) {
	this.cardApplication = cardApplication;
    }

    /**
     *
     * @param serviceAction the ServiceAction to which the SecurityCondition should be returned
     * @return the SecurityCondition for the specified ServiceAction
     */
    public SecurityConditionType getSecurityCondition(Enum<?> serviceAction) {
	if (securityConditions.isEmpty()) {
	    for (AccessRuleType accessRule : this.cardApplication.getCardApplicationACL().getAccessRule()) {
		if (accessRule.getAction().getConnectionServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getConnectionServiceAction(),
			    accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getAuthorizationServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getAuthorizationServiceAction(),
			    accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getDifferentialIdentityServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getDifferentialIdentityServiceAction(),
			    accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getNamedDataServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getNamedDataServiceAction(),
			    accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getCryptographicServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getCryptographicServiceAction(),
			    accessRule.getSecurityCondition());
		} else if (accessRule.getAction().getCardApplicationServiceAction() != null) {
		    securityConditions.put(accessRule.getAction().getCardApplicationServiceAction(),
			    accessRule.getSecurityCondition());
		}
	    }
	}

	return securityConditions.get(serviceAction);
    }

    /**
     *
     * @return the ApplicationIdentifier of the wrapped card application
     */
    public byte[] getApplicationIdentifier() {
	return cardApplication.getApplicationIdentifier();
    }

    /**
     *
     * @param didName the name of the DID to be returned
     * @return a DIDInfoWrapper wrapping the specified DID or null if no such DID exists in the card appication
     */
    public DIDInfoWrapper getDIDInfo(String didName) {
	if (didInfos.isEmpty()) {
	    for (DIDInfoType didInfo : cardApplication.getDIDInfo()) {
		didInfos.put(didInfo.getDifferentialIdentity().getDIDName(), new DIDInfoWrapper(didInfo));
	    }
	}
	return this.didInfos.get(didName);
    }

    /**
     *
     * @return list of DIDInfos in this card application
     */
    public List<DIDInfoType> getDIDInfoList() {
	return cardApplication.getDIDInfo();
    }

    /**
     *
     * @return the ACL for this card application
     */
    public AccessControlListType getCardApplicationACL() {
	return cardApplication.getCardApplicationACL();
    }

    /**
     *
     * @return list of data set names in this card application
     */
    public DataSetNameListType getDataSetNameList() {
	if (dataSetNameList.getDataSetName().isEmpty()) {
	    for (DataSetInfoType dataSetInfo : cardApplication.getDataSetInfo()) {
		dataSetNameList.getDataSetName().add(dataSetInfo.getDataSetName());
	    }
	}
	return this.dataSetNameList;
    }

    /**
     *
     * @param dataSetName the name of the dataset to be returned
     * @return a DataSetInfoWrapper wrapping the specified dataset or null if no such dataset exists in the card
     * appication
     */
    public DataSetInfoWrapper getDataSetInfo(String dataSetName) {
	if (dataSetInfos.isEmpty()) {
	    for (DataSetInfoType dataSetInfo : cardApplication.getDataSetInfo()) {
		dataSetInfos.put(dataSetInfo.getDataSetName(), new DataSetInfoWrapper(dataSetInfo));
	    }
	}
	return this.dataSetInfos.get(dataSetName);
    }

}
