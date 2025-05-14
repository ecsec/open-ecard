/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.transport.dispatcher

import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.CIFProvider
import org.openecard.common.interfaces.CardRecognition
import org.openecard.common.interfaces.Dispatchable
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.Environment
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.interfaces.EventFilter
import org.openecard.common.interfaces.SalSelector
import org.openecard.gui.UserConsent
import org.openecard.ws.IFD
import org.openecard.ws.Management
import org.openecard.ws.SAL

/**
 * Test environment.
 * The getIFD method has the Dispatchable annotation.
 *
 * @author Tobias Wich
 */
open class TestEnv1 : Environment {
	@get:Dispatchable(interfaceClass = IFD::class)
	override var ifd: IFD? = null

	override var sal: SAL?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(sal) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override var eventDispatcher: EventDispatcher?
		get() =
			object : EventDispatcher {
				override fun start() {
				}

				override fun terminate() {
				}

				override fun add(cb: EventCallback): EventCallback = throw UnsupportedOperationException("Not supported yet.")

				override fun add(
					cb: EventCallback,
					vararg eventTypes: EventType,
				): EventCallback = throw UnsupportedOperationException("Not supported yet.")

				override fun add(
					cb: EventCallback,
					filter: EventFilter,
				): EventCallback = throw UnsupportedOperationException("Not supported yet.")

				override fun del(cb: EventCallback): EventCallback = throw UnsupportedOperationException("Not supported yet.")

				override fun notify(
					t: EventType,
					o: EventObject,
				) {
				}
			}
		set(manager) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override var dispatcher: Dispatcher?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(dispatcher) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override fun setGenericComponent(
		id: String,
		component: Any,
	): Unit = throw UnsupportedOperationException("Not supported yet.")

	override fun getGenericComponent(id: String): Any? = throw UnsupportedOperationException("Not supported yet.")

	override var management: Management?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(m) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override var recognition: CardRecognition?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(recognition) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override var cifProvider: CIFProvider?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(provider) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override var salSelector: SalSelector?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(salSelect) {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override fun addIfdCtx(ctx: ByteArray): Unit = throw UnsupportedOperationException("Not supported yet.")

	override fun removeIfdCtx(ctx: ByteArray): Unit = throw UnsupportedOperationException("Not supported yet.")

	override val ifdCtx: List<ByteArray>
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override var gui: UserConsent?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
		set(gui) {
			throw UnsupportedOperationException("Not supported yet.")
		}
}
