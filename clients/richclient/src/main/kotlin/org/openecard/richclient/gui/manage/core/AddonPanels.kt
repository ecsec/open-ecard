/****************************************************************************
 * Copyright (C) 2024-2025 ecsec GmbH.
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

import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.i18n.I18N
import org.openecard.richclient.gui.manage.AddonPanel
import java.awt.Image
import java.io.IOException
import javax.swing.ImageIcon

object AddonPanelBuilder {
	fun createConnectionSettingsAddon() =
		AddonPanel(
			ConnectionsSettingsPanel(),
			I18N.strings.addon_list_core_connection.localized(),
			null,
			loadImage("images/network-wired.png"),
		)

	fun createGeneralSettingsAddon() =
		AddonPanel(
			GeneralSettingsPanel(),
			I18N.strings.addon_list_core_general.localized(),
			null,
			loadImage("images/general.png"),
		)

	fun createLogSettingsAddon() =
		AddonPanel(
			LogSettingsPanel(),
			I18N.strings.addon_list_core_logging.localized(),
			null,
			loadImage("images/logging.png"),
		)

	fun createMiddlewareSelectionAddon() =
		AddonPanel(
			MiddlewareSelectionPanel(),
			I18N.strings.addon_list_core_middleware.localized(),
			null,
			null,
		)

	private fun loadImage(fName: String): Image? {
		try {
			val imageData =
				resolveResourceAsStream(
					javaClass,
					fName,
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
