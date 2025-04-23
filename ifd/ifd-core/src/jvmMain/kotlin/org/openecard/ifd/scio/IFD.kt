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
 ***************************************************************************/

package org.openecard.ifd.scio

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.common.ECardConstants
import org.openecard.common.ThreadTerminateException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.makeResult
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.makeResultOK
import org.openecard.common.WSHelper.makeResultUnknownError
import org.openecard.common.WSHelper.makeResultUnknownIFDError
import org.openecard.common.event.EventType
import org.openecard.common.event.IfdEventObject
import org.openecard.common.ifd.PACECapabilities
import org.openecard.common.ifd.ProtocolFactory
import org.openecard.common.ifd.anytype.PACEInputType
import org.openecard.common.ifd.scio.*
import org.openecard.common.interfaces.Environment
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked
import org.openecard.common.interfaces.Publish
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.HandlerBuilder
import org.openecard.common.util.ValueGenerators.genBase64Session
import org.openecard.ifd.event.IfdEventManager
import org.openecard.ifd.scio.reader.*
import org.openecard.ifd.scio.wrapper.ChannelManager
import org.openecard.ifd.scio.wrapper.IFDTerminalFactory
import org.openecard.ifd.scio.wrapper.NoSuchChannel
import org.openecard.ifd.scio.wrapper.TerminalInfo
import org.openecard.ws.IFD
import org.openecard.ws.common.GenericFactoryException
import java.math.BigInteger
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

private val LOG = KotlinLogging.logger { }

/**
 * SCIO implementation of the IFD interface.
 *
 * @author Tobias Wich
 */
class IFD : IFD {
	private var ctxHandle: ByteArray? = null

	// private SCWrapper scwrapper;
	private var cm: ChannelManager? = null

	private var env: Environment? = null

	private val protocolFactories = ProtocolFactories()
	private var evManager: IfdEventManager? = null

	private var numClients: AtomicInteger? = null
	private var threadPool: ExecutorService? = null
	private var asyncWaitThreads: ConcurrentSkipListMap<String, Future<List<IFDStatusType>>>? = null
	private var syncWaitThread: Future<List<IFDStatusType>>? = null

	@get:Throws(IFDException::class)
	var terminalFactoryBuilder: IFDTerminalFactory? = null
		get() {
			if (field == null) {
				field = IFDTerminalFactory.Companion.configBackedInstance()
			}
			return field
		}

	@get:Throws(IFDException::class)
	private val termFactory: TerminalFactory
		get() {
			val factoryBuilder = this.terminalFactoryBuilder!!
			val currentTermFactory =
				try {
					factoryBuilder.instance
				} catch (ex: GenericFactoryException) {
					throw IFDException(ex)
				}
			return currentTermFactory
		}

	@Synchronized
	protected fun removeAsnycTerminal(session: String) {
		if (asyncWaitThreads != null) { // be sure the list still exists
			asyncWaitThreads!!.remove(session)
		}
	}

	private fun hasContext(): Boolean {
		val hasContext = ctxHandle != null
		return hasContext
	}

	fun setEnvironment(env: Environment) {
		this.env = env
	}

	fun addProtocol(
		proto: String,
		factory: ProtocolFactory,
	): Boolean = protocolFactories.add(proto, factory)

	@Synchronized
	override fun establishContext(parameters: EstablishContext?): EstablishContextResponse {
		val response: EstablishContextResponse
		try {
			// on first call, create a new unique handle
			if (ctxHandle == null) {
				val currentTermFactory = this.termFactory
				cm = ChannelManager(currentTermFactory)
				ctxHandle = ChannelManager.Companion.createCtxHandle()
				env!!.addIfdCtx(ctxHandle!!)
				numClients = AtomicInteger(1)
				// TODO: add custom ThreadFactory to control the thread name
				threadPool =
					Executors.newCachedThreadPool(
						object : ThreadFactory {
							private val num = AtomicInteger(0)
							private val group = ThreadGroup("IFD Wait")

							override fun newThread(r: Runnable): Thread {
								val name = "SCIO Watcher ${num.getAndIncrement()}"
								val t = Thread(group, r, name)
								t.setDaemon(false)
								return t
							}
						},
					)
				asyncWaitThreads = ConcurrentSkipListMap()
				evManager = IfdEventManager(env!!, ctxHandle!!)
				evManager!!.initialize()
			} else {
				// on second or further calls, increment usage counter
				numClients!!.incrementAndGet()
			}

			// prepare response
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.EstablishContextResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultOK(),
				)
			response.setContextHandle(ctxHandle)
			return response
		} catch (ex: IFDException) {
			LOG.warn(ex) { ex.message }
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.EstablishContextResponse::class.java,
				ex.result,
			)
		}
	}

	@Synchronized
	override fun releaseContext(parameters: ReleaseContext): ReleaseContextResponse {
		val response: ReleaseContextResponse
		if (ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
			if (numClients!!.decrementAndGet() == 0) { // last client detaches
				env!!.removeIfdCtx(ctxHandle!!)
				ctxHandle = null
				numClients = null
				// terminate thread pool
				threadPool!!.shutdownNow() // wait for threads to die and block new requests
				// just assume it worked ... and don't wait
				threadPool = null
				asyncWaitThreads = null
			}
			evManager!!.terminate()

			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultOK(),
				)
			return response
		} else {
			val msg = "Invalid context handle specified."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse::class.java,
					r,
				)
			return response
		}
	}

	override fun prepareDevices(parameters: PrepareDevices): PrepareDevicesResponse {
		val wasPrepared: Boolean
		try {
			wasPrepared = cm!!.prepareDevices()
		} catch (ex: SCIOException) {
			val minorError =
				when (ex.code) {
					SCIOErrorCode.SCARD_W_CANCELLED_BY_USER -> ECardConstants.Minor.IFD.CANCELLATION_BY_USER
					SCIOErrorCode.SCARD_E_TIMEOUT -> ECardConstants.Minor.IFD.Terminal.WAIT_FOR_DEVICE_TIMEOUT
					else -> ECardConstants.Minor.IFD.Terminal.PREPARE_DEVICES_ERROR
				}
			val r = makeResultError(minorError, ex.message)
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse::class.java,
				r,
			)
		}

		if (wasPrepared) {
			val handle =
				HandlerBuilder
					.create()
					.setContextHandle(parameters.getContextHandle())
					.buildConnectionHandle()

			env!!.eventDispatcher!!.notify(EventType.PREPARE_DEVICES, IfdEventObject(handle))
		}

		return WSHelper.makeResponse(
			iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse::class.java,
			org.openecard.common.WSHelper
				.makeResultOK(),
		)
	}

	override fun powerDownDevices(parameters: PowerDownDevices): PowerDownDevicesResponse {
		val wasPoweredDown = cm!!.powerDownDevices()

		if (wasPoweredDown) {
			val handle =
				HandlerBuilder
					.create()
					.setContextHandle(parameters.getContextHandle())
					.buildConnectionHandle()

			env!!.eventDispatcher!!.notify(EventType.POWER_DOWN_DEVICES, IfdEventObject(handle))
		}

		return WSHelper.makeResponse(
			iso.std.iso_iec._24727.tech.schema.PowerDownDevicesResponse::class.java,
			org.openecard.common.WSHelper
				.makeResultOK(),
		)
	}

	override fun listIFDs(parameters: ListIFDs): ListIFDsResponse {
		var response: ListIFDsResponse
		if (!ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
			val msg = "Invalid context handle specified."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ListIFDsResponse::class.java,
					r,
				)
			return response
		} else {
			try {
				val terminals: List<SCIOTerminal> = cm!!.terminals.list()
				val ifds = ArrayList<String>(terminals.size)
				for (next in terminals) {
					ifds.add(next.name)
				}
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.ListIFDsResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultOK(),
					)
				response.getIFDName().addAll(ifds)
				return response
			} catch (ex: SCIOException) {
				LOG.warn(ex) { "${ex.message}" }
				val r = makeResultUnknownIFDError(ex.message)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.ListIFDsResponse::class.java,
						r,
					)
				return response
			}
		}
	}

	override fun getIFDCapabilities(parameters: GetIFDCapabilities): GetIFDCapabilitiesResponse {
		var response: GetIFDCapabilitiesResponse

		// you thought of a different IFD obviously
		if (!ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
			val msg = "Invalid context handle specified."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse::class.java,
					r,
				)
			return response
		}

		try {
			val ifdName = parameters.getIFDName()
			val info =
				try {
					val channel = cm!!.openMasterChannel(ifdName)
					TerminalInfo(cm!!, channel)
				} catch (ex: NoSuchTerminal) {
					// continue without a channel
					val term = cm!!.terminals.getTerminal(ifdName)
					TerminalInfo(cm!!, term)
				}

			val cap = IFDCapabilitiesType()

			// slot capability
			val slotCap = info.slotCapability
			cap.getSlotCapability().add(slotCap)
			// ask protocol factory which types it supports
			val protocols = slotCap.getProtocol()
			for (proto in protocolFactories.protocols()) {
				if (!protocols.contains(proto)) {
					protocols.add(proto)
				}
			}
			// add built in protocols stuff
			// TODO: PIN Compare should be a part of establishChannel and thus just appear in the software protocol list
			if (!protocols.contains(ECardConstants.Protocol.PIN_COMPARE)) {
				protocols.add(ECardConstants.Protocol.PIN_COMPARE)
			}

			// display capability
			val dispCap = info.displayCapability
			if (dispCap != null) {
				cap.getDisplayCapability().add(dispCap)
			}

			// keypad capability
			val keyCap = info.keypadCapability
			if (keyCap != null) {
				cap.getKeyPadCapability().add(keyCap)
			}

			// biosensor capability
			val bioCap = info.biosensorCapability
			if (bioCap != null) {
				cap.getBioSensorCapability().add(bioCap)
			}

			// acoustic and optical elements
			cap.isOpticalSignalUnit = info.isOpticalSignal
			cap.isAcousticSignalUnit = info.isAcousticSignal

			// prepare response
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultOK(),
				)
			response.setIFDCapabilities(cap)
			return response
		} catch (ex: NullPointerException) {
			val msg = String.format("Requested terminal not found.")
			LOG.warn(ex) { msg }
			val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse::class.java,
					r,
				)
			return response
		} catch (ex: NoSuchTerminal) {
			val msg = String.format("Requested terminal not found.")
			LOG.warn(ex) { msg }
			val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse::class.java,
					r,
				)
			return response
		} catch (ex: SCIOException) {
			val msg = String.format("Failed to request status from terminal.")
			// use debug when card has been removed, as this happens all the time
			val code = ex.code
			if (!(code == SCIOErrorCode.SCARD_E_NO_SMARTCARD || code == SCIOErrorCode.SCARD_W_REMOVED_CARD)) {
				LOG.warn(ex) { msg }
			} else {
				LOG.debug(ex) { msg }
			}
			val r = makeResultUnknownIFDError(msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse::class.java,
					r,
				)
			return response
		} catch (ex: InterruptedException) {
			val msg = String.format("Cancellation by user.")
			LOG.warn(ex) { msg }
			val r = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse::class.java,
					r,
				)
			return response
		}
	}

	override fun getStatus(parameters: GetStatus): GetStatusResponse {
		var response: GetStatusResponse

		// you thought of a different IFD obviously
		if (!ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
			val msg = "Invalid context handle specified."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetStatusResponse::class.java,
					r,
				)
			return response
		}

		// get specific ifd or all if no specific one is requested
		val ifds: MutableList<SCIOTerminal> = LinkedList<SCIOTerminal>()
		try {
			val requestedIfd = parameters.getIFDName()
			if (requestedIfd != null) {
				try {
					val t = cm!!.terminals.getTerminal(requestedIfd)
					ifds.add(t)
				} catch (ex: NoSuchTerminal) {
					val msg = "The requested IFD name does not exist."
					LOG.warn(ex) { msg }
					val minor = ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD
					val r = makeResult(ECardConstants.Major.ERROR, minor, msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.GetStatusResponse::class.java,
							r,
						)
					return response
				}
			} else {
				ifds.addAll(cm!!.terminals.list())
			}
		} catch (ex: SCIOException) {
			val msg = "Failed to get list with the terminals."
			LOG.warn(ex) { msg }
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.GetStatusResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultUnknownIFDError(msg),
				)
			return response
		}

		// request status for each ifd
		val status = ArrayList<IFDStatusType>(ifds.size)
		for (ifd in ifds) {
			var info =
				try {
					val channel = cm!!.openMasterChannel(ifd.name)
					TerminalInfo(cm!!, channel)
				} catch (ex: NoSuchTerminal) {
					// continue without a channel
					TerminalInfo(cm!!, ifd)
				} catch (ex: SCIOException) {
					TerminalInfo(cm!!, ifd)
				}
			try {
				val s = info.status
				status.add(s)
			} catch (ex: SCIOException) {
				if (ex.code != SCIOErrorCode.SCARD_W_UNPOWERED_CARD &&
					ex.code != SCIOErrorCode.SCARD_W_UNRESPONSIVE_CARD &&
					ex.code != SCIOErrorCode.SCARD_W_UNSUPPORTED_CARD &&
					ex.code != SCIOErrorCode.SCARD_E_PROTO_MISMATCH
				) {
					val msg = String.format("Failed to determine status of terminal '%s'.", ifd.name)
					LOG.warn(ex) { msg }
					val r = makeResultUnknownIFDError(msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.GetStatusResponse::class.java,
							r,
						)
					return response
				} else {
					// fall through if there is a card which can not be connected
					LOG.info(ex) { "Ignoring failed status request from terminal." }
				}
			}
		}

		// everything worked out well
		response =
			WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.GetStatusResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)
		response.getIFDStatus().addAll(status)
		return response
	}

	override fun wait(parameters: Wait): WaitResponse {
		var response: WaitResponse

		// you thought of a different IFD obviously
		if (!ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
			val msg = "Invalid context handle specified."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
					r,
				)
			return response
		}

		// get timeout value
		var timeout = parameters.getTimeOut()
		if (timeout == null) {
			timeout = BigInteger.valueOf(Long.Companion.MAX_VALUE)
		}
		if (timeout.signum() == -1 || timeout.signum() == 0) {
			val msg = "Invalid timeout value given, must be strictly positive."
			val r = makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
					r,
				)
			return response
		}
		var timeoutL: Long
		try {
			timeoutL = timeout.toDouble().toLong()
		} catch (ex: ArithmeticException) {
			LOG.warn { "Too big timeout value give, shortening to Long.MAX_VALUE." }
			timeoutL = Long.Companion.MAX_VALUE
		}

		var callback = parameters.getCallback()
		// callback is only useful with a protocol termination point
		if (callback != null && callback.getProtocolTerminationPoint() == null) {
			callback = null
		}

		// if callback, generate session id
		var sessionId: String? = null
		if (callback != null) {
			val newCallback = ChannelHandleType()
			newCallback.setBinding(callback.getBinding())
			newCallback.setPathSecurity(callback.getPathSecurity())
			newCallback.setProtocolTerminationPoint(callback.getProtocolTerminationPoint())
			sessionId = genBase64Session()
			newCallback.setSessionIdentifier(sessionId)
			callback = newCallback
		}

		try {
			val watcher = EventWatcher(cm!!, timeoutL, callback)
			val initialState = watcher.start()

			// get expected status or initial status for all if none specified
			var expectedState = parameters.getIFDStatus()
			if (expectedState.isEmpty()) {
				expectedState = initialState
			} else {
				for (s in expectedState) {
					// check that ifdname is present, needed for comparison
					if (s.getIFDName() == null) {
						val msg = "IFD in a request IFDStatus not known."
						val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
						response =
							WSHelper.makeResponse(
								iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
								r,
							)
						return response
					}
					// check that at least one slot entry is present
					if (s.getSlotStatus().isEmpty()) {
						// assume an empty one
						val slot = SlotStatusType()
						slot.isCardAvailable = false
						slot.setIndex(BigInteger.ZERO)
						s.getSlotStatus().add(slot)
					}
				}
			}
			watcher.setExpectedState(expectedState)

			// create the future and fire
			val future = FutureTask(watcher)
			if (watcher.isAsync) {
				// add future to async wait list
				asyncWaitThreads!!.put(sessionId!!, future)
				threadPool!!.execute(future) // finally run this darn thingy

				// prepare result with session id in it
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultOK(),
					)
				response.setSessionIdentifier(sessionId)
				return response
			} else {
				// run wait in a future so it can be easily interrupted
				syncWaitThread = future
				if (threadPool == null) {
					val msg = "Thread pool is null. Cannot execute future."
					LOG.error { msg }
					val r = makeResultUnknownError(msg)
					return WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
						r,
					)
				}
				threadPool!!.execute(future)

				// get results from the future
				val events = future.get()

				// prepare response
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultOK(),
					)
				response.getIFDEvent().addAll(events)
				return response
			}
		} catch (ex: SCIOException) {
			val msg = "Unknown SCIO error occurred during wait call."
			LOG.warn(ex) { msg }
			val r = makeResultUnknownIFDError(msg)
			if (ex.code == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
				r.setResultMinor(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)
			}
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
					r,
				)
			return response
		} catch (ex: ExecutionException) {
			// this is the exception from within the future
			val cause = ex.cause
			if (cause is SCIOException) {
				val msg = "Unknown SCIO error occurred during wait call."
				LOG.warn(cause) { msg }
				val r = makeResultUnknownIFDError(msg)
				if ((ex.cause as SCIOException).code == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
					r.setResultMinor(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)
				}
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
						r,
					)
			} else {
				val msg = "Unknown error during wait call."
				LOG.error(cause) { msg }
				val r = makeResultUnknownError(msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
						r,
					)
			}
			return response
		} catch (ex: InterruptedException) {
			val msg = "Wait interrupted by another thread."
			LOG.warn(ex) { msg }
			val r = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.WaitResponse::class.java,
					r,
				)
			Thread.currentThread().interrupt()
			return response
		}
	}

	override fun cancel(parameters: Cancel): CancelResponse {
		val response: CancelResponse

		// you thought of a different IFD obviously
		if (!ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
			val msg = "Invalid context handle specified."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.CancelResponse::class.java,
					r,
				)
			return response
		}

		val ifdName = parameters.getIFDName()
		val session = parameters.getSessionIdentifier()
		if (session != null) {
			// async wait
			val f = this.asyncWaitThreads!!.get(session)
			if (f != null) {
				f.cancel(true)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.CancelResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultOK(),
					)
			} else {
				val msg = "No matching Wait call exists for the given session."
				val r = makeResultError(ECardConstants.Minor.IFD.IO.CANCEL_NOT_POSSIBLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.CancelResponse::class.java,
						r,
					)
			}
		} else if (ifdName != null) {
			// TODO: kill only if request is specific to the named terminal
			// sync wait
			synchronized(this) {
				if (syncWaitThread != null) {
					syncWaitThread!!.cancel(true)
					syncWaitThread = null // not really needed but seems cleaner
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.CancelResponse::class.java,
							org.openecard.common.WSHelper
								.makeResultOK(),
						)
				} else {
					val msg = "No synchronous Wait to cancel."
					val r = makeResultError(ECardConstants.Minor.IFD.IO.CANCEL_NOT_POSSIBLE, msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.CancelResponse::class.java,
							r,
						)
				}
			}
		} else {
			// nothing to cancel
			val msg = "Invalid parameters given."
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.CancelResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultUnknownError(msg),
				)
		}

		return response
	}

	/**
	 * Note: the first byte of the command data is the control code.
	 */
	override fun controlIFD(parameters: ControlIFD): ControlIFDResponse {
		var response: ControlIFDResponse

		if (!hasContext()) {
			val msg = "Context not initialized."
			val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
					r,
				)
			return response
		}

		val handle = parameters.getSlotHandle()
		var command = parameters.getCommand()
		if (handle == null || command == null) {
			val msg = "Missing parameter."
			val r = makeResultError(ECardConstants.Minor.App.PARM_ERROR, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
					r,
				)
			return response
		}
		val ctrlCode = command[0]
		command = command.copyOfRange(1, command.size)

		try {
			val ch = cm!!.getSlaveChannel(handle)
			val info = TerminalInfo(cm!!, ch)
			val featureCode = info.featureCodes[ctrlCode.toInt()]
			// see if the terminal can deal with that
			if (featureCode != null) {
				val resultCommand = ch.transmitControlCommand(featureCode, command)

				// evaluate result
				val result = evaluateControlIFDRAPDU(resultCommand)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
						result,
					)
				response.setResponse(resultCommand)
				return response
			} else {
				val msg = "The terminal is not capable of performing the requested action."
				val r = makeResultUnknownIFDError(msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
						r,
					)
				return response
			}
		} catch (ex: NoSuchChannel) {
			val msg = "The card or the terminal is not available anymore."
			val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
					r,
				)
			LOG.warn(ex) { msg }
			return response
		} catch (ex: IllegalStateException) {
			val msg = "The card or the terminal is not available anymore."
			val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
					r,
				)
			LOG.warn(ex) { msg }
			return response
		} catch (ex: SCIOException) {
			val msg = "Unknown error while sending transmit control command."
			val r = makeResultUnknownIFDError(msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
					r,
				)
			LOG.warn(ex) { msg }
			return response
		} catch (ex: InterruptedException) {
			val msg = String.format("Cancellation by user.")
			LOG.warn(ex) { msg }
			val r = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.ControlIFDResponse::class.java,
					r,
				)
			return response
		}
	}

	override fun connect(parameters: Connect): ConnectResponse {
		try {
			var response: ConnectResponse
			// check if the requested handle is valid
			if (!ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
				val msg = "Invalid context handle specified."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
						r,
					)
				return response
			} else {
				try {
					val name = parameters.getIFDName()

					// make sure the slot is connected before attemting to get a slave channel
					cm!!.openMasterChannel(name)

					val slotHandle = cm!!.openSlaveChannel(name).p1
					val ch = cm!!.getSlaveChannel(slotHandle)

					// make connection exclusive
					val exclusive = parameters.isExclusive
					if (exclusive != null && exclusive == true) {
						val transact = BeginTransaction()
						transact.setSlotHandle(slotHandle)
						val resp = beginTransaction(transact)
						if (resp.getResult().getResultMajor() == ECardConstants.Major.ERROR) {
							// destroy channel, when not successful here
							ch.shutdown()
							response =
								WSHelper.makeResponse(
									iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
									resp.getResult(),
								)
							return response
						}
					}

					// connection established, return result
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
							org.openecard.common.WSHelper
								.makeResultOK(),
						)
					response.setSlotHandle(slotHandle)
					return response
				} catch (ex: NoSuchTerminal) {
					val msg = "The requested terminal does not exist."
					val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
							r,
						)
					LOG.warn(ex) { msg }
					return response
				} catch (ex: NullPointerException) {
					val msg = "The requested terminal does not exist."
					val r = makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
							r,
						)
					LOG.warn(ex) { msg }
					return response
				} catch (ex: IllegalStateException) {
					val msg = "No card available in the requested terminal."
					val r = makeResultError(ECardConstants.Minor.IFD.Terminal.NO_CARD, msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
							r,
						)
					LOG.warn(ex) { msg }
					return response
				} catch (ex: SCIOException) {
					val msg = "Unknown error in the underlying SCIO implementation."
					val r = makeResultUnknownIFDError(msg)
					response =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
							r,
						)
					LOG.warn(ex) { msg }
					return response
				}
			}
		} catch (ex: Exception) {
			LOG.warn(ex) { "${ex.message}" }
			throwThreadKillException(ex)
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.ConnectResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(ex),
			)
		}
	}

	@Synchronized
	override fun disconnect(parameters: Disconnect): DisconnectResponse {
		try {
			var response: DisconnectResponse
			if (!hasContext()) {
				val msg = "Context not initialized."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.DisconnectResponse::class.java,
						r,
					)
				return response
			}

			try {
				val handle = parameters.getSlotHandle()
				val ch = cm!!.getSlaveChannel(handle)
				cm!!.closeSlaveChannel(handle)

				// process actions
				val card = ch.channel.card
				val action = parameters.getAction()
				if (ActionType.RESET == action) {
					val ifdName = card.terminal.name
					val master = cm!!.getMasterChannel(ifdName)

					var builder = HandlerBuilder.create()
					val cHandleIn =
						builder
							.setCardType(ECardConstants.UNKNOWN_CARD)
							.setCardIdentifier(card.aTR.bytes)
							.setContextHandle(ctxHandle)
							.setIfdName(ifdName)
							.setSlotIdx(BigInteger.ZERO)
							.buildConnectionHandle()
					builder = HandlerBuilder.create()
					val cHandleRm =
						builder
							.setContextHandle(ctxHandle)
							.setIfdName(ifdName)
							.setSlotIdx(BigInteger.ZERO)
							.buildConnectionHandle()

					try {
						master.reconnect()
						evManager!!.emitResetCardEvent(cHandleRm, cHandleIn, card.protocol.toUri())
					} catch (ex: IllegalStateException) {
						LOG.warn(ex) { "Card reconnect failed, trying to establish new card connection." }
						cm!!.closeMasterChannel(ifdName)
						LOG.debug { "Master channel closed successfully." }
						try {
							cm!!.getMasterChannel(ifdName)
							LOG.debug { "New card connection established successfully." }
							evManager!!.emitResetCardEvent(cHandleRm, cHandleIn, card.protocol.toUri())
						} catch (ex2: NoSuchTerminal) {
							LOG.error(ex) { "No terminal present anymore." }
						}
					}
				}

				// TODO: take care of other actions (probably over ControlIFD)
				// the default is to not disconnect the card, because all existing connections would be broken
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.DisconnectResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultOK(),
					)
				return response
			} catch (ex: NoSuchChannel) {
				val msg = "No card available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.DisconnectResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			} catch (ex: SCIOException) {
				val msg = "Unknown error in the underlying SCIO implementation."
				val r = makeResultUnknownIFDError(msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.DisconnectResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			}
		} catch (ex: Exception) {
			LOG.warn(ex) { "${ex.message}" }
			throwThreadKillException(ex)
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.DisconnectResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(ex),
			)
		}
	}

	override fun beginTransaction(beginTransaction: BeginTransaction): BeginTransactionResponse {
		try {
			val response: BeginTransactionResponse
			if (!hasContext()) {
				val msg = "Context not initialized."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse::class.java,
						r,
					)
				return response
			}

			try {
				val handle = beginTransaction.getSlotHandle()
				val ch = cm!!.getSlaveChannel(handle)
				ch.beginExclusive()
			} catch (ex: NoSuchChannel) {
				val msg = "No card available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			} catch (ex: IllegalStateException) {
				val msg = "No card available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			} catch (ex: SCIOException) {
				val msg: String?
				val minor: String?
				when (ex.code) {
					SCIOErrorCode.SCARD_W_RESET_CARD, SCIOErrorCode.SCARD_W_REMOVED_CARD, SCIOErrorCode.SCARD_E_READER_UNAVAILABLE, SCIOErrorCode.SCARD_E_NO_SMARTCARD, SCIOErrorCode.SCARD_E_NO_SERVICE -> {
						msg = String.format("Slot handle is not available [%s].", ex.code.name)
						minor = ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE
						LOG.debug(ex) { msg }
					}

					else -> {
						msg = "Unknown error in the underlying SCIO implementation."
						minor = ECardConstants.Minor.App.UNKNOWN_ERROR
						LOG.warn(ex) { msg }
					}
				}
				val r = makeResultError(minor, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse::class.java,
						r,
					)
				return response
			}

			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultOK(),
				)
			return response
		} catch (ex: Exception) {
			LOG.warn(ex) { "${ex.message}" }
			throwThreadKillException(ex)
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(ex),
			)
		}
	}

	override fun endTransaction(parameters: EndTransaction): EndTransactionResponse {
		try {
			val response: EndTransactionResponse
			if (!hasContext()) {
				val msg = "Context not initialized."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.EndTransactionResponse::class.java,
						r,
					)
				return response
			}

			try {
				val handle = parameters.getSlotHandle()
				val ch = cm!!.getSlaveChannel(handle)
				ch.endExclusive()
			} catch (ex: NoSuchChannel) {
				val msg = "No card with transaction available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.EndTransactionResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			} catch (ex: IllegalStateException) {
				val msg = "No card with transaction available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.EndTransactionResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			} catch (ex: SCIOException) {
				val msg = "Unknown error in the underlying SCIO implementation."
				val r = makeResultUnknownIFDError(msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.EndTransactionResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			}

			response =
				WSHelper.makeResponse(
					iso.std.iso_iec._24727.tech.schema.EndTransactionResponse::class.java,
					org.openecard.common.WSHelper
						.makeResultOK(),
				)
			return response
		} catch (ex: Exception) {
			LOG.warn(ex) { "${ex.message}" }
			throwThreadKillException(ex)
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.EndTransactionResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(ex),
			)
		}
	}

	@Publish
	override fun transmit(parameters: Transmit): TransmitResponse {
		try {
			var response: TransmitResponse
			if (!hasContext()) {
				val msg = "Context not initialized."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.TransmitResponse::class.java,
						r,
					)
				return response
			}

			try {
				val handle = parameters.getSlotHandle()
				val ch = cm!!.getSlaveChannel(handle)

				val apdus = parameters.getInputAPDUInfo()
				// check that the apdus contain sane values
				for (apdu in apdus) {
					for (code in apdu.getAcceptableStatusCode()) {
						if (code.size == 0 || code.size > 2) {
							val msg = "Invalid accepted status code given."
							val r = makeResultError(ECardConstants.Minor.App.PARM_ERROR, msg)
							response =
								WSHelper.makeResponse(
									iso.std.iso_iec._24727.tech.schema.TransmitResponse::class.java,
									r,
								)
							return response
						}
					}
				}

				// transmit APDUs and stop if an error occurs or a not expected status is hit
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.TransmitResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultOK(),
					)
				var result: Result?
				val rapdus = response.getOutputAPDU()
				try {
					for (capdu in apdus) {
						val rapdu = ch.transmit(capdu.getInputAPDU(), capdu.getAcceptableStatusCode())
						rapdus.add(rapdu)
					}
					result = makeResultOK()
				} catch (ex: TransmitException) {
					rapdus.add(ex.responseAPDU)
					result = ex.result
				} catch (ex: SCIOException) {
					val msg = "Error during transmit."
					LOG.warn(ex) { msg }
					result = makeResultUnknownIFDError(msg)
				} catch (ex: IllegalStateException) {
					val msg = "Card removed during transmit."
					LOG.warn(ex) { msg }
					result = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				} catch (ex: IllegalArgumentException) {
					val msg = "Given command contains a MANAGE CHANNEL APDU."
					LOG.error(ex) { msg }
					result = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				} catch (ex: InterruptedException) {
					val msg = String.format("Cancellation by user.")
					LOG.error(ex) { msg }
					result = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
				}

				response.setResult(result)

				return response
			} catch (ex: NoSuchChannel) {
				val msg = "No card with transaction available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.TransmitResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			} catch (ex: IllegalStateException) {
				val msg = "No card with transaction available in the requested terminal."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						iso.std.iso_iec._24727.tech.schema.TransmitResponse::class.java,
						r,
					)
				LOG.warn(ex) { msg }
				return response
			}
		} catch (ex: Exception) {
			LOG.warn(ex) { "${ex.message}" }
			throwThreadKillException(ex)
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.TransmitResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(ex),
			)
		}
	}

	override fun verifyUser(parameters: VerifyUser): VerifyUserResponse {
		// TODO: convert to IFD Protocol
		try {
			var response: VerifyUserResponse?
			if (!hasContext()) {
				val msg = "Context not initialized."
				val r = makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg)
				response =
					WSHelper.makeResponse(
						VerifyUserResponse::class.java,
						r,
					)
				return response
			}

			val channel = cm!!.getSlaveChannel(parameters.getSlotHandle())
			val aTerm = AbstractTerminal(this, cm!!, channel, env!!.gui, ctxHandle, parameters.getDisplayIndex())
			try {
				response = aTerm.verifyUser(parameters)
				return response
			} catch (ex: IFDException) {
				response =
					WSHelper.makeResponse(
						VerifyUserResponse::class.java,
						ex.result,
					)
				return response
			}
		} catch (ex: Exception) {
			LOG.warn(ex) { "${ex.message}" }
			throwThreadKillException(ex)
			return WSHelper.makeResponse(
				VerifyUserResponse::class.java,
				makeResult(ex),
			)
		}
	}

	override fun modifyVerificationData(parameters: ModifyVerificationData?): ModifyVerificationDataResponse {
		val response: ModifyVerificationDataResponse
		val msg = "Command not supported."
		response =
			WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.ModifyVerificationDataResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultUnknownError(msg),
			)
		return response
	}

	override fun output(parameters: Output?): OutputResponse {
		val response: OutputResponse
		val msg = "Command not supported."
		response =
			WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.OutputResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultUnknownError(msg),
			)
		return response
	}

	override fun establishChannel(parameters: EstablishChannel): EstablishChannelResponse {
		val slotHandle = parameters.getSlotHandle()
		try {
			val channel = cm!!.getSlaveChannel(slotHandle)
			val termInfo = TerminalInfo(cm!!, channel)
			val protoParam = parameters.getAuthenticationProtocolData()
			val protocol = protoParam.getProtocol()

			// check if it is PACE and try to perform native implementation
			// get pace capabilities
			val paceCapabilities = termInfo.pACECapabilities
			val supportedProtos = TerminalInfo.Companion.buildPACEProtocolList(paceCapabilities)

			// check out if this actually a PACE request
			// FIXME: check type of protocol

			// i don't care which type is supported, i try it anyways
			if (!supportedProtos.isEmpty() && supportedProtos[0].startsWith(protocol)) {
				// yeah, PACE seems to be supported by the reader, big win
				val paceParam = PACEInputType(protoParam)
				// extract variables needed for pace
				val pinID = paceParam.pINID
				// optional elements
				val chat = paceParam.cHAT
				val pin = paceParam.pIN
				val certDesc = paceParam.certificateDescription

				// prepare pace data structures
				val estPaceReq = EstablishPACERequest(pinID, chat, null, certDesc) // TODO: add supplied PIN
				val execPaceReq =
					ExecutePACERequest(ExecutePACERequest.Function.EstablishPACEChannel, estPaceReq.toBytes())
				// see if PACE type demanded for this input value combination is supported
				// TODO: check if this additional check is really necessary
				if (estPaceReq.isSupportedType(paceCapabilities)) {
					val reqData = execPaceReq.toBytes()
					LOG.debug { "executeCtrlCode request: ${ByteUtils.toHexString(reqData)}" }
					// execute pace
					val features = termInfo.featureCodes
					val resData = channel.transmitControlCommand(features.get(PCSCFeatures.EXECUTE_PACE)!!, reqData)
					LOG.debug { "Response of executeCtrlCode: ${ByteUtils.toHexString(resData)}" }
					// evaluate response
					val execPaceRes = ExecutePACEResponse(resData)
					if (execPaceRes.isError) {
						return WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse::class.java,
							execPaceRes.getResult(),
						)
					}
					val estPaceRes = EstablishPACEResponse(execPaceRes.data)
					// get values and prepare response
					val authDataResponse = paceParam.outputType
					// mandatory fields
					authDataResponse.setRetryCounter(estPaceRes.retryCounter)
					authDataResponse.setEFCardAccess(estPaceRes.eFCardAccess)
					// optional fields
					if (estPaceRes.hasCurrentCAR()) {
						authDataResponse.setCurrentCAR(estPaceRes.currentCAR)
					}
					if (estPaceRes.hasPreviousCAR()) {
						authDataResponse.setPreviousCAR(estPaceRes.previousCAR)
					}
					if (estPaceRes.hasIDICC()) {
						authDataResponse.setIDPICC(estPaceRes.iDICC)
					}
					// create response type and return
					val response: EstablishChannelResponse =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse::class.java,
							org.openecard.common.WSHelper
								.makeResultOK(),
						)
					response.setAuthenticationProtocolData(authDataResponse.authDataType)
					return response
				}
			} // end native pace support

			// check out available software protocols
			this.protocolFactories.get(protocol)?.let { factory ->
				val protoImpl = factory.createInstance()
				val response = protoImpl.establish(parameters, env!!.dispatcher!!, env!!.gui!!)
				// register protocol instance for secure messaging when protocol was processed successful
				if (response.getResult().getResultMajor() == ECardConstants.Major.OK) {
					channel.addSecureMessaging(protoImpl)
				}
				return response
			}

			// if this point is reached a native implementation is not present, try registered protocols
			val r = makeResultUnknownError("No such protocol available in this IFD.")
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse::class.java,
				r,
			)
		} catch (t: Throwable) {
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(t),
			)
		}
	}

	override fun destroyChannel(parameters: DestroyChannel): DestroyChannelResponse {
		try {
			var destroyChannelResponse = DestroyChannelResponse()
			val slotHandle = parameters.getSlotHandle()
			val channel = cm!!.getSlaveChannel(slotHandle)
			val termInfo = TerminalInfo(cm!!, channel)

			// check if it is PACE and try to perform native implementation
			// get pace capabilities
			val paceCapabilities = termInfo.pACECapabilities
			if (paceCapabilities.contains(PACECapabilities.PACECapability.DestroyPACEChannel)) {
				val execPaceReq = ExecutePACERequest(ExecutePACERequest.Function.DestroyPACEChannel)

				val reqData = execPaceReq.toBytes()
				LOG.debug { "executeCtrlCode request: ${ByteUtils.toHexString(reqData)}" }
				// execute pace
				val features = termInfo.featureCodes
				val resData = channel.transmitControlCommand(features[PCSCFeatures.EXECUTE_PACE]!!, reqData)
				LOG.debug { "Response of executeCtrlCode: ${ByteUtils.toHexString(resData)}" }
				// evaluate response
				val execPaceRes = ExecutePACEResponse(resData)
				if (execPaceRes.isError) {
					destroyChannelResponse =
						WSHelper.makeResponse(
							iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse::class.java,
							execPaceRes.getResult(),
						)
				}
			}

			channel.removeSecureMessaging()

			if (destroyChannelResponse.getResult() == null) {
				val r = Result()
				r.setResultMajor(ECardConstants.Major.OK)
				destroyChannelResponse.setResult(r)
			}

			return destroyChannelResponse
		} catch (t: Throwable) {
			return WSHelper.makeResponse(
				iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse::class.java,
				org.openecard.common.WSHelper
					.makeResult(t),
			)
		}
	}

	private fun evaluateControlIFDRAPDU(resultCommand: ByteArray): Result {
		val result = ByteUtils.toInteger(resultCommand)
		return when (result) {
			0x9000 -> makeResultOK()
			0x6400 -> makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Timeout.")
			else -> makeResultUnknownIFDError("Unknown return code from terminal.")
		}
	}

	private fun throwThreadKillException(ex: Exception) {
		val cause =
			if (ex is InvocationTargetExceptionUnchecked) {
				ex.cause
			} else {
				ex
			}

		if (cause is ThreadTerminateException) {
			throw cause as RuntimeException
		} else if (cause is InterruptedException) {
			throw ThreadTerminateException("Thread running inside SAL interrupted.", cause)
		} else if (cause is RuntimeException) {
			throw ex as RuntimeException
		}
	}

	// TODO: make all commands cancellable
}
