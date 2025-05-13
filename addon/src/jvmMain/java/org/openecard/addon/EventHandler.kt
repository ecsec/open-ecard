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
package org.openecard.addon

import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.EventCallback
import org.openecard.ws.schema.StatusChange
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 *
 * @author Johannes Schmölz
 * @author Benedikt Biallowons
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class EventHandler : EventCallback {
	private val eventQueues: MutableMap<String?, LinkedBlockingQueue<StatusChange?>?>
	private val timers: MutableMap<String?, ReschedulableTimer?>

	/**
	 * Create a new EventHandler.
	 */
	init {
		eventQueues = HashMap<String?, LinkedBlockingQueue<StatusChange?>?>()
		timers = HashMap<String?, ReschedulableTimer?>()
	}

	/**
	 *
	 * @param session
	 * @return a StatusChange containing the new status, or null if no eventQueue for the given session exists or if
	 * interrupted
	 */
	fun next(session: String?): StatusChange? {
		// String session = statusChangeRequest.getSessionIdentifier();
		var handle: StatusChange? = null
		val queue = eventQueues.get(session)
		if (queue == null) {
			LOG.error("No queue found for session {}", session)
			return null
		}
		do {
			try {
				timers.get(session)!!.reschedule(deleteDelay.toLong())
				handle = eventQueues.get(session)!!.poll(30, TimeUnit.SECONDS)
				LOG.debug("WaitForChange event pulled from event queue.")
			} catch (ex: InterruptedException) {
				return null
			}
		} while (handle == null)
		return handle
	}

	override fun signalEvent(
		eventType: EventType,
		eventData: EventObject,
	) {
		val connectionHandle = eventData.handle

		for (entry in eventQueues.entries) {
			try {
				val queue: LinkedBlockingQueue<StatusChange?> = entry.value!!
				val statusChange = StatusChange()
				statusChange.setAction(eventType.eventTypeIdentifier)
				statusChange.setConnectionHandle(connectionHandle)
				queue.put(statusChange)
			} catch (ignore: InterruptedException) {
			}
		}
	}

	/**
	 * Adds a new EventQueue for a given session.
	 *
	 * @param sessionIdentifier session identifier
	 */
	fun addQueue(sessionIdentifier: String?) {
		if (eventQueues.get(sessionIdentifier) == null) {
			eventQueues.put(sessionIdentifier, LinkedBlockingQueue<StatusChange?>())
			val timer = ReschedulableTimer()
			timer.schedule(this.DeleteTask(sessionIdentifier), deleteDelay.toLong())
			timers.put(sessionIdentifier, timer)
		} else {
			timers.get(sessionIdentifier)!!.reschedule(deleteDelay.toLong())
		}
	}

	private inner class DeleteTask(
		private val sessionIdentifier: String?,
	) : Runnable {
		override fun run() {
			eventQueues.remove(sessionIdentifier)
			timers.remove(sessionIdentifier)
		}
	}

	companion object {
		private val LOG: Logger = LoggerFactory.getLogger(EventHandler::class.java)

		// after this delay of inactivity an event queue (and it's timer) will be deleted
		private val deleteDelay = 60 * 1000
	}
}
