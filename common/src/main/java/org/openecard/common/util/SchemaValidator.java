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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


/**
 * Utility class which allows to validate an JAXB Class against one or more schemas.
 *
 * @author Hans-Martin Haase
 */
public class SchemaValidator {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidator.class);

    /**
     * Validate an object which have to be a JAXB class against one or more schemas.
     *
     * Note: If you pass one single schema into the function the validator tries to load referenced schemas. If you
     * specify more than one schema you have to specify all other schemas which are referenced in the schemas. This have
     * to be done in the correct order. Means at first schemas which define the types which are used in the other schemas.
     * 
     * @param obj The object to validate. This have to be an JAXB class.
     * @param schemaNames Names of the schemas which shall be used in the validation process.
     * @return TRUE if the object is valid against the schemas else FALSE.
     * @throws SAXException Thrown if one of the validator components could not be initialized correctly.
     * @throws JAXBException Thrown if the JAXBContext or the JAXBSource object could not be initialized.
     * @throws IOException Thrown if no schema was given or a schema couldn't be found.
     */
    public static boolean validateObject(Object obj, String ... schemaNames) throws SAXException, JAXBException, IOException {
	JAXBContext jc = JAXBContext.newInstance(obj.getClass());
	JAXBSource source = new JAXBSource(jc, obj);
	SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	Schema schema;
	try {
	    if (schemaNames == null || schemaNames.length == 0) {
		throw new IOException("No schemas given to validate the object.");
	    } else if (schemaNames.length == 1) {
		URL schemaURL = FileUtils.resolveResourceAsURL(SchemaValidator.class, schemaNames[0]);
		schema = sf.newSchema(schemaURL);
	    } else {
		StreamSource[] schemaDocuments = convertSchemaStrings2StreamSources(schemaNames);
		schema = sf.newSchema(schemaDocuments);
	    }
	} catch (IOException ex) {
	    logger.error("No schemas given for the validation or the schemas could not be found.", ex);
	    throw ex;
	}

	try {
	    Validator validator= schema.newValidator();
	    validator.setErrorHandler(new CustomErrorHandler());
	    validator.validate(source);
	    return true;
	} catch (SAXException ex) {
	    logger.error("Validation of the input object failed.", ex);
	    return false;
	}
    }

    /**
     * Converts an array of of schema to name Strings to an array of StreamSource objects.
     *
     * @param schemaNames Array with schema names.
     * @return An array of StreamSource object.
     * @throws IOException Thrown if a schema file was not found.
     */
    private static StreamSource[] convertSchemaStrings2StreamSources(String[] schemaNames) throws IOException {
	ArrayList<StreamSource> sources = new ArrayList<>();

	for(String schemaName : schemaNames) {
	    StreamSource source = new StreamSource(FileUtils.resolveResourceAsStream(SchemaValidator.class, schemaName));
	    sources.add(source);
	}

	return sources.toArray(new StreamSource[sources.size()]);
    }

}
