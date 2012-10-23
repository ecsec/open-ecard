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

package org.openecard.client.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import iso.std.iso_iec._24727.tech.schema.StateInfoType;
import java.math.BigInteger;
import org.openecard.client.common.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PinCompareMarkerType {

    private KeyRefType pinRef = null;
    private String pinValue = null;
    private PasswordAttributesType passwordAttributes = null;
    private final String protocol;

    /**
     * 
     * @param baseType the iso PinCompareMarkerType to create our PinCompareMarkerType from
     */
    public PinCompareMarkerType(iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType baseType) {
	this.protocol = baseType.getProtocol();
	for (Element elem : baseType.getAny()) {
	    if (elem.getLocalName().equals("PinRef")) {
		pinRef = new KeyRefType();
		NodeList nodeList = elem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);
		    if (n.getLocalName().equals("KeyRef")) {
			pinRef.setKeyRef(StringUtils.toByteArray(n.getTextContent()));
		    } else if (n.getLocalName().equals("Protected")) {
			pinRef.setProtected(Boolean.parseBoolean(n.getTextContent()));
		    }
		}
	    } else if (elem.getLocalName().equals("PinValue")) {
		pinValue = elem.getTextContent();
	    } else if (elem.getLocalName().equals("PasswordAttributes")) {
		passwordAttributes = new PasswordAttributesType();
		NodeList nodeList = elem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);
		    if (n.getLocalName().equals("pwdFlags")) {
			for (String s : n.getTextContent().split(" ")) {
			    passwordAttributes.getPwdFlags().add(s);
			}
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
	    } else if (elem.getLocalName().equals("StateInfo")) {
		;// TODO
	    }
	}
    }

    public KeyRefType getPinRef() {
	return pinRef;
    }

    public String getPinValue() {
	return pinValue;
    }

    public PasswordAttributesType getPasswordAttributes() {
	return passwordAttributes;
    }

    public String getProtocol() {
	return protocol;
    }

    public StateInfoType getStateInfo() throws Exception {
       throw new Exception("Not yet implemented");
    }

}
