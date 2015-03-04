/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.ws.GetCardInfoOrACD;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Classpath resource based CardInfo repository.
 *
 * @author Tobias Wich
 */
public class LocalCifRepo implements GetCardInfoOrACD {

    private static final Logger logger = LoggerFactory.getLogger(LocalCifRepo.class);

    private final WSMarshaller m;
    private final HashMap<String, Document> cifs = new HashMap<>();

    public LocalCifRepo(final WSMarshaller m) throws IOException, WSMarshallerException, SAXException {
	this.m = m;
	// load properties
	InputStream propStream = getStream("repo-config.properties");
	Properties conf = new Properties();
	conf.load(propStream);

	String fileNames = conf.getProperty("cifFiles");
	String[] files = fileNames.split(",");

	for (final String next : files) {
	    // load and unmarshal
	    InputStream cifStream = getStream(next.trim());
	    Document cifDoc = m.str2doc(cifStream);
	    CardInfoType cif = (CardInfoType) m.unmarshal(cifDoc);
	    String cardType = cif.getCardType().getObjectIdentifier();
	    // add file
	    cifs.put(cardType, cifDoc);
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
	List<String> cardTypes = parameters.getCardTypeIdentifier();
	ArrayList<CardInfoType> cifsResult = new ArrayList<>(cardTypes.size());
	Result result = WSHelper.makeResultOK();

	try {
	    if (ECardConstants.CIF.GET_SPECIFIED.equals(parameters.getAction())) {
		ArrayList<String> missingTypes = new ArrayList<>();
		for (String cardType : cardTypes) {
		    Document cif = cifs.get(cardType);
		    if (cif == null) {
			missingTypes.add(cardType);
		    } else {
			// marshal here to receive a copy of the CIF
			cifsResult.add((CardInfoType) m.unmarshal(cif));
		    }
		}

		if (! missingTypes.isEmpty()) {
		    StringBuilder error = new StringBuilder("The following card types could not be found:");
		    for (String type : missingTypes) {
			error.append("\n  ").append(type);
		    }
		    result = WSHelper.makeResultError(ECardConstants.Minor.SAL.UNKNOWN_CARDTYPE, error.toString());
		}
	    } else if (ECardConstants.CIF.GET_OTHER.equals(parameters.getAction())) {
		HashMap<String, Document> cifsTmp = new HashMap<>();
		cifsTmp.putAll(cifs);
		for (String cardType : cardTypes) {
		    cifsTmp.remove(cardType);
		}
		for (Map.Entry<String, Document> e : cifsTmp.entrySet()) {
		    Document next = e.getValue();
		    cifsResult.add((CardInfoType) m.unmarshal(next));
		}
	    } else {
		result = WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, "Given action is unsupported.");
	    }

	    GetCardInfoOrACDResponse res = WSHelper.makeResponse(GetCardInfoOrACDResponse.class, result);
	    res.getCardInfoOrCapabilityInfo().addAll(cifsResult);
	    return res;
	} catch (WSMarshallerException ex) {
	    String msg = "Failed to unmarshal a CIF document.";
	    logger.error(msg, ex);
	    result = WSHelper.makeResultError(ECardConstants.Minor.App.INT_ERROR, msg);
	    GetCardInfoOrACDResponse res = WSHelper.makeResponse(GetCardInfoOrACDResponse.class, result);
	    return res;
	}
    }

}
