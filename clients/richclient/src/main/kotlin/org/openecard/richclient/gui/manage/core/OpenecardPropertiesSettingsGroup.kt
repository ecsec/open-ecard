/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.richclient.gui.manage.core

import org.openecard.common.OpenecardProperties
import org.openecard.richclient.AddonPropertiesException
import org.openecard.richclient.gui.manage.SettingsFactory
import org.openecard.richclient.gui.manage.SettingsGroup
import java.io.IOException

/**
 * Settings group taking its values from the global OpenecardProperties instance.
 *
 * @author Tobias Wich
 */
open class OpenecardPropertiesSettingsGroup(
	title: String?,
) : SettingsGroup(title, SettingsFactory.getInstance()) {
	@Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
	override fun saveProperties() {
		super.saveProperties()
		// reload global Open eCard properties
		OpenecardProperties.load()
	}
}
