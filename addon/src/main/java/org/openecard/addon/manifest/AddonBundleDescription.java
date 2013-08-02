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
@XmlRootElement(name = "AddonBundleDescription")
@XmlType(propOrder = { "id", "version", "license", "localizedName", "localizedDescription", "about", "logo",
	"configDescription", "bindingActions", "applicationActions", "ifdActions", "salActions" })
public class AddonBundleDescription {

    private static final Logger logger = LoggerFactory.getLogger(AddonBundleDescription.class);

    private String id;
    private String logoFile;
    private byte[] logoBytes;
    private String version;
    private String license;
    private Configuration configDescription;
    private final List<LocalizedString> localizedName = new ArrayList<LocalizedString>();
    private final List<LocalizedString> localizedDescription = new ArrayList<LocalizedString>();
    private final List<LocalizedString> about = new ArrayList<LocalizedString>();
    private final ArrayList<AppExtensionActionDescription> appExtensionActions = new ArrayList<AppExtensionActionDescription>();
    private final ArrayList<AppPluginActionDescription> appPluginActions = new ArrayList<AppPluginActionDescription>();
    private final ArrayList<ProtocolPluginDescription> ifdActions = new ArrayList<ProtocolPluginDescription>();
    private final ArrayList<ProtocolPluginDescription> salActions = new ArrayList<ProtocolPluginDescription>();

    @XmlElement(name = "ID")
    public String getId() {
	return id;
    }

    @XmlElement(name = "LocalizedName")
    public List<LocalizedString> getLocalizedName() {
	return localizedName;
    }

    public String getLocalizedName(String languageCode) {
	//TODO implement
	throw new UnsupportedOperationException("Not yet implemented.");
    }

    @XmlElement(name = "LocalizedDescription")
    public List<LocalizedString> getLocalizedDescription() {
	return localizedDescription;
    }

    public String getLocalizedDescription(String languageCode) {
	//TODO implement
	throw new UnsupportedOperationException("Not yet implemented.");
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
    @XmlElement(name = "AppPluginActionDescription")
    public ArrayList<AppPluginActionDescription> getBindingActions() {
	return appPluginActions;
    }

    @XmlElementWrapper(name = "IFDActions")
    @XmlElement(name = "ProtocolPluginDescription")
    public ArrayList<ProtocolPluginDescription> getIfdActions() {
	return ifdActions;
    }

    @XmlElementWrapper(name = "ApplicationActions")
    @XmlElement(name = "AppExtensionActionDescription")
    public ArrayList<AppExtensionActionDescription> getApplicationActions() {
	return appExtensionActions;
    }

    @XmlElementWrapper(name = "SALActions")
    @XmlElement(name = "ProtocolPluginDescription")
    public ArrayList<ProtocolPluginDescription> getSalActions() {
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
		InputStream logoStream = FileUtils.resolveResourceAsStream(AddonBundleDescription.class, logoFile);
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

    public AppPluginActionDescription searchByResourceName(String resourceName) {
	for (AppPluginActionDescription desc : appPluginActions) {
	    if (resourceName.equals(desc.getResourceName())) {
		return desc;
	    }
	}
	return null;
    }

    public AppExtensionActionDescription searchByActionId(String id) {
	for (AppExtensionActionDescription desc : appExtensionActions) {
	    if (desc.getId().equals(id)) {
		return desc;
	    }
	}
	return null;
    }

    public ProtocolPluginDescription searchIFDActionByURI(String uri) {
	for (ProtocolPluginDescription desc : ifdActions) {
	    if (desc.getUri().equals(uri)) {
		return desc;
	    }
	}
	return null;
    }

    public ProtocolPluginDescription searchSALActionByURI(String uri) {
	for (ProtocolPluginDescription desc : salActions) {
	    if (desc.getUri().equals(uri)) {
		return desc;
	    }
	}
	return null;
    }
}
