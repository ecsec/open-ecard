/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.state.cif;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import jakarta.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich
 */
public class CifLoader {

    public CardInfoType getNpaCif() {
	try {
	    String cifFile = "/cif-repo/CardInfo_nPA_1-0-0.xml";
	    InputStream in = CifLoader.class.getResourceAsStream(cifFile);
	    WSMarshaller m = WSMarshallerFactory.createInstance();
	    Document doc = m.str2doc(in);
	    JAXBElement<CardInfoType> cif = m.unmarshal(doc, CardInfoType.class);
	    return cif.getValue();
	} catch (IOException | SAXException | WSMarshallerException ex) {
	    throw new RuntimeException("Error reading CIF.", ex);
	}
    }

}
