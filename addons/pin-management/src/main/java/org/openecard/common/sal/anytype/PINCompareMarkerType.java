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

package org.openecard.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import iso.std.iso_iec._24727.tech.schema.StateInfoType;
import java.math.BigInteger;
import java.util.Arrays;
import org.openecard.common.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Dirk Petrautzki
 */
public class PINCompareMarkerType {

    private KeyRefType pinRef = null;
    private String pinValue = null;
    private PasswordAttributesType passwordAttributes = null;
    private String protocol;

    public PINCompareMarkerType(iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType didAbstractMarkerType) {
	if(!(didAbstractMarkerType instanceof iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType)){
	    throw new IllegalArgumentException();
	}

	protocol = didAbstractMarkerType.getProtocol();

	for (Element e : didAbstractMarkerType.getAny()) {
	    if (e.getLocalName().equals("PinRef")) {
		pinRef = new KeyRefType();
		NodeList nodeList = e.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);

		    if (n.getLocalName().equals("KeyRef")) {
			pinRef.setKeyRef(StringUtils.toByteArray(n.getTextContent()));
		    } else if (n.getLocalName().equals("Protected")) {
			pinRef.setProtected(Boolean.parseBoolean(n.getTextContent()));
		    }
		}
	    } else if (e.getLocalName().equals("PinValue")) {
		pinValue = e.getTextContent();
	    } else if (e.getLocalName().equals("PasswordAttributes")) {
		passwordAttributes = new PasswordAttributesType();
		NodeList nodeList = e.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);

		    if (n.getLocalName().equals("pwdFlags")) {
			passwordAttributes.getPwdFlags().addAll(Arrays.asList(n.getTextContent().split(" ")));
		    } else if (n.getLocalName().equals("pwdType")) {
			passwordAttributes.setPwdType(PasswordTypeType.fromValue(n.getTextContent()));
		    } else if (n.getLocalName().equals("minLength")) {
			passwordAttributes.setMinLength(new BigInteger(n.getTextContent()));
		    } else if (n.getLocalName().equals("maxLength")) {
			passwordAttributes.setMaxLength(new BigInteger(n.getTextContent()));
		    } else if (n.getLocalName().equals("storedLength")) {
			passwordAttributes.setStoredLength(new BigInteger(n.getTextContent()));
		    } else if (n.getLocalName().equals("padChar")) {
			passwordAttributes.setPadChar(StringUtils.toByteArray(n.getTextContent()));
		    }
		}
	    } else if (e.getLocalName().equals("StateInfo")) {
		// TODO
	    }
	}
    }

    public KeyRefType getPINRef() {
	return pinRef;
    }

    public String getPINValue() {
	return pinValue;
    }

    public PasswordAttributesType getPasswordAttributes() {
	return passwordAttributes;
    }

    public String getProtocol() {
	return protocol;
    }

    public StateInfoType getStateInfo() {
	throw new UnsupportedOperationException("Not yet implemented");
    }

}
