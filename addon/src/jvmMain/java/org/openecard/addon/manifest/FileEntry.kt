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
 */
package org.openecard.addon.manifest

import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType

/**
 *
 * @author Hans-Martin Haase
 */
@XmlRootElement(name = "FileEntry")
@XmlType(propOrder = ["key", "isRequiredBeforeAction", "fileType", "localizedName", "localizedDescription"])
open class FileEntry : ConfigurationEntry() {
	/**
	 * Set the FileEntry as required.
	 *
	 * @param required
	 */
	@XmlElement(name = "RequiredBeforeAction", required = true)
	var isRequiredBeforeAction: Boolean? = null

	@JvmField
	@XmlElement(name = "FileType", required = true)
	val fileType: String? = null
}
