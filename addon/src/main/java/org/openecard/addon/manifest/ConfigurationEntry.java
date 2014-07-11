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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@XmlTransient
@XmlSeeAlso({EnumEntry.class, EnumListEntry.class, ScalarEntry.class, ScalarListEntry.class})
public abstract class ConfigurationEntry {

    private String key;
    private final List<LocalizedString> localizedName = new ArrayList<>();
    private final List<LocalizedString> localizedDescription = new ArrayList<>();

    @XmlElement(name = "Key")
    public String getKey() {
	return key;
    }

    @XmlElement(name = "LocalizedName")
    public List<LocalizedString> getLocalizedName() {
	return localizedName;
    }

    @XmlElement(name = "LocalizedDescription")
    public List<LocalizedString> getLocalizedDescription() {
	return localizedDescription;
    }

    public void setKey(String key) {
	this.key = key;
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

}
