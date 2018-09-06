/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import org.openecard.common.interfaces.ObjectValidatorException;
import org.openecard.common.interfaces.ObjectSchemaValidator;
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
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Utility class which allows to validate a JAXB object against XML schemas.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class MarshallerSchemaValidator implements ObjectSchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger(MarshallerSchemaValidator.class);

    private static final String ANDROID_FACTORY = "org.apache.xerces.jaxp.validation.XMLSchemaFactory";

    private final Schema schema;
    private final WSMarshaller wsm;

    /**
     * Loads a JAXBSchemaValidator instance based on the given JAXB class and schemas.
     *
     * @param clazz Class of the JAXB element which shall be validated.
     * @param schemaNames Resource names of the schemas which shall be used in the validation process.
     * @return Instance if the schema validator capable of verificating the given schema.
     * @throws IOException Thrown in case the schemas could not be loaded from the given resources.
     * @throws SAXException Thrown in case the XML schemas are errornous.
     * @throws WSMarshallerException Thrown in case the given JAXB class or one of its decendents is invalid.
     */
    public static ObjectSchemaValidator load(@Nonnull Class<?> clazz, String... schemaNames) throws IOException,
	    SAXException, WSMarshallerException {
	try {
	    if (schemaNames == null || schemaNames.length == 0) {
		throw new IOException("No schemas given to validate the object.");
	    } else if (schemaNames.length == 1) {
		URL schemaURL = FileUtils.resolveResourceAsURL(MarshallerSchemaValidator.class, schemaNames[0]);
		return new MarshallerSchemaValidator(clazz, schemaURL);
	    } else {
		StreamSource[] schemaDocuments = convertSchemaStrings2StreamSources(schemaNames);
		return new MarshallerSchemaValidator(clazz, schemaDocuments);
	    }
	} catch (IOException ex) {
	    LOG.error("No schemas given for the validation or the schemas could not be found.", ex);
	    throw ex;
	}
    }

    private MarshallerSchemaValidator(Class<?> clazz, URL schemaURL) throws SAXException, WSMarshallerException {
	schema = getSchemaFactory().newSchema(schemaURL);
	wsm = getMarshaller(clazz);
    }

    private MarshallerSchemaValidator(Class<?> clazz, StreamSource[] schemaDocuments) throws SAXException, WSMarshallerException {
	schema = getSchemaFactory().newSchema(schemaDocuments);
	wsm = getMarshaller(clazz);
    }


    /**
     * Validates a JAXB object against the loaded XML schemas.
     *
     * Note: If you pass one single schema into the function the validator tries to load referenced schemas. If you
     * specify more than one schema you have to specify all other schemas which are referenced in the schemas. This has
     * to be done in the correct order. The correct ordering is definition first, then use of the definitions.
     *
     * @param obj The object to validate. This have to be an JAXB class.
     * @return TRUE if the object is valid against the schemas else FALSE.
     * @throws ObjectValidatorException Thrown if the JAXBSource object could not be initialized.
     */
    @Override
    public boolean validateObject(@Nonnull Object obj) throws ObjectValidatorException {
	try {
	    Document doc = wsm.marshal(obj);
	    Source source = new DOMSource(doc);
	    Validator validator= schema.newValidator();
	    validator.setErrorHandler(new CustomErrorHandler());
	    validator.validate(source);
	    return true;
	} catch (SAXException ex) {
	    LOG.error("Validation of the input object failed.", ex);
	    return false;
	} catch (MarshallingTypeException ex) {
	    String msg = "Failed to create Source instance from given object.";
	    LOG.error(msg, ex);
	    throw new ObjectValidatorException(msg, ex);
	} catch (IOException ex) {
	    LOG.error("No object given for validation.", ex);
	    throw new ObjectValidatorException("No object given for validation.", ex);
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
	ArrayList<StreamSource> sources = new ArrayList<>();

	for(String schemaName : schemaNames) {
	    StreamSource source = new StreamSource(FileUtils.resolveResourceAsStream(MarshallerSchemaValidator.class, schemaName));
	    sources.add(source);
	}

	return sources.toArray(new StreamSource[sources.size()]);
    }

    private static SchemaFactory getSchemaFactory() throws WSMarshallerException {
	try {
	    return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	} catch (IllegalArgumentException ex) {
	    LOG.warn("Did not find a default SchemaFactory.");
	}
	try {
	    // on android we need an additional library for XML support
	    ClassLoader cl = MarshallerSchemaValidator.class.getClassLoader();
	    return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI, ANDROID_FACTORY, cl);
	} catch (IllegalArgumentException ex) {
	    String msg = "No SchemaFactory available on this platform.";
	    LOG.warn(msg);
	    throw new WSMarshallerException(msg);
	}
    }

    private WSMarshaller getMarshaller(Class<?> clazz) throws WSMarshallerException {
	WSMarshaller m = WSMarshallerFactory.createInstance();
	m.removeAllTypeClasses();
	m.addXmlTypeClass(clazz);
	return m;
    }

    private class CustomErrorHandler implements ErrorHandler {

	@Override
	public void warning(SAXParseException exception) throws SAXException {
	    // Ignore this. One of the TRs demands to accept as much as possible.
	    LOG.warn(exception.getLocalizedMessage());
	    throw exception;
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
