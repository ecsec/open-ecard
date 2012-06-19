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

package org.openecard.client.common.ifd.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AuthDataResponse {

    private static final String isoNs = "urn:iso:std:iso-iec:24727:tech:schema";
    
    private DIDAuthenticationDataType responseObj;
    
    private final Document xmlDoc;

    protected AuthDataResponse(Document xmlDoc, DIDAuthenticationDataType responseObj) {
        this.xmlDoc = xmlDoc;
        this.responseObj = responseObj;
    }

    public DIDAuthenticationDataType getResponse() {
        return responseObj;
    }

    public Element addElement(QName qname, String data) {
        Element e;
        if (qname.getNamespaceURI() != null) {
            e = xmlDoc.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
        } else {
            e = xmlDoc.createElement(qname.getLocalPart());
        }
        e.setTextContent(data);
        // add to list and return
        responseObj.getAny().add(e);
        return e;
    }

    public Element addElement(String ns, String localName, String data) {
        return addElement(new QName(ns, localName), data);
    }

    public Element addElement(String localName, String data) {
        return addElement(new QName(isoNs, localName), data);
    }

}
