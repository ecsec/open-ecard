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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import org.openecard.addon.utils.LocalizedStringExtractor;


/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({EnumEntry.class, EnumListEntry.class, ScalarEntry.class, ScalarListEntry.class})
public abstract class ConfigurationEntry {

    @XmlElement(name = "Key")
    private String key;
    @XmlElement(name = "LocalizedName")
    private final List<LocalizedString> localizedName = new ArrayList<>();
    @XmlElement(name = "LocalizedDescription")
    private final List<LocalizedString> localizedDescription = new ArrayList<>();

    public String getKey() {
	return key;
    }

    public List<LocalizedString> getLocalizedName() {
	return localizedName;
    }

    public List<LocalizedString> getLocalizedDescription() {
	return localizedDescription;
    }

    public void setKey(String key) {
	this.key = key;
    }

    public String getLocalizedName(String languageCode) {
	return LocalizedStringExtractor.getLocalizedString(localizedName, languageCode);
    }

    public String getLocalizedDescription(String languageCode) {
	return LocalizedStringExtractor.getLocalizedString(localizedDescription, languageCode);
    }

}
