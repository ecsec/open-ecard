/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
package org.openecard.common.event

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.interfaces.EventFilter
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * EventDispatcherImpl class distributing the events to all registered listeners.
 * Filtering is applied as requested at registration of the listener.
 *
 * @author Tobias Wich
 * @author René Lottes
 */

private val LOG = KotlinLogging.logger { }

class EventDispatcherImpl : EventDispatcher {
	private val threadFactory: ThreadFactory =
		object : ThreadFactory {
			private val num = AtomicInteger(0)
			private val group = ThreadGroup("Event Dispatcher")

			@Suppress("DefaultLocale")
			override fun newThread(r: Runnable): Thread {
				val name = String.format("Dispatcher Event %d", num.getAndIncrement())
				val t = Thread(group, r, name)
				t.isDaemon = false
				return t
			}
		}

	private var eventFilter: ConcurrentHashMap<EventCallback, ArrayList<EventFilter>>? = null
	private var threadPools: HashMap<EventCallback, ExecutorService>? = null
	private var initialized = false

	@Synchronized
	override fun start() {
		this.eventFilter = ConcurrentHashMap()
		this.threadPools = HashMap()

		this.initialized = true
	}

	@Synchronized
	override fun terminate() {
		if (initialized) {
			// remove everything and thereby shutdown thread pools
			for (entry in Collections.list(
				eventFilter!!.keys(),
			)) {
				del(entry)
			}

			initialized = false
			eventFilter = null
			threadPools = null
		}
	}

	override fun add(cb: EventCallback): EventCallback {
		add(cb, EventTypeFilter())
		return cb
	}

	override fun add(
		cb: EventCallback,
		vararg eventTypes: EventType,
	): EventCallback {
		add(cb, EventTypeFilter(*eventTypes))
		return cb
	}

	@Synchronized
	override fun add(
		cb: EventCallback,
		filter: EventFilter,
	): EventCallback {
		if (initialized) {
			if (!eventFilter!!.containsKey(cb)) {
				eventFilter!![cb] = ArrayList()
			}
			eventFilter!![cb]!!.add(filter)
			// create an executor service for each callback
			createExecutorService(cb)
		}
		return cb
	}

	@Synchronized
	override fun del(cb: EventCallback): EventCallback {
		if (initialized && eventFilter!!.containsKey(cb)) {
			eventFilter!!.remove(cb)
			val exec = threadPools!!.remove(cb)
			exec!!.shutdownNow()
		}
		return cb
	}

	@Synchronized
	override fun notify(
		t: EventType,
		o: EventObject,
	) {
		if (initialized) {
			for ((cb, value) in eventFilter!!) {
				for (filter in value) {
					// when there is a filter match, then fire out the event (only once!)
					if (filter.matches(t, o)) {
						LOG.debug { "${"Sending event notification {} to EventCallback {}."} $t $cb" }
						val executor = threadPools!![cb]
						executor!!.execute { cb.signalEvent(t, o) }
						break
					}
				}
			}
		}
	}

	private fun createExecutorService(cb: EventCallback) {
		val executor = Executors.newSingleThreadExecutor(threadFactory)
		threadPools!![cb] = executor
	}
}
