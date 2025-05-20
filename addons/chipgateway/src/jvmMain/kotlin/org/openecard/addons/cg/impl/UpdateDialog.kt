/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import dev.icerock.moko.resources.format
import jdk.internal.org.jline.utils.Colors.s
import org.openecard.common.AppVersion.name
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.Hyperlink
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.i18n.I18N
import java.net.MalformedURLException

/**
 *
 * @author Tobias Wich
 */
class UpdateDialog(
	private val gui: UserConsent,
	private val dlUrl: String,
	private val updateRequired: Boolean,
) {
	private val ucDesc = createDialog()

	@Throws(MalformedURLException::class)
	private fun createDialog(): UserConsentDescription {
		val uc =
			UserConsentDescription(
				I18N.strings.chipgateway_dialog_dl_title.localized(),
				"update_dialog",
			)

		val t =
			if (updateRequired) {
				Text(
					I18N.strings.chipgateway_dialog_dl_text_required
						.format(name)
						.localized(),
				)
			} else {
				Text(
					I18N.strings.chipgateway_dialog_dl_text_optional
						.format(name)
						.localized(),
				)
			}
		val link = Hyperlink().apply { setHref(dlUrl) }

		val s =
			Step(
				I18N.strings.chipgateway_dialog_dl_title.localized(),
			).apply {
				inputInfoUnits.add(t)
				inputInfoUnits.add(
					Text(
						I18N.strings.chipgateway_dialog_dl_text_instructions.localized(),
					),
				)
				inputInfoUnits.add(link)
			}

		uc.steps.add(s)
		return uc
	}

	fun display() {
		val nav = gui.obtainNavigator(ucDesc)
		val ee = ExecutionEngine(nav)
		ee.process()
	}
}
