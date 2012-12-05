/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.recognition.staticrepo;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACDResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.ws.GetCardInfoOrACD;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class LocalCifRepo implements GetCardInfoOrACD {

    private final HashMap<String,CardInfoType> cifs = new HashMap<String, CardInfoType>();

    public LocalCifRepo(WSMarshaller m) throws IOException, WSMarshallerException, SAXException {
	// load properties
	InputStream propStream = getStream("repo-config.properties");
	Properties conf = new Properties();
	conf.load(propStream);

	String fileNames = conf.getProperty("cifFiles");
	String[] files = fileNames.split(",");

	for (String next : files) {
	    // load and unmarshal
	    InputStream cifStream = getStream(next);
	    Document cifDoc = m.str2doc(cifStream);
	    CardInfoType cif = (CardInfoType) m.unmarshal(cifDoc);
	    String cardType = cif.getCardType().getObjectIdentifier();
	    // add file
	    cifs.put(cardType, cif);
	}
    }

    private static InputStream getStream(String res) throws FileNotFoundException {
	String prefix = "cif-repo/";
	InputStream s = LocalCifRepo.class.getResourceAsStream(prefix + res);
	if (s == null) {
	    s = LocalCifRepo.class.getResourceAsStream("/" + prefix + res);
	}
	if (s == null) {
	    throw new FileNotFoundException("Unable to load file " + res + ".");
	}
	return s;
    }

    @Override
    public GetCardInfoOrACDResponse getCardInfoOrACD(iso.std.iso_iec._24727.tech.schema.GetCardInfoOrACD parameters) {
	String type = parameters.getCardTypeIdentifier().get(0);
	CardInfoType cif = cifs.get(type);

	Result result;
	if (cif != null) {
	    result = WSHelper.makeResultOK();
	} else {
	    result = WSHelper.makeResultError(ECardConstants.Minor.SAL.UNKNOWN_CARDTYPE, "Requested type " + type + " is unknown.");
	}
	GetCardInfoOrACDResponse res = WSHelper.makeResponse(GetCardInfoOrACDResponse.class, result);
	if (cif != null) {
	    res.getCardInfoOrCapabilityInfo().add(cif);
	}
	return res;
    }

}
