/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.gui.definition

/**
 * Definition of a user consent.
 * This class is the parent element for steps.
 *
 * @author Tobias Wich
 */
open class UserConsentDescription

	/**
	 * Creates a user consent description with the given title and dialog type.
	 *
	 * @param title The title of the user consent.
	 * @param dialogType The dialog type of the user consent. This must not be null.
	 */
	@JvmOverloads
	constructor(
		/**
		 * Sets the title of this instance.
		 *
		 * @param title The title of this instance.
		 */
		var title: String,
		/**
		 * Sets the dialog type of this instance.
		 * The dialog type is used to request specific behaviour of the user consent. For example an EAC user consent may
		 * need special layouting, so the a dialog type URI http://openecard.org/uc/eac may be defined and implemented by
		 * the user consent implementation.
		 *
		 * @param dialogType The dialog type of this instance. The empty string may be used to reset the user consent to the
		 * default behaviour.
		 */
		var dialogType: String = "",
	) {
		val steps: MutableList<Step> by lazy { mutableListOf() }
	}
