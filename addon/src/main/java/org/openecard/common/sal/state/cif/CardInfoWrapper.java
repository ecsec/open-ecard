/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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
import iso.std.iso_iec._24727.tech.schema.DSIType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.openecard.common.util.ByteArrayWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class wraps a single card info in order to make the access to attributes more efficient
 * and more user friendly.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class CardInfoWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(CardInfoWrapper.class);

    private final CardInfoType cif;
    private final Map<ByteArrayWrapper, CardApplicationWrapper> cardApplications = new HashMap<>();
    private final List<byte[]> cardApplicationNames = new ArrayList<>();
    private final String interfaceProtocol;

    /**
     *
     * @param cif the CardInfo that should be wrapped
     * @param interfaceProtocol Protocol with which the card is connected.
     */
    public CardInfoWrapper(CardInfoType cif, @Nullable String interfaceProtocol) {
	this.cif = cif;
	this.interfaceProtocol = interfaceProtocol;

	filterForProtocol();
    }

    public CardInfoWrapper(CardInfoWrapper other) {
	this(other.cif, other.interfaceProtocol);
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
	    for (CardApplicationType cardApplication : getApplicationCapabilities().getCardApplication()) {
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
     * @param didName name of the did to get the DIDInfo for
     * @param didScope Scope of the DID
     * @return the DIDInfo of the specified DID or null, if either the card application or the DID do not exist
     */
    public DIDInfoType getDIDInfo(String didName, DIDScopeType didScope) {
	List<CardApplicationType> cardApps = getApplicationCapabilities().getCardApplication();
	for (CardApplicationType cardApp : cardApps) {
	    for (DIDInfoType did : cardApp.getDIDInfo()) {
		if (did.getDifferentialIdentity().getDIDName().equals(didName)) {
		    if (did.getDifferentialIdentity().getDIDScope() != null) {
			if (didScope != null) {
			    if (didScope.value().equals(did.getDifferentialIdentity().getDIDScope().value())) {
				return did;
			    }
			}
		    } else {
			return did;
		    }
		}
	    }
	}

	return null;
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
     * @param didName Name of the DID to get the structure for
     * @param  didScope Scope of the DID
     * @return DIDStructure for the specified didName and card application or null, if no such did exists.
     */
    public DIDStructureType getDIDStructure(String didName, DIDScopeType didScope) {
	DIDInfoType didInfo = this.getDIDInfo(didName, didScope);
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
    public List<byte[]> getCardApplicationNameList() {
	if (cardApplicationNames.isEmpty()) {
	    for (CardApplicationType cardApplication : getApplicationCapabilities().getCardApplication()) {
		cardApplicationNames.add(cardApplication.getApplicationIdentifier());
	    }
	}
	return cardApplicationNames;
    }

    /**
     * The method searches a specific DSI by name.
     *
     * @param dsiName The name of the DSI to look for.
     * @return A DSIType object which contains the given DSI name or null if no DSI with such a name was found.
     */
    public DSIType getDSIbyName(String dsiName) {
	for (CardApplicationWrapper cardAppWrapper : cardApplications.values()) {
	    for (DataSetInfoType dSetInfoWrapper : cardAppWrapper.getDataSetInfoList()) {
		if (dSetInfoWrapper.getDSI() != null) {
		    for (DSIType dsi : dSetInfoWrapper.getDSI()) {
			if (dsi.getDSIName().equals(dsiName)) {
			    return dsi;
			}
		    }
		}
	    }
	}

	return null;
    }

    /**
     * The method searches a specific data set by the DSI name.
     *
     * @param fileIdentifier The DSIName which shall be found in a data set.
     * @return A DataSetInfoType object containing which contains the DSI which is referenced by the given dsiName. The
     * method returns NULL if no data set was found.
     */
    public DataSetInfoType getDataSetByFid(byte[] fileIdentifier) {
	for (CardApplicationWrapper cardAppWrapper : cardApplications.values()) {
	    for (DataSetInfoType dSetInfoWrapper : cardAppWrapper.getDataSetInfoList()) {
		byte[] dataSetPath = dSetInfoWrapper.getDataSetPath().getEfIdOrPath();
		int pathLength = dataSetPath.length;
		if (dataSetPath[pathLength - 2] == fileIdentifier[0] && dataSetPath[pathLength - 1] == fileIdentifier[1]) {
		    return dSetInfoWrapper;
		}
	    }
	}

	return null;
    }

    /**
     * The method searches an application ID by a DIDName and a DIDScope.
     *
     * @param didName Name of the DID to search.
     * @param didScope Scope of the DID to look for.
     * @return The application ID of  the application which contains the DID with DIDName and DIDScope or NULL if no
     * such application was found. NOTE: If the parameter didScope is NULL then the application ID of the application
     * with the first occurrence of didName is returned.
     */
    public byte[] getApplicationIdByDidName(String didName, DIDScopeType didScope) {
	List<CardApplicationType> cardApps = getApplicationCapabilities().getCardApplication();
	for (CardApplicationType cardApp : cardApps) {
	    for (DIDInfoType did : cardApp.getDIDInfo()) {
		if (did.getDifferentialIdentity().getDIDName().equals(didName)) {
		    if (did.getDifferentialIdentity().getDIDScope() != null) {
			if (didScope != null) {
			    if (didScope.value().equals(did.getDifferentialIdentity().getDIDScope().value())) {
				return cardApp.getApplicationIdentifier();
			    }
			} else {
			    return cardApp.getApplicationIdentifier();
			}
		    } else {
			return cardApp.getApplicationIdentifier();
		    }
		}
	    }
	}

	return null;
    }

    public DataSetInfoType getDataSetByDsiName(String dsiName) {
	for (CardApplicationWrapper cardAppWrapper : cardApplications.values()) {
	    for (DataSetInfoType dSetInfoWrapper : cardAppWrapper.getDataSetInfoList()) {
		if (dSetInfoWrapper.getDSI() != null) {
		    for (DSIType dsi : dSetInfoWrapper.getDSI()) {
			if (dsi.getDSIName().equals(dsiName)) {
			    return dSetInfoWrapper;
			}
		    }
		}
	    }
	}

	return null;
    }

    public DataSetInfoType getDataSetByName(String dataSetName) {
	for (CardApplicationWrapper cardAppWrapper : cardApplications.values()) {
	    for (DataSetInfoType dSetInfoWrapper : cardAppWrapper.getDataSetInfoList()) {
		if (dSetInfoWrapper.getDataSetName().equals(dataSetName)) {
		    return dSetInfoWrapper;
		}
	    }
	}

	return null;
    }

    private void filterForProtocol() {
	List<CardApplicationType> apps = getApplicationCapabilities().getCardApplication();
	Iterator<CardApplicationType> it = apps.iterator();
	while (it.hasNext()) {
	    CardApplicationType app = it.next();
	    List<String> interfaceProtos = app.getInterfaceProtocol();
	    // remove when there is a protocol list not containing the current protocol
	    if (! interfaceProtos.isEmpty()) {
		if (interfaceProtocol == null) {
		    String msg = "Interface protocol is not available.";
		    LOG.error(msg);
		    throw new IllegalStateException(msg);
		}

		if (! interfaceProtos.contains(interfaceProtocol)) {
		    it.remove();
		}
	    }
	}
    }

}
