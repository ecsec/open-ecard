/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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
package org.openecard.common.util

import org.openecard.common.interfaces.DocumentSchemaValidator
import org.openecard.common.interfaces.DocumentValidatorException
import org.openecard.common.util.FileUtils.resolveResourceAsURL
import org.openecard.common.util.JAXPSchemaValidator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.ErrorHandler
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException
import java.io.IOException
import java.net.URL
import javax.annotation.Nonnull
import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

/**
 * Utility class which allows to validate documents against the eCard schemas.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class JAXPSchemaValidator : DocumentSchemaValidator {
    private val schema: Schema

    private constructor(schemaURL: URL) {
        schema = schemaFactory.newSchema(schemaURL)
    }

    private constructor(schemaDocuments: Array<StreamSource>) {
        schema = schemaFactory.newSchema(schemaDocuments)
    }


    @Throws(DocumentValidatorException::class)
    override fun validate(@Nonnull doc: Document) {
        validateNode(doc)
    }

    @Throws(DocumentValidatorException::class)
    override fun validate(@Nonnull doc: Element) {
        validateNode(doc)
    }

    @Throws(DocumentValidatorException::class)
    private fun validateNode(@Nonnull doc: Node) {
        try {
            val source: Source = DOMSource(doc)
            val validator = schema.newValidator()
            validator.errorHandler = CustomErrorHandler()
            validator.validate(source)
        } catch (ex: SAXException) {
            LOG.error("Validation of the input object failed.", ex)
            throw DocumentValidatorException("Failed to validate eCard message.", ex)
        } catch (ex: IOException) {
            throw IllegalArgumentException("Given object contains errors.", ex)
        }
    }


    private class CustomErrorHandler : ErrorHandler {
        @Throws(SAXException::class)
        override fun warning(exception: SAXParseException) {
            LOG.warn(exception.localizedMessage)
        }

        @Throws(SAXException::class)
        override fun error(exception: SAXParseException) {
            throw exception
        }

        @Throws(SAXException::class)
        override fun fatalError(exception: SAXParseException) {
            throw exception
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(JAXPSchemaValidator::class.java)

        private const val XERCES_FACTORY = "org.apache.xerces.jaxp.validation.XMLSchemaFactory"

        /**
         * Loads a ECardSchemaValidator instance based on the given schemas.
         *
         * @param schemaNames Resource names of the schemas which shall be used in the validation process.
         * @return Instance if the schema validator capable of verificating the given schema.
         * @throws IOException Thrown in case the schemas could not be loaded from the given resources.
         * @throws SAXException Thrown in case the XML schemas are errornous.
         */
        @JvmStatic
        @Throws(IOException::class, SAXException::class)
        fun load(vararg schemaNames: String): DocumentSchemaValidator {
            try {
                if (schemaNames == null || schemaNames.size == 0) {
                    throw IOException("No schemas given to validate the object.")
                } else {
                    val schemaDocuments = convertSchemaStrings2StreamSources(schemaNames)
                    return JAXPSchemaValidator(schemaDocuments)
                }
            } catch (ex: IOException) {
                LOG.error("Not all schemas could not be found or loaded.", ex)
                throw ex
            }
        }

        /**
         * Converts an array of schema name strings to an array of StreamSource objects.
         *
         * @param schemaNames Array with schema names.
         * @return An array of StreamSource object.
         * @throws IOException Thrown in case a schema file was not found.
         */
        @Throws(IOException::class)
        private fun convertSchemaStrings2StreamSources(schemaNames: Array<String>): Array<StreamSource> {
            val ssources = ArrayList<StreamSource>()

            for (sname in schemaNames) {
                val surl = resolveResourceAsURL(JAXPSchemaValidator::class.java, sname)
                val ssource = StreamSource(surl!!.toExternalForm())
                ssources.add(ssource)
            }

            return ssources.toTypedArray<StreamSource>()
        }

        @get:Throws(SAXException::class)
        private val schemaFactory: SchemaFactory
            get() {
                try {
                    // try to use original xerces if it is in the classpath
                    val cl = JAXPSchemaValidator::class.java.classLoader
                    return SchemaFactory.newInstance(
                        XMLConstants.W3C_XML_SCHEMA_NS_URI,
                        XERCES_FACTORY,
                        cl
                    )
                } catch (ex: IllegalArgumentException) {
                    LOG.warn("Did not find a default SchemaFactory.")
                }
                try {
                    // fallback to default implementation
                    return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                } catch (ex: IllegalArgumentException) {
                    val msg = "No SchemaFactory available on this platform."
                    LOG.warn(msg)
                    throw SAXException(msg)
                }
            }
    }
}
