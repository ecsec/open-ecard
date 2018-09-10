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
 ***************************************************************************/

package org.openecard.common.util;

import org.openecard.common.interfaces.DocumentValidatorException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.openecard.common.interfaces.DocumentSchemaValidator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Utility class which allows to validate documents against the eCard schemas.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class JAXPSchemaValidator implements DocumentSchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger(JAXPSchemaValidator.class);

    private static final String XERCES_FACTORY = "org.apache.xerces.jaxp.validation.XMLSchemaFactory";

    private final Schema schema;

    /**
     * Loads a ECardSchemaValidator instance based on the given schemas.
     *
     * @param schemaNames Resource names of the schemas which shall be used in the validation process.
     * @return Instance if the schema validator capable of verificating the given schema.
     * @throws IOException Thrown in case the schemas could not be loaded from the given resources.
     * @throws SAXException Thrown in case the XML schemas are errornous.
     */
    public static DocumentSchemaValidator load(String... schemaNames) throws IOException, SAXException {
	try {
	    if (schemaNames == null || schemaNames.length == 0) {
		throw new IOException("No schemas given to validate the object.");
	    } else {
		StreamSource[] schemaDocuments = convertSchemaStrings2StreamSources(schemaNames);
		return new JAXPSchemaValidator(schemaDocuments);
	    }
	} catch (IOException ex) {
	    LOG.error("Not all schemas could not be found or loaded.", ex);
	    throw ex;
	}
    }

    private JAXPSchemaValidator(URL schemaURL) throws SAXException {
	schema = getSchemaFactory().newSchema(schemaURL);
    }

    private JAXPSchemaValidator(StreamSource[] schemaDocuments) throws SAXException {
	schema = getSchemaFactory().newSchema(schemaDocuments);
    }


    @Override
    public void validate(@Nonnull Document doc) throws DocumentValidatorException {
	validateNode(doc);
    }

    @Override
    public void validate(@Nonnull Element doc) throws DocumentValidatorException {
	validateNode(doc);
    }

    private void validateNode(@Nonnull Node doc) throws DocumentValidatorException {
	try {
	    Source source = new DOMSource(doc);
	    Validator validator= schema.newValidator();
	    validator.setErrorHandler(new CustomErrorHandler());
	    validator.validate(source);
	} catch (SAXException ex) {
	    LOG.error("Validation of the input object failed.", ex);
	    throw new DocumentValidatorException("Failed to validate eCard message.", ex);
	} catch (IOException ex) {
	    throw new IllegalArgumentException("Given object contains errors.", ex);
	}
    }


    /**
     * Converts an array of schema name strings to an array of StreamSource objects.
     *
     * @param schemaNames Array with schema names.
     * @return An array of StreamSource object.
     * @throws IOException Thrown in case a schema file was not found.
     */
    private static StreamSource[] convertSchemaStrings2StreamSources(String[] schemaNames) throws IOException {
	ArrayList<StreamSource> ssources = new ArrayList<>();

	for(String sname : schemaNames) {
	    URL surl = FileUtils.resolveResourceAsURL(JAXPSchemaValidator.class, sname);
	    StreamSource ssource = new StreamSource(surl.toExternalForm());
	    ssources.add(ssource);
	}

	return ssources.toArray(new StreamSource[ssources.size()]);
    }

    private static SchemaFactory getSchemaFactory() throws SAXException {
	try {
	    // try to use original xerces if it is in the classpath
	    ClassLoader cl = JAXPSchemaValidator.class.getClassLoader();
	    return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI, XERCES_FACTORY, cl);
	} catch (IllegalArgumentException ex) {
	    LOG.warn("Did not find a default SchemaFactory.");
	}
	try {
	    // fallback to default implementation
	    return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	} catch (IllegalArgumentException ex) {
	    String msg = "No SchemaFactory available on this platform.";
	    LOG.warn(msg);
	    throw new SAXException(msg);
	}
    }

    private static class CustomErrorHandler implements ErrorHandler {

	@Override
	public void warning(SAXParseException exception) throws SAXException {
	    LOG.warn(exception.getLocalizedMessage());
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
	    throw exception;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
	    throw exception;
	}

    }

}
