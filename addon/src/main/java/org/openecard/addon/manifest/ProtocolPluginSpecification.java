/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@XmlRootElement(name = "ProtocolPluginSpecification")
@XmlType(propOrder = { "uri", "className", "loadOnStartup", "localizedName", "localizedDescription", "configDescription" })
@XmlAccessorType(XmlAccessType.FIELD)
public class ProtocolPluginSpecification {

    @XmlElement(name = "ClassName")
    private String className;
    @XmlElement(name = "LoadOnStartup", required = false, defaultValue = "false")
    private Boolean loadOnStartup;
    @XmlElement(name = "URI")
    private String uri;
    @XmlElement(name = "LocalizedName")
    private final List<LocalizedString> localizedName = new ArrayList<>();
    @XmlElement(name = "LocalizedDescription")
    private final List<LocalizedString> localizedDescription = new ArrayList<>();
    @XmlElement(name = "ConfigDescription")
    private Configuration configDescription;

    public String getClassName() {
	return className;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public Boolean isLoadOnStartup() {
	if (loadOnStartup == null) {
	    return false;
	}
	return loadOnStartup;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
	this.loadOnStartup = loadOnStartup;
    }

    public String getUri() {
	return uri;
    }

    public void setUri(String uri) {
	this.uri = uri;
    }

    public List<LocalizedString> getLocalizedName() {
	return localizedName;
    }

    public List<LocalizedString> getLocalizedDescription() {
	return localizedDescription;
    }

    public Configuration getConfigDescription() {
	return configDescription;
    }

    public void setConfigDescription(Configuration configDescription) {
	this.configDescription = configDescription;
    }

    public String getLocalizedName(String languageCode) {
	String fallback = "";
	for (LocalizedString s : localizedName) {
	    if (s.getLang().equalsIgnoreCase(languageCode)) {
		return s.getValue();
	    } else if (s.getLang().equalsIgnoreCase("EN")) {
		fallback = s.getValue();
	    }
	}
	return fallback;
    }

    public String getLocalizedDescription(String languageCode) {
	String fallback = "";
	for (LocalizedString s : localizedDescription) {
	    if (s.getLang().equalsIgnoreCase(languageCode)) {
		return s.getValue();
	    } else if (s.getLang().equalsIgnoreCase("EN")) {
		fallback = s.getValue();
	    }
	}
	return fallback;
    }
}
