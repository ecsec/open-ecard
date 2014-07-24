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

package org.openecard.addon.manifest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
@XmlRootElement(name = "FileEntry")
@XmlType(propOrder = { "key", "requiredBeforeAction", "fileType", "localizedName", "localizedDescription" })
public class FileEntry extends ConfigurationEntry {

    @XmlElement(name = "RequiredBeforeAction", required = true)
    private Boolean requiredBeforeAction;

    @XmlElement(name = "FileType", required = true)
    private String fileType;

    /**
     * Set the FileEntry as required.
     *
     * @param required
     */
    public void setRequiredBeforeAction(boolean required) {
	requiredBeforeAction = required;
    }

    public boolean isRequiredBeforeAction() {
	return requiredBeforeAction;
    }

    public String getFileType() {
	return fileType;
    }
}
