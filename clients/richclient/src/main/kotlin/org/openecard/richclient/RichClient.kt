/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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

package org.openecard.richclient

import ch.qos.logback.core.joran.spi.JoranException
import com.sun.jna.Platform
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.Win32Exception
import com.sun.jna.platform.win32.WinReg
import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.apache.http.HttpException
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.UserAgent
import org.openecard.build.BuildInfo
import org.openecard.common.AppVersion.name
import org.openecard.common.ClientEnv
import org.openecard.common.OpenecardProperties
import org.openecard.common.event.EventDispatcherImpl
import org.openecard.control.binding.ktor.HttpService
import org.openecard.gui.message.DialogType
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.openecard.gui.swing.common.GUIDefaults
import org.openecard.httpcore.HttpRequestHelper
import org.openecard.httpcore.KHttpUtils
import org.openecard.httpcore.StreamHttpClientConnection
import org.openecard.i18n.I18N
import org.openecard.richclient.gui.AppTray
import org.openecard.richclient.gui.SettingsAndDefaultViewWrapper
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith
import org.openecard.richclient.sc.CifDb
import org.openecard.richclient.sc.EventCardRecognition
import org.openecard.richclient.tr03124.RichclientTr03124Binding
import org.openecard.richclient.tr03124.registerTr03124Binding
import org.openecard.richclient.updater.VersionUpdateChecker
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pcsc.PcscTerminalFactory
import org.openecard.transport.dispatcher.MessageDispatcher
import java.io.IOException
import java.net.BindException
import java.net.Socket
import java.net.URI
import java.net.URL
import java.nio.charset.UnsupportedCharsetException
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.FutureTask
import kotlin.system.exitProcess

/**
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Hans-Martin Haase
 * @author René Lottes
 * @author Tobias Wich
 */
class RichClient {
	// Tray icon
	private var tray: AppTray? = null

	// Control interface
	private var httpBinding: HttpService? = null

	// Client environment
	private var env = ClientEnv()

	private var terminalFactory: TerminalFactory? = null
	private var cardWatcher: CardWatcher? = null

	// EventDispatcherImpl
	private var eventDispatcher: EventDispatcherImpl? = null

	// ContextHandle determines a specific IFD layer context
	private var contextHandle: ByteArray? = null

	fun setup() {
		GUIDefaults.initialize()

		val title =
			I18N.strings.richclient_client_startup_failed_headline
				.format(name)
				.localized()
		var message: String? = null
		// Set up GUI
		val gui = SwingUserConsent(SwingDialogWrapper())

		try {
			tray = AppTray(this)
			tray!!.beginSetup()

			// Set up client environment
			env = ClientEnv()

			env.gui = gui

			// Set up the Dispatcher
			val dispatcher = MessageDispatcher(env)
			env.dispatcher = dispatcher

			// Set up EventDispatcherImpl
			eventDispatcher = EventDispatcherImpl()
			// start event dispatcher
			eventDispatcher!!.start()

			env.eventDispatcher = eventDispatcher

			// Set up the IFD and card watcher
			val terminalFactory = PcscTerminalFactory.instance
			this.terminalFactory = terminalFactory

			val cifDb = CifDb.Companion.Bundled
			val cardWatcher = CardWatcher(CoroutineScope(Dispatchers.Default), cifDb.getCardRecognition(), terminalFactory)
			this.cardWatcher = cardWatcher
			cardWatcher.start()

			// Set up Middleware SAL
// 	    for (MiddlewareSALConfig mwSALConfig : mwSALConfigs) {
// 		if (! mwSALConfig.isDisabled()) {
// 		    MiddlewareSAL mwSal = new MiddlewareSAL(env, mwSALConfig);
// 		    mwSal.setGui(gui);
// 		    sal.addSpecializedSAL(mwSal);
// 		}
// 	    }

			val eventCardRecognition = EventCardRecognition()
			eventCardRecognition.registerWith(cardWatcher)

			// Start up control interface
			val guiWrapper = SettingsAndDefaultViewWrapper()
			try {
				// initialize http binding
				var port = 24727
				var dispatcherMode = false
				val hk = WinReg.HKEY_LOCAL_MACHINE
				val regPath = "SOFTWARE\\" + OpenecardProperties.getProperty("registry.app_name")
				if (Platform.isWindows()) {
					LOG.debug { "Checking if dispatcher mode should be used." }
					try {
						if (regKeyExists(hk, regPath, "Dispatcher_Mode")) {
							val value = Advapi32Util.registryGetStringValue(hk, regPath, "Dispatcher_Mode")
							dispatcherMode = value.toBoolean()
							// let socket chose its port
							port = 0
						}
					} catch (ex: Win32Exception) {
						LOG.warn(ex) { "Failed to read 'Dispatcher_Mode' registry key. Using normal operation mode." }
					}
				}
				if (!dispatcherMode) {
					try {
						port = OpenecardProperties.getProperty("http-binding.port")!!.toInt()
					} catch (ex: NumberFormatException) {
						LOG.warn { "Error in config file, HTTP binding port is malformed." }
					}
				}

				// configure TR-03124 addon
				val uaVersion =
					BuildInfo.version.let { v ->
						UserAgent.Version(v.major, v.minor, v.patch)
					}
				val clientInfo = ClientInformation(UserAgent("Open-eCard Richclient", uaVersion))
				val tr03124Binding =
					RichclientTr03124Binding(
						clientInfo = clientInfo,
						terminalFactory = terminalFactory,
						cardRecognition = eventCardRecognition,
						cardWatcher = cardWatcher,
						gui = gui,
					)

				// start HTTP server
				httpBinding =
					HttpService.start(wait = false, port = port) {
						registerTr03124Binding(tr03124Binding)
					}

				if (dispatcherMode) {
					val waitTime = getRegInt(hk, regPath, "Retry_Wait_Time", 5000L)!!
					val timeout = getRegInt(hk, regPath, "DP_Timeout", 3600000L)!!
					// try to register with dispatcher service
					LOG.debug { "Trying to register HTTP binding port with dispatcher service." }
					val realPort = httpBinding!!.port
					val regUrl = URI("http://127.0.0.1:24727/dp/register").toURL()
					val ft: FutureTask<*> = FutureTask(DispatcherRegistrator(regUrl, realPort, waitTime, timeout), 1)
					val registerThread = Thread(ft, "Register-Dispatcher-Service")
					registerThread.isDaemon = true
					registerThread.start()
					// wait until thread is finished
					ft.get()
				}
			} catch (e: BindException) {
				message =
					I18N.strings.richclient_client_startup_failed_portinuse
						.format(name)
						.localized()
				throw e
			}

			tray!!.endSetup(cifDb, cardWatcher)

			// perform GC to bring down originally allocated memory
			Timer("GC-Task").schedule(GCTask(), 5000)

			val update = OpenecardProperties.getProperty("check-for-updates").toBoolean()
			if (update) {
				// check for updates
				Timer("Update-Task").schedule(UpdateTask(tray!!), 1)
			}
		} catch (ex: Exception) {
			LOG.error(ex) { "${ex.message}" }

			if (message.isNullOrEmpty()) {
				// Add exception message if no custom message is set
				message = ex.message
			}

			// Show dialog to the user and shut down the client
			val msg = String.format("%s%n%n%s", title, message)
			gui.obtainMessageDialog().showMessageDialog(msg, name, DialogType.ERROR_MESSAGE)
			teardown()
		} catch (ex: Throwable) {
			LOG.error(ex) { "Unexpected error occurred. Exiting client." }
			exitProcess(1)
		}
	}

	private class UpdateTask(
		private val tray: AppTray,
	) : TimerTask() {
		override fun run() {
			if (!BuildInfo.version.isStable) {
				// snapshot versions don't need updates
				LOG.info { "Skipping update check for developer build." }
				return
			}

			val updateChecker = VersionUpdateChecker.loadCurrentVersionList()

			updateChecker?.let {
				if (updateChecker.getUpdateInfo() != null) {
					LOG.info { "Available update found." }
					tray.status?.showUpdateIcon(updateChecker)
				} else {
					LOG.info { "No update found, trying again later." }
				}
			}

			// repeat every 24 hours
			Timer().schedule(UpdateTask(tray), (24 * 60 * 60 * 1000).toLong())
		}
	}

	private class GCTask : TimerTask() {
		override fun run() {
			System.gc()
			System.runFinalization()
			System.gc()
			// repeat every 5 minutes
			Timer().schedule(GCTask(), (5 * 60 * 1000).toLong())
		}
	}

	fun teardown() {
		try {
			cardWatcher?.stop()

			if (eventDispatcher != null) {
				eventDispatcher!!.terminate()
			}

			// shutdown control modules
			httpBinding?.let {
				it.stop()
				httpBinding = null
			}

			// shutdown IFD
			terminalFactory = null
		} catch (ex: Exception) {
			LOG.error(ex) { "Failed to stop Richclient." }
		}

		exitProcess(0)
	}

	private class DispatcherRegistrator(
		private val regUrl: URL,
		private val bindingPort: Int,
		private val waitTime: Long,
		private val timeout: Long,
	) : Runnable {
		override fun run() {
			// TODO: replace with ktor
			val startTime = System.currentTimeMillis()
			val exec = HttpRequestExecutor()
			val httpCtx: HttpContext = BasicHttpContext()

			do {
				try {
					val port = if (regUrl.port == -1) regUrl.defaultPort else regUrl.port
					val sock = Socket(regUrl.host, port)
					val con = StreamHttpClientConnection(sock.getInputStream(), sock.getOutputStream())
					val req = BasicHttpEntityEnclosingRequest("POST", regUrl.file)
					// prepare request
					HttpRequestHelper.setDefaultHeader(req, regUrl)
					val reqContentType = ContentType.create("application/x-www-form-urlencoded", "UTF-8")
					val bodyStr = "Port=$bindingPort"
					val bodyEnt = StringEntity(bodyStr, reqContentType)
					req.entity = bodyEnt
					req.setHeader(bodyEnt.contentType)
					req.setHeader("Content-Length", bodyEnt.contentLength.toString())

					// send request
					KHttpUtils.dumpHttpRequest(LOG, req)
					val response = exec.execute(req, con, httpCtx)
					KHttpUtils.dumpHttpResponse(LOG, response)

					val statusCode = response.statusLine.statusCode
					if (statusCode == 204) {
						return
					} else {
						val msg = "Execution of dispatcher registration is not successful (code=$statusCode), trying again ..."
						LOG.info { msg }
					}
				} catch (ex: HttpException) {
					LOG.error(ex) { "Failed to send dispatcher registration reguest." }
				} catch (ex: IOException) {
					LOG.error(ex) { "Failed to send dispatcher registration reguest." }
				} catch (ex: UnsupportedCharsetException) {
					LOG.error(ex) { "Failed to send dispatcher registration reguest." }
				}

				// terminate in case there is no time left
				val now = System.currentTimeMillis()
				if (now - startTime > timeout) {
					throw RuntimeException("Failed to register with dispatcher service in a timely manner.")
				}
				// wait a bit and try again
				try {
					Thread.sleep(waitTime)
				} catch (ex: InterruptedException) {
					LOG.info { "Dispatcher registration interrupted." }
					return
				}
			} while (true)
		}
	}

	companion object {
		private val LOG: KLogger

		init {
			try {
				// load logger config from HOME if set
				LogbackConfig.load()
			} catch (ex: IOException) {
				System.err.println("Failed to load logback config from user config.")
				ex.printStackTrace(System.err)
				try {
					LogbackConfig.loadDefault()
				} catch (ex2: JoranException) {
					System.err.println("Failed to load logback default config.")
					ex.printStackTrace(System.err)
				}
			} catch (ex: JoranException) {
				System.err.println("Failed to load logback config from user config.")
				ex.printStackTrace(System.err)
				try {
					LogbackConfig.loadDefault()
				} catch (ex2: JoranException) {
					System.err.println("Failed to load logback default config.")
					ex.printStackTrace(System.err)
				}
			}
			LOG = KotlinLogging.logger { }
		}

		@JvmStatic
		fun main(args: Array<String>) {
			LOG.info { "Starting $name ${BuildInfo.version} ..." }

			LOG.debug {
				"Running on ${System.getProperty("os.name")} ${System.getProperty("os.version")} ${System.getProperty("os.arch")}."
			}

			val client = RichClient()
			client.setup()
		}

		private fun regKeyExists(
			hk: WinReg.HKEY,
			key: String,
			value: String,
		): Boolean =
			Advapi32Util.registryKeyExists(hk, key) &&
				Advapi32Util.registryValueExists(hk, key, value)

		private fun getRegInt(
			hk: WinReg.HKEY,
			key: String,
			value: String,
			defaultValue: Long?,
		): Long? {
			try {
				if (regKeyExists(hk, key, value)) {
					return Advapi32Util.registryGetIntValue(hk, key, value).toLong()
				}
			} catch (ex: Win32Exception) {
				LOG.debug { "Registry key ${key}\\$value does not exist or has wrong type." }
			}
			return defaultValue
		}
	}
}
