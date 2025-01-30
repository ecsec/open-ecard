/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

import org.openecard.common.I18n
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.richclient.gui.manage.AddonPanel
import java.awt.Image
import java.io.IOException
import javax.swing.ImageIcon

object AddonPanelBuilder {
	private val lang: I18n = I18n.getTranslation("addon")

	fun createConnectionSettingsAddon() = AddonPanel(
		ConnectionsSettingsPanel(),
		lang.translationForKey("addon.list.core.connection"),
		null,
		loadImage("images/network-wired.png"),
	)

	fun createGeneralSettingsAddon() = AddonPanel(
		GeneralSettingsPanel(),
		lang.translationForKey("addon.list.core.general"),
		null,
		loadImage("images/general.png"),
	)

	fun createLogSettingsAddon() = AddonPanel(
		LogSettingsPanel(),
		lang.translationForKey("addon.list.core.logging"),
		null,
		loadImage("images/logging.png"),
	)

	fun createMiddlewareSelectionAddon() = AddonPanel(
		MiddlewareSelectionPanel(),
		lang.translationForKey("addon.list.core.middleware"),
		null,
		null,
	)

	private fun loadImage(fName: String): Image? {
		try {
			val imageData = resolveResourceAsStream(
				javaClass, fName
			)
			return imageData?.let {
				val icon: ImageIcon = ImageIcon(imageData.readAllBytes())
				icon.image
			}
		} catch (ex: IOException) {
			// ignore and let the default decide
			return null
		}
	}
}
