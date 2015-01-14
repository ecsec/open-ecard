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

package org.openecard.common.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Helper class to make life with DIDAuthenticationDataTypes much easier.
 *
 * @author Tobias Wich
 */
public class AuthDataMap {

    private static final String isoNs = "urn:iso:std:iso-iec:24727:tech:schema";

    private final boolean ignoreNs;
    private final String protocol;
    private final HashMap<QName, Element> contentMap = new HashMap<>();
    private final HashMap<QName, String> attributeMap;
    private final Document xmlDoc;

    public AuthDataMap(DIDAuthenticationDataType data) throws ParserConfigurationException {
	ignoreNs = Boolean.valueOf(OpenecardProperties.getProperty("legacy.ignore_ns"));
	this.protocol = data.getProtocol();
	// read content
	List<Element> content = data.getAny();
	for (Element next : content) {
	    String name = next.getLocalName();
	    String ns = next.getNamespaceURI();
	    QName qname = new QName(ns, name);
	    // when ns should be ignored, always omit the ns part
	    if (ignoreNs) {
		qname = new QName(qname.getLocalPart());
	    }
	    contentMap.put(qname, next);
	}
	// read other attributes
	attributeMap = new HashMap<>(data.getOtherAttributes());
	// save document so new elements can be created -- there must always be an element, or this thing won't work
	xmlDoc = content.isEmpty() ? loadXMLBuilder() : content.get(0).getOwnerDocument();
    }

    private Document loadXMLBuilder() throws ParserConfigurationException {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	return builder.newDocument();
    }

    public AuthDataResponse createResponse(DIDAuthenticationDataType responseObj) {
	responseObj.setProtocol(protocol);
	return new AuthDataResponse(xmlDoc, responseObj);
    }

    public String getProtocol() {
	return protocol;
    }

    public boolean containsContent(QName qname) {
	if (ignoreNs) {
	    qname = new QName(qname.getLocalPart());
	}
	return contentMap.containsKey(qname);
    }
    public boolean containsContent(String ns, String localName) {
	return containsContent(new QName(ns, localName));
    }
    public boolean containsContent(String localName) {
	return containsContent(new QName(isoNs, localName));
    }

    public Element getContent(QName qname) {
	if (ignoreNs) {
	    qname = new QName(qname.getLocalPart());
	}
	return contentMap.get(qname);
    }
    public Element getContent(String ns, String localName) {
	return getContent(new QName(ns, localName));
    }
    public Element getContent(String localName) {
	return getContent(new QName(isoNs, localName));
    }

    public String getContentAsString(QName qname) {
	if (containsContent(qname)) {
	    Element content = getContent(qname);
	    String contentStr = content.getTextContent();
	    return contentStr;
	} else {
	    return null;
	}
    }
    public String getContentAsString(String ns, String localName) {
	return getContentAsString(new QName(ns, localName));
    }
    public String getContentAsString(String localName) {
	return getContentAsString(new QName(isoNs, localName));
    }

    public byte[] getContentAsBytes(QName qname) {
	if (containsContent(qname)) {
	    String content = getContentAsString(qname);
	    byte[] contentBytes = StringUtils.toByteArray(content, true);
	    return contentBytes;
	} else {
	    return null;
	}
    }
    public byte[] getContentAsBytes(String ns, String localName) {
	return getContentAsBytes(new QName(ns, localName));
    }
    public byte[] getContentAsBytes(String localName) {
	return getContentAsBytes(new QName(isoNs, localName));
    }

    public boolean containsAttribute(QName qname) {
	return attributeMap.containsKey(qname);
    }
    public boolean containsAttribute(String ns, String name) {
	return containsAttribute(new QName(ns, name));
    }
    public boolean containsAttribute(String name) {
	return containsAttribute(new QName(isoNs, name));
    }

    public String getAttribute(QName qname) {
	return attributeMap.get(qname);
    }
    public String getAttribute(String ns, String name) {
	return getAttribute(new QName(ns, name));
    }
    public String getAttribute(String name) {
	return getAttribute(new QName(isoNs, name));
    }

}
