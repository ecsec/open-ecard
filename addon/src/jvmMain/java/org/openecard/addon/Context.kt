/****************************************************************************
 * Copyright (C) 2013-2019 ecsec GmbH.
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
package org.openecard.addon

import org.openecard.addon.manifest.AddonSpecification
import org.openecard.addon.sal.SalStateView
import org.openecard.common.interfaces.CardRecognition
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.Environment
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.ViewController

/**
 * This class implements a context object used for the exchange of information with addons.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class Context(
	/**
	 * Get the AddonManager of this Context.
	 *
	 * @return The [AddonManager] of this context.
	 */
	@JvmField val manager: AddonManager,
	private val env: Environment,
	spec: AddonSpecification,
	/**
	 * Get the ViewController object of this Context.
	 *
	 * @return The [ViewController] of this Context.
	 */
	@JvmField val viewController: ViewController,
	@JvmField val salStateView: SalStateView,
) {
	/**
	 * Get the AddonProperties of this Context.
	 *
	 * @return The [AddonProperties] of this Context.
	 */
	val addonProperties: AddonProperties = AddonProperties(spec)

	/**
	 * Get the ID of this Context object.
	 *
	 * @return The ID of the Context object.
	 */
	val id: String = spec.getId()

	/**
	 * Get the UserConsent of this Context.
	 *
	 * @return The [UserConsent] of this Context.

	 * Sets the UserConsent of this Context.
	 *
	 * @param uConsent The [UserConsent] to set.
	 */
	@JvmField
	var userConsent: UserConsent? = null

	/**
	 * Get the CardRecognition implementation of this Context.
	 *
	 * @return The [CardRecognition] implementation of this Context.
	 */
	var recognition: CardRecognition? = null
		private set

	/**
	 * Get the EventHandler of this Context.
	 *
	 * @return The [EventHandler] of this Context.
	 */
	var eventHandler: EventHandler? = null
		private set

	/**
	 * Sets the CardRecognition implementation of this Context.
	 *
	 * @param cardRec The [CardRecognition] implementation to set.
	 */
	fun setCardRecognition(cardRec: CardRecognition?) {
		recognition = cardRec
	}

	/**
	 * Sets the EventHandler of for this Context.
	 *
	 * @param eventHandler The [EventHandler] to set.
	 */
	fun setEventHandle(eventHandler: EventHandler?) {
		this.eventHandler = eventHandler
	}

	val dispatcher: Dispatcher
		/**
		 * Get the Dispatcher of this Context.
		 *
		 * @return The [Dispatcher] of this Context.
		 */
		get() = env.dispatcher!!

	val ifdCtx: MutableList<ByteArray>
		get() = env.ifdCtx.toMutableList()

	val eventDispatcher: EventDispatcher
		/**
		 * Get the EventManager of this Context.
		 *
		 * @return The [org.openecard.common.event.EventDispatcherImpl] of this Context.
		 */
		get() = env.eventDispatcher!!
}
