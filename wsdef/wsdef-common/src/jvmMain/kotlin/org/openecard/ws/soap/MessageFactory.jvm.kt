/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.ws.soap

import io.github.oshai.kotlinlogging.KotlinLogging
import org.w3c.dom.Document
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

private val LOG = KotlinLogging.logger {}

/**
 *
 * @author Tobias Wich
 */
class MessageFactory private constructor(
	private val protocol: String,
	private val domBuilder: DocumentBuilder,
) {
	@Throws(SOAPException::class)
	fun createMessage(): SOAPMessage {
		val msg =
			SOAPMessage(
				domBuilder,
				getNamespace(
					protocol,
				),
			)
		return msg
	}

	@Throws(SOAPException::class)
	fun createMessage(doc: Document): SOAPMessage {
		val msg = SOAPMessage(doc)
		return msg
	}

	companion object {
		@Throws(SOAPException::class)
		fun newInstance(domBuilder: DocumentBuilder): MessageFactory =
			newInstance(SOAPConstants.DEFAULT_SOAP_PROTOCOL, domBuilder)

		@JvmOverloads
		@Throws(SOAPException::class)
		fun newInstance(protocol: String = SOAPConstants.DEFAULT_SOAP_PROTOCOL): MessageFactory {
			try {
				val tmpW3Factory = DocumentBuilderFactory.newInstance()
				tmpW3Factory.isNamespaceAware = true
				tmpW3Factory.isIgnoringComments = true
				try {
					tmpW3Factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
				} catch (ex: ParserConfigurationException) {
					// LOG.warn { "Failed to enable secure processing for DOM Builder." }
				}
				// XXE countermeasures
				tmpW3Factory.isExpandEntityReferences = false
				try {
					tmpW3Factory.isXIncludeAware = false
				} catch (ex: UnsupportedOperationException) {
					// LOG.warn { "Failed to disable XInclude support." }
				}
				try {
					tmpW3Factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
				} catch (ex: IllegalArgumentException) {
					// LOG.warn { "Failed to disallow external DTD access." }
				}
				try {
					tmpW3Factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
				} catch (ex: ParserConfigurationException) {
					// LOG.warn { "Failed to disallow DTDs entirely." }
				}
				try {
					tmpW3Factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
					tmpW3Factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
				} catch (ex: ParserConfigurationException) {
					// LOG.warn { "Failed to disable XEE mitigations." }
				}

				val tmpW3Builder = tmpW3Factory.newDocumentBuilder()

				return newInstance(protocol, tmpW3Builder)
			} catch (ex: ParserConfigurationException) {
				throw SOAPException(ex)
			}
		}

		@Throws(SOAPException::class)
		fun newInstance(
			protocol: String,
			domBuilder: DocumentBuilder,
		): MessageFactory = MessageFactory(protocol, domBuilder)

		@Throws(SOAPException::class)
		private fun getNamespace(protocol: String): String =
			when (protocol) {
				SOAPConstants.SOAP_1_1_PROTOCOL -> {
					SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE
				}
				SOAPConstants.SOAP_1_2_PROTOCOL -> {
					SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE
				}
				else -> {
					throw SOAPException("Unsupported SOAP protocol.")
				}
			}

		@Throws(SOAPException::class)
		internal fun verifyNamespace(namespace: String): String =
			when (namespace) {
				SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE -> {
					namespace
				}
				SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE -> {
					namespace
				}
				else -> {
					throw SOAPException("Unsupported SOAP protocol.")
				}
			}
	}
}
