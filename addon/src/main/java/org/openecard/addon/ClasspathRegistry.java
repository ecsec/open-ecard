/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.addon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.LocalizedString;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * Addon registry serving add-ons from the classpath of the base app.
 * This type of registry works for JNLP and integrated plugins.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ClasspathRegistry implements AddonRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathRegistry.class);

    private final ArrayList<AddonSpecification> registeredAddons = new ArrayList<AddonSpecification>();


    public ClasspathRegistry() throws WSMarshallerException {
	WSMarshaller marshaller = WSMarshallerFactory.createInstance();
	marshaller.addXmlTypeClass(AddonSpecification.class);

	try {
	    String name = "TCToken-Manifest.xml";
	    InputStream manifestStream = FileUtils.resolveResourceAsStream(ClasspathRegistry.class, name);
	    Document manifestDoc = marshaller.str2doc(manifestStream);
	    register((AddonSpecification) marshaller.unmarshal(manifestDoc));
	    logger.info("Loaded TR-03112 Add-on.");
	} catch (IOException ex) {
	    logger.warn("Failed to load TR-03112 Add-on.", ex);
	} catch (SAXException ex) {
	    logger.warn("Failed to load TR-03112 Add-on.", ex);
	} catch (WSMarshallerException ex) {
	    logger.warn("Failed to load TR-03112 Add-on.", ex);
	}

	try {
	    String name = "PIN-Plugin-Manifest.xml";
	    InputStream manifestStream = FileUtils.resolveResourceAsStream(ClasspathRegistry.class, name);
	    Document manifestDoc = marshaller.str2doc(manifestStream);
	    register((AddonSpecification) marshaller.unmarshal(manifestDoc));
	    logger.info("Loaded PIN-Management Add-on.");
	} catch (IOException ex) {
	    logger.warn("Failed to load PIN-Management Add-on.", ex);
	} catch (SAXException ex) {
	    logger.warn("Failed to load PIN-Management Add-on.", ex);
	} catch (WSMarshallerException ex) {
	    logger.warn("Failed to load PIN-Management Add-on.", ex);
	}

	try {
	    String name = "GenericCrypto-Plugin-Manifest.xml";
	    InputStream manifestStream = FileUtils.resolveResourceAsStream(ClasspathRegistry.class, name);
	    Document manifestDoc = marshaller.str2doc(manifestStream);
	    register((AddonSpecification) marshaller.unmarshal(manifestDoc));
	    logger.info("Loaded GenericCrypto Add-on.");
	} catch (IOException ex) {
	    logger.warn("Failed to load GenericCrypto Add-on.", ex);
	} catch (SAXException ex) {
	    logger.warn("Failed to load GenericCrypto Add-on.", ex);
	} catch (WSMarshallerException ex) {
	    logger.warn("Failed to load GenericCrypto Add-on.", ex);
	}

	try {
	    String name = "Status-Plugin-Manifest.xml";
	    InputStream manifestStream = FileUtils.resolveResourceAsStream(ClasspathRegistry.class, name);
	    Document manifestDoc = marshaller.str2doc(manifestStream);
	    register((AddonSpecification) marshaller.unmarshal(manifestDoc));
	    logger.info("Loaded Status Add-on.");
	} catch (IOException ex) {
	    logger.warn("Failed to load Status Add-on.", ex);
	} catch (SAXException ex) {
	    logger.warn("Failed to load Status Add-on.", ex);
	} catch (WSMarshallerException ex) {
	    logger.warn("Failed to load Status Add-on.", ex);
	}
    }

    public final void register(AddonSpecification desc) {
	registeredAddons.add(desc);
    }

    @Override
    public Set<AddonSpecification> listPlugins() {
	Set<AddonSpecification> list = new HashSet<AddonSpecification>();
	list.addAll(registeredAddons);
	return list;
    }

    @Override
    public AddonSpecification search(String id) {
	for (AddonSpecification desc : registeredAddons) {
	    if (desc.getId().equals(id)) {
		return desc;
	    }
	}
	return null;
    }

    @Override
    public Set<AddonSpecification> searchByName(String name) {
	Set<AddonSpecification> matchingAddons = new HashSet<AddonSpecification>();
	for (AddonSpecification desc : registeredAddons) {
	    for (LocalizedString s : desc.getLocalizedName()) {
		if (s.getValue().equals(name)) {
		    matchingAddons.add(desc);
		}
	    }
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchIFDProtocol(String uri) {
	Set<AddonSpecification> matchingAddons = new HashSet<AddonSpecification>();
	for (AddonSpecification desc : registeredAddons) {
	    ProtocolPluginSpecification protocolDesc = desc.searchIFDActionByURI(uri);
	    if (protocolDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchSALProtocol(String uri) {
	Set<AddonSpecification> matchingAddons = new HashSet<AddonSpecification>();
	for (AddonSpecification desc : registeredAddons) {
	    ProtocolPluginSpecification protocolDesc = desc.searchSALActionByURI(uri);
	    if (protocolDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public ClassLoader downloadPlugin(AddonSpecification addonSpec) {
	// TODO use other own classloader impl with security features
	return this.getClass().getClassLoader();
    }

    @Override
    public Set<AddonSpecification> searchByResourceName(String resourceName) {
	Set<AddonSpecification> matchingAddons = new HashSet<AddonSpecification>();
	for (AddonSpecification desc : registeredAddons) {
	    AppPluginSpecification actionDesc = desc.searchByResourceName(resourceName);
	    if (actionDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchByActionId(String actionId) {
	Set<AddonSpecification> matchingAddons = new HashSet<AddonSpecification>();
	for (AddonSpecification desc : registeredAddons) {
	    AppExtensionSpecification actionDesc = desc.searchByActionId(actionId);
	    if (actionDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

}
