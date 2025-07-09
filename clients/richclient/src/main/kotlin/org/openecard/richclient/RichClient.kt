/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.Initialize
import iso.std.iso_iec._24727.tech.schema.ReleaseContext
import iso.std.iso_iec._24727.tech.schema.Terminate
import org.apache.http.HttpException
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpEntityEnclosingRequest
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.protocol.HttpRequestExecutor
import org.openecard.addon.AddonManager
import org.openecard.build.BuildInfo
import org.openecard.common.AppVersion.name
import org.openecard.common.ClientEnv
import org.openecard.common.ECardConstants
import org.openecard.common.OpenecardProperties
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.event.EventDispatcherImpl
import org.openecard.common.event.EventType
import org.openecard.common.sal.CombinedCIFProvider
import org.openecard.control.binding.http.HttpBinding
import org.openecard.gui.message.DialogType
import org.openecard.gui.swing.SwingDialogWrapper
import org.openecard.gui.swing.SwingUserConsent
import org.openecard.gui.swing.common.GUIDefaults
import org.openecard.httpcore.HttpRequestHelper
import org.openecard.httpcore.KHttpUtils
import org.openecard.httpcore.StreamHttpClientConnection
import org.openecard.i18n.I18N
import org.openecard.ifd.protocol.pace.PACEProtocolFactory
import org.openecard.ifd.scio.IFD
import org.openecard.management.TinyManagement
import org.openecard.recognition.CardRecognitionImpl
import org.openecard.recognition.RepoCifProvider
import org.openecard.richclient.gui.AppTray
import org.openecard.richclient.gui.SettingsAndDefaultViewWrapper
import org.openecard.richclient.updater.VersionUpdateChecker
import org.openecard.sal.TinySAL
import org.openecard.transport.dispatcher.MessageDispatcher
import org.openecard.ws.SAL
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
	private var httpBinding: HttpBinding? = null

	// Client environment
	private var env = ClientEnv()

	// Interface Device Layer (IFD)
	private var ifd: IFD? = null

	// Service Access Layer (SAL)
	private var sal: SAL? = null

	// AddonManager
	private var manager: AddonManager? = null

	// EventDispatcherImpl
	private var eventDispatcher: EventDispatcherImpl? = null

	// Card recognition
	private var recognition: CardRecognitionImpl? = null

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

			// Set up Management
			val management = TinyManagement(env)
			env.management = management

			// Set up MiddlewareConfig
// 	    MiddlewareConfigLoader mwConfigLoader = new MiddlewareConfigLoader();
// 	    List<MiddlewareSALConfig> mwSALConfigs = mwConfigLoader.getMiddlewareSALConfigs();

			// Set up CardRecognitionImpl
			recognition = CardRecognitionImpl(env)
			env.recognition = recognition

			// Set up the IFD
			ifd = IFD()
			ifd!!.addProtocol(ECardConstants.Protocol.PACE, PACEProtocolFactory())
			ifd!!.setEnvironment(env)
			env.ifd = ifd

			val cifProv = CombinedCIFProvider()
			env.cifProvider = cifProv
			cifProv.addCifProvider(RepoCifProvider(recognition))

			// Set up SAL
			val mainSal = TinySAL(env)
			sal = mainSal
			env.sal = sal

			// Set up Middleware SAL
// 	    for (MiddlewareSALConfig mwSALConfig : mwSALConfigs) {
// 		if (! mwSALConfig.isDisabled()) {
// 		    MiddlewareSAL mwSal = new MiddlewareSAL(env, mwSALConfig);
// 		    mwSal.setGui(gui);
// 		    sal.addSpecializedSAL(mwSal);
// 		}
// 	    }

			// Start up control interface
			val guiWrapper = SettingsAndDefaultViewWrapper()
			try {
				manager = AddonManager(env, guiWrapper, mainSal.salStateView)
				guiWrapper.setAddonManager(manager)
				mainSal.setAddonManager(manager)

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

				// start HTTP server
				httpBinding = HttpBinding(port)
				httpBinding!!.setAddonManager(manager!!)
				httpBinding!!.start()

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

			tray!!.endSetup(env, manager!!)

			// Initialize the EventManager
			eventDispatcher!!.add(
				tray!!.status!!,
				EventType.TERMINAL_ADDED,
				EventType.TERMINAL_REMOVED,
				EventType.CARD_INSERTED,
				EventType.CARD_RECOGNIZED,
				EventType.CARD_REMOVED,
			)

			// Perform an EstablishContext to get a ContextHandle
			try {
				val establishContext = EstablishContext()
				val establishContextResponse = ifd!!.establishContext(establishContext)
				checkResult(establishContextResponse)
				contextHandle = establishContextResponse.contextHandle
				mainSal.setIfdCtx(contextHandle)
			} catch (ex: WSHelper.WSException) {
				message = I18N.strings.richclient_client_startup_failed_nocontext.localized()
				throw ex
			}

			// initialize SAL
			checkResult(sal!!.initialize(Initialize()))

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
			if (eventDispatcher != null) {
				eventDispatcher!!.terminate()
			}

			// TODO: shutdown addon manager and related components?
			if (manager != null) {
				manager!!.shutdown()
			}

			// shutdown control modules
			if (httpBinding != null) {
				httpBinding!!.stop()
			}

			// shutdown SAL
			if (sal != null) {
				val terminate = Terminate()
				sal!!.terminate(terminate)
			}

			// shutdown IFD
			if (ifd != null && contextHandle != null) {
				val releaseContext = ReleaseContext()
				releaseContext.contextHandle = contextHandle
				ifd!!.releaseContext(releaseContext)
			}
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
			val startTime = System.currentTimeMillis()
			val exec = HttpRequestExecutor()
			val httpCtx: HttpContext = BasicHttpContext()

			do {
				try {
					val port = if (regUrl.port == -1) regUrl.defaultPort else regUrl.port
					val sock = Socket(regUrl.host, port)
					val con = StreamHttpClientConnection(sock.getInputStream(), sock.getOutputStream())
					var req = BasicHttpEntityEnclosingRequest("POST", regUrl.file)
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
