/****************************************************************************
 * Copyright (C) 2025 ecsec GmbH.
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

package org.openecard.richclient.tr03124

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.request.acceptItems
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EcardStatus
import org.openecard.addons.tr03124.Tr03124Binding
import org.openecard.addons.tr03124.Tr03124Binding.Parameter.ShowUi.ShowUiModules.Companion.toUiModule
import org.openecard.gui.UserConsent
import org.openecard.richclient.sc.CardWatcher
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class RichclientTr03124Binding(
	val clientInfo: ClientInformation,
	val terminalFactory: TerminalFactory,
	val cardRecognition: CardRecognition,
	val cardWatcher: CardWatcher,
	val gui: UserConsent,
	val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Tr03124Binding {
	private val paceFactory = PaceFeatureSoftwareFactory()

	private val requestMutex = Mutex()
	private var eacJob: Job? = null

	override suspend fun activate(tcTokenUrl: String): BindingResponse {
		val jobFuture =
			requestMutex.withLock {
				if (eacJob?.isActive == true) {
					return BindingResponse.ReferencedContentResponse(
						HttpStatusCode.Conflict.value,
						BindingResponse.ContentCode.OTHER_PROCESS_RUNNING,
					)
				} else {
					val job =
						CoroutineScope(dispatcher).async(CoroutineName("EAC-Process")) {
							try {
								terminalFactory.load().withContextSuspend { ctx ->
									val eacProcess =
										EacProcess(
											ctx,
											cardRecognition,
											paceFactory,
											clientInfo,
											cardWatcher,
											gui,
										)
									eacProcess.start(tcTokenUrl)
								}
							} catch (ex: BindingException) {
								ex.toResponse()
							}
						}
					eacJob = job.job
					job
				}
			}

		return jobFuture.await()
	}

	override suspend fun status(): EcardStatus =
		EcardStatus(
			app =
				EcardStatus.ProductEntry(
					name = clientInfo.userAgent.name,
					vendor = "ecsec GmbH",
					version = clientInfo.userAgent.version.toString(),
				),
			specs =
				listOf(
					EcardStatus.ProductEntry(name = "TR-03124-1", vendor = "Federal Office for Information Security", version = "1.4"),
				),
		)

	override suspend fun showUi(module: Tr03124Binding.Parameter.ShowUi.ShowUiModules) {
		// TODO: Not yet implemented
	}
}

fun Routing.registerTr03124Binding(trBinding: Tr03124Binding) {
	get(Tr03124Binding.Parameter.serverPath) {
		val tokenUrl = call.queryParameters[Tr03124Binding.Parameter.Activate.tcTokenUrl]
		val isStatusRequest = call.queryParameters.contains(Tr03124Binding.Parameter.Status.status)
		val gui = call.queryParameters[Tr03124Binding.Parameter.ShowUi.showUi]

		val response: BindingResponse =
			if (tokenUrl != null) {
				trBinding.activate(tokenUrl)
			} else if (isStatusRequest) {
				val status = trBinding.status()
				call.request
					.acceptItems()
					.asSequence()
					.mapNotNull {
						if (ContentType.Application.Json.match(it.value)) {
							status.asJson()
						} else if (ContentType.Text.Plain.match(it.value)) {
							status.asAusweisAppPlainText()
						} else {
							null
						}
					}.firstOrNull() ?: BindingResponse.ReferencedContentResponse(
					status = HttpStatusCode.NotAcceptable.value,
					BindingResponse.ContentCode.NO_ACCEPTABLE_FORMAT,
				)
			} else if (gui != null) {
				trBinding.showUi(gui.toUiModule())
				BindingResponse.NoContent()
			} else {
				BindingResponse.ReferencedContentResponse(
					HttpStatusCode.BadRequest.value,
					BindingResponse.ContentCode.NO_SUITABLE_ACTIVATION_PARAMETERS,
				)
			}

		response.toKtorResponse(call)
	}
}

fun EcardStatus.asAusweisAppPlainText(): BindingResponse {
	val entries =
		buildList {
			app.name.let { add("Implementation-Title: $it") }
			app.vendor?.let { add("Implementation-Title: $it") }
			app.version.let { add("Implementation-Version: $it") }
			specs.firstOrNull()?.let { spec ->
				spec.name.let { add("Specification-Title: $it") }
				spec.vendor?.let { add("Specification-Title: $it") }
				spec.version.let { add("Specification-Version: $it") }
			}
		}
	return BindingResponse.ContentResponse(
		status = HttpStatusCode.OK.value,
		ContentType.Text.Plain
			.withCharset(Charsets.UTF_8)
			.toString(),
		entries.joinToString(separator = "\n").encodeToByteArray(),
	)
}

fun EcardStatus.asJson(): BindingResponse {
	val text = Json.encodeToString(this)
	return BindingResponse.ContentResponse(
		status = HttpStatusCode.OK.value,
		ContentType.Application.Json
			.withCharset(Charsets.UTF_8)
			.toString(),
		text.encodeToByteArray(),
	)
}
