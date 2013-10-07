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


import iso.std.iso_iec._24727.tech.schema.ApplicationCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecard.common.util.ByteArrayWrapper;


/**
 * This class wraps a single card info in order to make the access to attributes more efficient
 * and more user friendly.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoWrapper {

    private final CardInfoType cif;
    private Map<ByteArrayWrapper, CardApplicationWrapper> cardApplications =
	    new HashMap<ByteArrayWrapper, CardApplicationWrapper>();
    List<byte[]> cardApplicationNames = new ArrayList<byte[]>();

    /**
     *
     * @param cif the CardInfo that should be wrapped
     */
    public CardInfoWrapper(CardInfoType cif) {
	this.cif = cif;
    }

    /**
     *
     * @return the object identifier of this card
     */
    public String getCardType() {
	return cif.getCardType().getObjectIdentifier();
    }

    /**
     *
     * @return the implicitly selected application of this card
     */
    public byte[] getImplicitlySelectedApplication() {
	return cif.getApplicationCapabilities().getImplicitlySelectedApplication();
    }

    /**
     *
     * @return the application capabilities of this card
     */
    public ApplicationCapabilitiesType getApplicationCapabilities() {
	return cif.getApplicationCapabilities();
    }

    /**
     *
     * @return a map that maps ByteArrayWrapper keys (the application identifier) to CardApplicationWrapper (the card
     *   applications)
     */
    public Map<ByteArrayWrapper, CardApplicationWrapper> getCardApplications() {
	if (cardApplications.isEmpty()) {
	    for (CardApplicationType cardApplication : cif.getApplicationCapabilities().getCardApplication()) {
		cardApplications.put(new ByteArrayWrapper(cardApplication.getApplicationIdentifier()),
			new CardApplicationWrapper(cardApplication));
	    }
	}
	return cardApplications;
    }

    /**
     *
     * @param didName name of the did to get the DIDInfo for
     * @param applicationIdentifier identifier of the application the DID belongs to
     * @return the DIDInfo of the specified DID or null, if either the card application or the DID do not exist
     */
    public DIDInfoType getDIDInfo(String didName, byte[] applicationIdentifier) {
	CardApplicationWrapper application = this.getCardApplications().get(new ByteArrayWrapper(applicationIdentifier));
	if (application == null) {
	    return null;
	}
	DIDInfoWrapper didInfo = application.getDIDInfo(didName);
	if (didInfo == null) {
	    return null;
	} else {
	    return didInfo.getDIDInfo();
	}
    }

    /**
     *
     * @param cardApplication identifier of the application to get the list of data set names from
     * @return list of data set names contained in this application or null, if no such card application exists
     */
    public DataSetNameListType getDataSetNameList(byte[] cardApplication) {
	CardApplicationWrapper wrapper = cardApplications.get(new ByteArrayWrapper(cardApplication));
	if (wrapper == null) {
	    return null;
	} else {
	    return wrapper.getDataSetNameList();
	}
    }

    /**
     *
     * @param dataSetName
     *            name of the data set to get the datasetinfo for
     * @param applicationIdentifier
     *            identifier of the application containing the data set
     * @return the DataSetInfo for the specified data set or null, if no such data set exists in the specified
     *         application
     */
    public DataSetInfoType getDataSet(String dataSetName, byte[] applicationIdentifier) {
	CardApplicationWrapper application = cardApplications.get(new ByteArrayWrapper(applicationIdentifier));
	if (application == null) {
	    return null;
	}
	DataSetInfoWrapper dataSet = application.getDataSetInfo(dataSetName);
	if (dataSet == null) {
	    return null;
	} else {
	    return dataSet.getDataSetInfo();
	}
    }

    /**
     *
     * @param applicationIdentifier identifier of the application to return
     * @return CardApplication for the specified applicationIdentifier or null, if no application with this identifier
     *         exists.
     */
    public CardApplicationWrapper getCardApplication(byte[] applicationIdentifier) {
	return this.getCardApplications().get(new ByteArrayWrapper(applicationIdentifier));
    }

    /**
     *
     * @param didName Name of the DID to get the structure for
     * @param cardApplication Identifier of the card application
     * @return DIDStructure for the specified didName and card application or null, if no such did exists.
     */
    public DIDStructureType getDIDStructure(String didName, byte[] cardApplication) {
	DIDInfoType didInfo = this.getDIDInfo(didName, cardApplication);
	if (didInfo == null) {
	    return null;
	}
	DIDStructureType didStructure = new DIDStructureType();
	didStructure.setDIDName(didInfo.getDifferentialIdentity().getDIDName());
	didStructure.setDIDScope(didInfo.getDifferentialIdentity().getDIDScope());
	if (didStructure.getDIDScope() == null) {
	    // no scope is equal to local
	    didStructure.setDIDScope(DIDScopeType.LOCAL);
	}
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
	return didStructure;
    }

    /**
     *
     * @return list of application identifiers in this cardinfo
     */
    public List<String> getCardApplicationNameList() {
	if (!cardApplicationNames.isEmpty()) {
	    cardApplicationNames.clear();
	} 
	    
        for (CardApplicationType cardApplication : cif.getApplicationCapabilities().getCardApplication()) {
		cardApplicationNames.add(cardApplication.getApplicationName());
        }
	
	return cardApplicationNames;
    }
}
