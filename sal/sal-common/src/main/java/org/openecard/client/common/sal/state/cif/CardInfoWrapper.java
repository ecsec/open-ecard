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

import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecard.client.common.util.ByteArrayWrapper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoWrapper {

    private final CardInfoType cif;
    private Map<ByteArrayWrapper, CardApplicationWrapper> cardApplications = new HashMap<ByteArrayWrapper, CardApplicationWrapper>();
    List<byte[]> cardApplicationNames = new ArrayList<byte[]>();

    public CardInfoWrapper(CardInfoType cif) {
	this.cif = cif;
    }

    public byte[] getImplicitlySelectedApplication(){
	return cif.getApplicationCapabilities().getImplicitlySelectedApplication();
    }

    public Map<ByteArrayWrapper, CardApplicationWrapper> getCardApplications() {
        if(cardApplications.isEmpty()){
            for(CardApplicationType cardApplication : cif.getApplicationCapabilities().getCardApplication()){
               cardApplications.put(new ByteArrayWrapper(cardApplication.getApplicationIdentifier()), new CardApplicationWrapper(cardApplication)); 
            }
        } 
        return cardApplications;
    }
    
    public DIDInfoType getDIDInfo(String didName, byte[] applicationIdentifier) {
        CardApplicationWrapper application = cardApplications.get(new ByteArrayWrapper(applicationIdentifier));
        if (application == null)
            return null;
        DIDInfoWrapper didInfo = application.getDIDInfo(didName);
        if (didInfo == null)
            return null;
        else
            return didInfo.getDIDInfo();
    }
    
    public DataSetNameListType getDataSetNameList(byte[] cardApplication) {
        return cardApplications.get(new ByteArrayWrapper(cardApplication)).getDataSetNameList();
    }
    
    public DataSetInfoType getDataSet(String dataSetName, byte[] applicationIdentifier) {
        CardApplicationWrapper application = cardApplications.get(new ByteArrayWrapper(applicationIdentifier));
        if (application == null)
            return null;
        DataSetInfoWrapper dataSet = application.getDataSetInfo(dataSetName);
        if (dataSet == null)
            return null;
        else
            return dataSet.getDataSetInfo();
    }
    
    /**
     *
     * @param applicationIdentifier
     * @return CardApplication for the specified applicationIdentifier or null,
     *         if no application with this identifier exists.
     */
    public CardApplicationWrapper getCardApplication(byte[] applicationIdentifier) {
	return this.getCardApplications().get(new ByteArrayWrapper(applicationIdentifier));
    }
    
    /**
     *
     * @param didName Name of the DID
     * @param cardApplication Identifier of the cardapplication
     * @return DIDStructure for the specified didName and cardapplication or null,
     *         if no such did exists.
     */
    public DIDStructureType getDIDStructure(String didName, byte[] cardApplication) {
	DIDInfoType didInfo = this.getDIDInfo(didName, cardApplication);
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
	return didStructure;
    }

    public List<byte[]> getCardApplicationNameList() {
	if(cardApplicationNames.isEmpty()){
	    for(CardApplicationType cardApplication : cif.getApplicationCapabilities().getCardApplication()){
		cardApplicationNames.add(cardApplication.getApplicationIdentifier());
	    }
	}
	return cardApplicationNames;
    }

}
