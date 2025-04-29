/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
 */
package org.openecard.common.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.openecard.common.OpenecardProperties
import org.openecard.common.util.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Helper class to make life with DIDAuthenticationDataTypes much easier.
 *
 * @author Tobias Wich
 */
class AuthDataMap(data: DIDAuthenticationDataType) {
    private val ignoreNs = OpenecardProperties.getProperty("legacy.invalid_schema").toBoolean()
    val protocol: String
    private val contentMap = HashMap<QName, Element>()
    private val attributeMap: HashMap<QName, String>
    private val xmlDoc: Document

    init {
        this.protocol = data.protocol
        // read content
        val content = data.any
        for (next in content) {
            val name = next.localName
            val ns = next.namespaceURI
            var qname = QName(ns, name)
            // when ns should be ignored, always omit the ns part
            if (ignoreNs) {
                qname = QName(qname.localPart)
            }
            contentMap[qname] = next
        }
        // read other attributes
        attributeMap = HashMap(data.otherAttributes)
        // save document so new elements can be created -- there must always be an element, or this thing won't work
        xmlDoc = if (content.isEmpty()) loadXMLBuilder() else content[0].ownerDocument
    }

    @Throws(ParserConfigurationException::class)
    private fun loadXMLBuilder(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        return builder.newDocument()
    }

    fun <T : DIDAuthenticationDataType?> createResponse(responseObj: T): AuthDataResponse<T> {
        responseObj!!.protocol = protocol
        return AuthDataResponse(xmlDoc, responseObj)
    }

    fun containsContent(qname: QName): Boolean {
        var qname = qname
        if (ignoreNs) {
            qname = QName(qname.localPart)
        }
        return contentMap.containsKey(qname)
    }

    fun containsContent(ns: String?, localName: String): Boolean {
        return containsContent(QName(ns, localName))
    }

    fun containsContent(localName: String): Boolean {
        return containsContent(QName(ISONS, localName))
    }

    fun getContent(qname: QName): Element? {
        var qname = qname
        if (ignoreNs) {
            qname = QName(qname.localPart)
        }
        return contentMap[qname]
    }

    fun getContent(ns: String?, localName: String): Element? {
        return getContent(QName(ns, localName))
    }

    fun getContent(localName: String): Element? {
        return getContent(QName(ISONS, localName))
    }

    fun getContentAsString(qname: QName): String? {
        if (containsContent(qname)) {
            val content = getContent(qname)
            val contentStr = content!!.textContent
            return contentStr
        } else {
            return null
        }
    }

    fun getContentAsString(ns: String?, localName: String): String? {
        return getContentAsString(QName(ns, localName))
    }

    fun getContentAsString(localName: String): String? {
        return getContentAsString(QName(ISONS, localName))
    }

    fun getContentAsBytes(qname: QName): ByteArray? {
        if (containsContent(qname)) {
            val content = getContentAsString(qname)
            val contentBytes = StringUtils.toByteArray(content!!, true)
            return contentBytes
        } else {
            return null
        }
    }

    fun getContentAsBytes(ns: String?, localName: String): ByteArray? {
        return getContentAsBytes(QName(ns, localName))
    }

    fun getContentAsBytes(localName: String): ByteArray? {
        return getContentAsBytes(QName(ISONS, localName))
    }

    fun containsAttribute(qname: QName): Boolean {
        return attributeMap.containsKey(qname)
    }

    fun containsAttribute(ns: String?, name: String): Boolean {
        return containsAttribute(QName(ns, name))
    }

    fun containsAttribute(name: String): Boolean {
        return containsAttribute(QName(ISONS, name))
    }

    fun getAttribute(qname: QName): String? {
        return attributeMap[qname]
    }

    fun getAttribute(ns: String?, name: String): String? {
        return getAttribute(QName(ns, name))
    }

    fun getAttribute(name: String): String? {
        return getAttribute(QName(ISONS, name))
    }

    companion object {
        private const val ISONS = "urn:iso:std:iso-iec:24727:tech:schema"
    }
}
