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

package org.openecard.addon.manifest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@XmlRootElement(name = "AddonSpecification")
@XmlType(propOrder = { "id", "version", "license", "localizedName", "localizedDescription", "about", "logo",
	"configDescription", "bindingActions", "applicationActions", "ifdActions", "salActions" })
public class AddonSpecification {

    private static final Logger logger = LoggerFactory.getLogger(AddonSpecification.class);

    private String id;
    private String logoFile;
    private byte[] logoBytes;
    private String version;
    private String license;
    private Configuration configDescription;
    private final List<LocalizedString> localizedName = new ArrayList<LocalizedString>();
    private final List<LocalizedString> localizedDescription = new ArrayList<LocalizedString>();
    private final List<LocalizedString> about = new ArrayList<LocalizedString>();
    private final ArrayList<AppExtensionSpecification> appExtensionActions = new ArrayList<AppExtensionSpecification>();
    private final ArrayList<AppPluginSpecification> appPluginActions = new ArrayList<AppPluginSpecification>();
    private final ArrayList<ProtocolPluginSpecification> ifdActions = new ArrayList<ProtocolPluginSpecification>();
    private final ArrayList<ProtocolPluginSpecification> salActions = new ArrayList<ProtocolPluginSpecification>();

    @XmlElement(name = "ID")
    public String getId() {
	return id;
    }

    @XmlElement(name = "LocalizedName")
    public List<LocalizedString> getLocalizedName() {
	return localizedName;
    }

    public String getLocalizedName(String languageCode) {
	String fallback = "No localized Name found.";
	for (LocalizedString s : localizedName) {
	    if (s.getLang().equalsIgnoreCase(languageCode)) {
		return s.getValue();
	    } else if (s.getLang().equalsIgnoreCase("EN")) {
		fallback = s.getValue();
	    }
	}
	return fallback;
    }

    @XmlElement(name = "LocalizedDescription")
    public List<LocalizedString> getLocalizedDescription() {
	return localizedDescription;
    }

    public String getLocalizedDescription(String languageCode) {
	String fallback = "No localized Description found.";
	for (LocalizedString s : localizedDescription) {
	    if (s.getLang().equalsIgnoreCase(languageCode)) {
		return s.getValue();
	    } else if (s.getLang().equalsIgnoreCase("EN")) {
		fallback = s.getValue();
	    }
	}
	return fallback;
    }

    @XmlElement(name = "Logo")
    public String getLogo() {
	return logoFile;
    }

    @XmlElement(name = "Version")
    public String getVersion() {
	return version;
    }

    @XmlElement(name = "About")
    public List<LocalizedString> getAbout() {
	return about;
    }

    public String getAbout(String languageCode) {
	//TODO implement
	throw new UnsupportedOperationException("Not yet implemented.");
    }

    @XmlElement(name = "License")
    public String getLicense() {
	return license;
    }

    @XmlElementWrapper(name = "BindingActions")
    @XmlElement(name = "AppPluginSpecification")
    public ArrayList<AppPluginSpecification> getBindingActions() {
	return appPluginActions;
    }

    @XmlElementWrapper(name = "IFDActions")
    @XmlElement(name = "ProtocolPluginSpecification")
    public ArrayList<ProtocolPluginSpecification> getIfdActions() {
	return ifdActions;
    }

    @XmlElementWrapper(name = "ApplicationActions")
    @XmlElement(name = "AppExtensionSpecification")
    public ArrayList<AppExtensionSpecification> getApplicationActions() {
	return appExtensionActions;
    }

    @XmlElementWrapper(name = "SALActions")
    @XmlElement(name = "ProtocolPluginSpecification")
    public ArrayList<ProtocolPluginSpecification> getSalActions() {
	return salActions;
    }

    @XmlElement(name = "ConfigDescription")
    public Configuration getConfigDescription() {
	return configDescription;
    }

    public void setId(String id) {
	this.id = id;
    }

    public void setLogo(String logo) {
	this.logoFile = logo;
	logger.debug("LogoFile: " + logoFile);
	if (logoFile != null && !logoFile.isEmpty()) {
	    try {
		// TODO security checks and maybe modified loading
		InputStream logoStream = FileUtils.resolveResourceAsStream(AddonSpecification.class, logoFile);
		this.logoBytes = FileUtils.toByteArray(logoStream);
	    } catch (FileNotFoundException e) {
		logger.error("Logo file couldn't be found.", e);
	    } catch (IOException e) {
		logger.error("Logo file couldn't be read.", e);
	    } catch (NullPointerException e) {
		logger.error("Logo file couldn't be read.", e);
	    }
	}
    }

    public byte[] getLogoBytes() {
	return logoBytes;
    }

    public void setVersion(String version) {
	this.version = version;
    }

    public void setLicense(String license) {
	this.license = license;
    }

    public void setConfigDescription(Configuration configDescription) {
	this.configDescription = configDescription;
    }

    public AppPluginSpecification searchByResourceName(String resourceName) {
	for (AppPluginSpecification desc : appPluginActions) {
	    if (resourceName.equals(desc.getResourceName())) {
		return desc;
	    }
	}
	return null;
    }

    public AppExtensionSpecification searchByActionId(String id) {
	for (AppExtensionSpecification desc : appExtensionActions) {
	    if (desc.getId().equals(id)) {
		return desc;
	    }
	}
	return null;
    }

    public ProtocolPluginSpecification searchIFDActionByURI(String uri) {
	for (ProtocolPluginSpecification desc : ifdActions) {
	    if (desc.getUri().equals(uri)) {
		return desc;
	    }
	}
	return null;
    }

    public ProtocolPluginSpecification searchSALActionByURI(String uri) {
	for (ProtocolPluginSpecification desc : salActions) {
	    if (desc.getUri().equals(uri)) {
		return desc;
	    }
	}
	return null;
    }
}
