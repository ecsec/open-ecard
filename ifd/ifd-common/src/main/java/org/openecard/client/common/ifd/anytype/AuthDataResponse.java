/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
