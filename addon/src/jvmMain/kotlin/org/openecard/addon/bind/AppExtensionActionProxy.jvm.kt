/****************************************************************************
 * Copyright (C) 2013-2024 ecsec GmbH.
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

package org.openecard.addon.bind

import org.openecard.addon.AbstractFactory
import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context

/**
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
class AppExtensionActionProxy(
	implClass: String,
	classLoader: ClassLoader,
) : AbstractFactory<AppExtensionAction>(implClass, classLoader),
	AppExtensionAction {
	private var c: AppExtensionAction? = null

	@Throws(AppExtensionException::class)
	override fun execute() {
		c?.execute() ?: throw IllegalStateException("AppExtensionAction not initialized")
	}

	@Throws(ActionInitializationException::class)
	override fun init(ctx: Context) {
		c = loadInstance(ctx, AppExtensionAction::class.java)
	}

	override fun destroy(force: Boolean) {
		c?.destroy(force)
	}
}
