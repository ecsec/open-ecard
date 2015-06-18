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
import org.openecard.addon.utils.LocalizedStringExtractor;


/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
@XmlRootElement(name = "AppExtensionSpecification")
@XmlType(propOrder = { "id", "className", "loadOnStartup", "localizedName", "localizedDescription", "configDescription" })
@XmlAccessorType(XmlAccessType.FIELD)
public class AppExtensionSpecification {

    @XmlElement(name = "ID", required = true)
    private String id;
    @XmlElement(name = "ClassName", required = true)
    private String className;
    @XmlElement(name = "LoadOnStartup", required = false, defaultValue = "false")
    private Boolean loadOnStartup;
    @XmlElement(name = "LocalizedName", required = false)
    private final List<LocalizedString> localizedName = new ArrayList<>();
    @XmlElement(name = "LocalizedDescription", required = false)
    private final List<LocalizedString> localizedDescription = new ArrayList<>();
    @XmlElement(name = "ConfigDescription", required = true)
    private Configuration configDescription;

    public String getId() {
	return id;
    }

    public String getClassName() {
	return className;
    }

    public Boolean isLoadOnStartup() {
	if (loadOnStartup == null) {
	    return false;
	}
	return loadOnStartup;
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

    public void setId(String id) {
	this.id = id;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public void setLoadOnStartup(boolean loadOnStartup) {
	this.loadOnStartup = loadOnStartup;
    }

    public void setConfigDescription(Configuration configDescription) {
	this.configDescription = configDescription;
    }

    public String getLocalizedName(String languageCode) {
	return LocalizedStringExtractor.getLocalizedString(localizedName, languageCode);
    }

    public String getLocalizedDescription(String languageCode) {
	return LocalizedStringExtractor.getLocalizedString(localizedDescription, languageCode);
    }

}
