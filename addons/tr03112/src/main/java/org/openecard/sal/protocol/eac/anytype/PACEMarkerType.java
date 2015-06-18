/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import org.openecard.common.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Convenience class to convert {@link iso.std.iso_iec._24727.tech.schema.PACEMarkerType} with AnyTypes to a type
 * with attributes instead.
 * 
 * @author Dirk Petrautzki
 */
public class PACEMarkerType {

    private KeyRefType passwordRef;
    private String passwordValue;
    private Integer minLength;
    private Integer maxLength;
    private String protocol;

    /**
     * Create a new PACEMarkerType from a {@link iso.std.iso_iec._24727.tech.schema.PACEMarkerType} as base.
     * 
     * @param paceMarker the PACEMarkerType to convert
     */
    public PACEMarkerType(iso.std.iso_iec._24727.tech.schema.PACEMarkerType paceMarker) {
	protocol = paceMarker.getProtocol();
	for (Element elem : paceMarker.getAny()) {
	    if (elem.getLocalName().equals("PasswordRef")) {
		passwordRef = new KeyRefType();
		NodeList algorithmInfoNodes = elem.getChildNodes();
		for (int i = 0; i < algorithmInfoNodes.getLength(); i++) {
		    Node node = algorithmInfoNodes.item(i);
		    if (node.getLocalName().equals("KeyRef")) {
			passwordRef.setKeyRef(StringUtils.toByteArray(node.getTextContent()));
		    } else if (node.getLocalName().equals("Protected")) {
			passwordRef.setProtected(Boolean.parseBoolean(node.getTextContent()));
		    }
		}
	    } else if (elem.getLocalName().equals("PasswordValue")) {
		passwordValue = elem.getTextContent();
	    } else if (elem.getLocalName().equals("minLength")) {
		minLength = Integer.parseInt(elem.getTextContent());
	    } else if (elem.getLocalName().equals("maxLength")) {
		maxLength = Integer.parseInt(elem.getTextContent());
	    } else if (elem.getLocalName().equals("StateInfo")) {
		// TODO
	    }
	}
    }

    /**
     * Returns the maximum length for this password type, or {@code Integer.MAX_VALUE} as default if none was set.
     * 
     * @return the maximum length for this password type
     */
    public int getMaxLength() {
	if (maxLength != null) {
	    return maxLength;
	} else {
	    return Integer.MAX_VALUE;
	}
    }

    /**
     * Returns the minimum length for this password type, or 0 as default if none was set.
     * 
     * @return the minimum length for this password type
     */
    public int getMinLength() {
	if (minLength != null) {
	    return minLength;
	} else {
	    return 0;
	}
    }

    public String getPasswordValue() {
	return passwordValue;
    }

    public KeyRefType getPasswordRef() {
	return passwordRef;
    }

    public String getProtocol() {
	return protocol;
    }

    public void setProtocol(String protocol) {
	this.protocol = protocol;
    }

}
