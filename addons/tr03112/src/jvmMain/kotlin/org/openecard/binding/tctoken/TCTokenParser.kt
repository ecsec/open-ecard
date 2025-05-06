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
 */
package org.openecard.binding.tctoken

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.binding.tctoken.ex.InvalidTCTokenException
import org.openecard.common.I18n
import org.openecard.common.io.LimitedInputStream
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.SAXNotRecognizedException
import org.xml.sax.SAXNotSupportedException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

private val logger = KotlinLogging.logger { }

/**
 * Implements a parser for TCTokens.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class TCTokenParser {
	private val lang: I18n? = I18n.getTranslation("tr03112")
	private val saxFactory: SAXParserFactory = SAXParserFactory.newInstance()
	private val saxHandler: TCTokenSAXHandler = TCTokenSAXHandler()

	/**
	 * Creates a new parser for TCTokens.
	 *
	 * @throws IllegalArgumentException Thrown when the parser could not be initialized with the specified parameters.
	 */
	init {

		try {
			saxFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
		} catch (ex: ParserConfigurationException) {
			logger.warn(ex.message, ex)
			// Android doesn't support the corresponding xml feature
			// TODO: translate when exception changes
			// throw new IllegalArgumentException(lang.getOriginalMessage(UNSUPPORTED_FEATURE), ex);
		} catch (ex: SAXNotRecognizedException) {
			logger.warn(ex.message, ex)
		} catch (ex: SAXNotSupportedException) {
			logger.warn(ex.message, ex)
		}
	}

	/**
	 * Parse TCTokens from the input stream.
	 *
	 * @param data Data
	 * @return List of TCTokens
	 * @throws InvalidTCTokenException Thrown in case the SAX parser had an error.
	 */
	@Throws(InvalidTCTokenException::class)
	fun parse(data: String): List<TCToken> = parse(ByteArrayInputStream(data.toByteArray()))

	/**
	 * Parse TCTokens from given the input stream.
	 *
	 * @param inputStream Input stream
	 * @return List of TCTokens
	 * @throws InvalidTCTokenException Thrown in case the SAX parser had an error reading the stream.
	 */
	@Throws(InvalidTCTokenException::class)
	fun parse(inputStream: InputStream): List<TCToken> {
		try {
			// Parse TCTokens
			val saxParser = saxFactory.newSAXParser()
			val reader = saxParser.getXMLReader()
			reader.setContentHandler(saxHandler)
			val stream = LimitedInputStream(inputStream)
			reader.parse(InputSource(stream))

			// Get TCTokens
			val tokens = saxHandler.tCTokens

			return tokens
		} catch (ex: ParserConfigurationException) {
			logger.error(ex) { "${ex.message}" }
			throw InvalidTCTokenException(ErrorTranslations.MALFORMED_TOKEN, ex)
		} catch (ex: SAXException) {
			logger.error(ex) { "${ex.message}" }
			throw InvalidTCTokenException(ErrorTranslations.MALFORMED_TOKEN, ex)
		} catch (ex: IOException) {
			logger.error(ex) { "${ex.message}" }
			throw InvalidTCTokenException(ErrorTranslations.MALFORMED_TOKEN, ex)
		}
	}
}
