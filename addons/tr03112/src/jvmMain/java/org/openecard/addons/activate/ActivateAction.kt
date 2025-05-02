/****************************************************************************
 * Copyright (C) 2013-2018 HS Coburg.
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
package org.openecard.addons.activate

import iso.std.iso_iec._24727.tech.schema.PowerDownDevices
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.addon.AddonManager
import org.openecard.addon.AddonNotFoundException
import org.openecard.addon.Context
import org.openecard.addon.bind.*
import org.openecard.binding.tctoken.TCTokenHandler
import org.openecard.binding.tctoken.TCTokenResponse
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.binding.tctoken.ex.ActivationError
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.binding.tctoken.ex.FatalActivationError
import org.openecard.binding.tctoken.ex.NonGuiException
import org.openecard.common.*
import org.openecard.common.WSHelper.minorIsOneOf
import org.openecard.common.interfaces.Dispatcher
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.ViewController
import org.openecard.gui.message.DialogType
import org.openecard.httpcore.cookies.CookieManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.*

/**
 * Implementation of a plugin action performing a client activation with a TCToken.
 *
 * @author Dirk Petrautzki
 * @author Benedikt Biallowons
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class ActivateAction : AppPluginAction {
    private val lang: I18n = I18n.getTranslation("tr03112")

    private var tokenHandler: TCTokenHandler? = null
    private var statusAction: AppPluginAction? = null
    private var pinManAction: AppExtensionAction? = null
    private var gui: UserConsent? = null
    private var manager: AddonManager? = null
    private var settingsAndDefaultView: ViewController? = null
    private var dispatcher: Dispatcher? = null
    private var ctx: Context? = null

    override fun init(ctx: Context) {
        tokenHandler = TCTokenHandler(ctx)
        this.ctx = ctx
        gui = ctx.getUserConsent()
        dispatcher = ctx.getDispatcher()
        manager = ctx.getManager()
        settingsAndDefaultView = ctx.getViewController()
        try {
            val addonSpecStatus = manager!!.getRegistry().search("Status")
            statusAction = manager!!.getAppPluginAction(addonSpecStatus, "getStatus")
            val addonSpecPinMngmt = manager!!.getRegistry().search("PIN-Plugin")
            pinManAction = manager!!.getAppExtensionAction(addonSpecPinMngmt, "GetCardsAndPINStatusAction")
        } catch (ex: AddonNotFoundException) {
            // this should never happen because the status and pin plugin are always available
            val msg = "Failed to get Status or PIN Plugin."
            LOG.error(msg, ex)
            throw RuntimeException(msg, ex)
        }
    }

    override fun destroy(force: Boolean) {
        tokenHandler = null
        manager!!.returnAppPluginAction(statusAction)
        manager!!.returnAppExtensionAction(pinManAction)
    }

    override fun execute(
        body: RequestBody?,
        params: MutableMap<String?, String?>,
        headers: Headers?,
        attachments: MutableList<Attachment?>?,
        extraParams: MutableMap<String?, out Any?>?
    ): BindingResult {
        val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

        try {
            return checkRequestParameters(body, params, headers, attachments)
        } catch (t: Throwable) {
            LOG.error("Unexpected error returned from eID-Client Activation.", t)
            if (t is Error) {
                // don't handle errors, they are reserved for unhandleable errors
                throw t
            } else {
                return BindingResult(BindingResultCode.INTERNAL_ERROR)
            }
        } finally {
            // TODO: move this code to a location where it is explicitly called by the invoker (mobile case only)
            try {
                this.dispatcher!!.safeDeliver(PowerDownDevices())
            } catch (e: Exception) {
            }

            // clean up context
            dynCtx.clear()
            DynamicContext.remove()
        }
    }

    private val isShowRemoveCard: Boolean
        get() {
            val str =
                OpenecardProperties.getProperty("notification.omit_show_remove_card")
            return !str.toBoolean()
        }

    /**
     * Use the [UserConsent] to display the success message.
     */
    private fun showFinishMessage(response: TCTokenResponse) {
        // show the finish message just if we have a major ok
        if (ECardConstants.Major.OK == response.getResult().getResultMajor() && this.isShowRemoveCard) {
            val title = lang.translationForKey(ErrorTranslations.FINISH_TITLE)
            val msg = lang.translationForKey(ErrorTranslations.REMOVE_CARD)
            showBackgroundMessage(msg, title, DialogType.INFORMATION_MESSAGE)
        }
    }

    /**
     * Display a dialog in a separate thread.
     *
     * @param msg The message which shall be displayed.
     * @param title Title of the dialog window.
     * @param dialogType Type of the dialog.
     */
    private fun showBackgroundMessage(msg: String?, title: String?, dialogType: DialogType?) {
        Thread(object : Runnable {
            override fun run() {
                gui!!.obtainMessageDialog().showMessageDialog(msg, title, dialogType)
            }
        }, "Background_MsgBox").start()
    }

    /**
     * Use the [UserConsent] to display the given error message.
     *
     * @param errMsg Error message to display.
     */
    private fun showErrorMessage(errMsg: String?) {
        val title = lang.translationForKey(ErrorTranslations.ERROR_TITLE)
        val baseHeader = lang.translationForKey(ErrorTranslations.ERROR_HEADER)
        val exceptionPart = lang.translationForKey(ErrorTranslations.ERROR_MSG_IND)
        val removeCard = lang.translationForKey(ErrorTranslations.REMOVE_CARD)
        val msg = String.format("%s\n\n%s\n%s\n\n%s", baseHeader, exceptionPart, errMsg, removeCard)
        showBackgroundMessage(msg, title, DialogType.ERROR_MESSAGE)
    }

    /**
     * Check the request for correct parameters and invoke their processing if they are ok.
     *
     * @param body The body of the request.
     * @param params The query parameters and their values.
     * @param attachments Attachments of the request.
     * @return A [BindingResult] with an error if the parameters are not correct or one depending on the processing
     * of the parameters.
     */
    private fun checkRequestParameters(
        body: RequestBody?, params: MutableMap<String?, String?>, headers: Headers?,
        attachments: MutableList<Attachment?>?
    ): BindingResult {
        val response: BindingResult
        var emptyParms: Boolean
        var tokenUrl: Boolean
        var status: Boolean
        var showUI: Boolean
        showUI = false
        status = showUI
        tokenUrl = status
        emptyParms = tokenUrl

        if (params.isEmpty()) {
            emptyParms = true
        }

        if (params.containsKey("tcTokenURL")) {
            tokenUrl = true
        }

        if (params.containsKey("Status")) {
            status = true
        }

        if (params.containsKey("ShowUI")) {
            showUI = true
        }

        // only continue, when there are known parameters in the request
        if (emptyParms || !(tokenUrl || status || showUI)) {
            response = BindingResult(BindingResultCode.MISSING_PARAMETER)
            response.setResultMessage(lang.translationForKey(ErrorTranslations.NO_ACTIVATION_PARAMETERS))
            setMinorResult(response, ECardConstants.Minor.App.INCORRECT_PARM)
            showErrorMessage(lang.translationForKey(ErrorTranslations.NO_ACTIVATION_PARAMETERS))
            return response
        }

        // check illegal parameter combination
        if ((tokenUrl && showUI) || (tokenUrl && status) || (showUI && status)) {
            response = BindingResult(BindingResultCode.WRONG_PARAMETER)
            response.setResultMessage(lang.translationForKey(ErrorTranslations.NO_PARAMS))
            setMinorResult(response, ECardConstants.Minor.App.INCORRECT_PARM)
            showErrorMessage(lang.translationForKey(ErrorTranslations.NO_PARAMS))
            return response
        }

        return processRequest(body, params, headers, attachments, tokenUrl, showUI, status)
    }

    /**
     * Process the request.
     *
     * @param body Body of the request.
     * @param params Query parameters of the request.
     * @param attachments Attachments of the request.
     * @param tokenUrl `TRUE` if `params` contains a TCTokenURL.
     * @param showUI `TRUE` if `params` contains a ShowUI parameter.
     * @param status `TRUE` if `params` contains a Status parameter.
     * @return A [BindingResult] representing the result of the request processing.
     */
    private fun processRequest(
        body: RequestBody?, params: MutableMap<String?, String?>, headers: Headers?,
        attachments: MutableList<Attachment?>?, tokenUrl: Boolean, showUI: Boolean, status: Boolean
    ): BindingResult {
        var response: BindingResult

        if (status) {
            response = processStatus(body, params, headers, attachments)
            return response
        }

        if (SEMAPHORE.tryAcquire()) {
            try {
                if (tokenUrl) {
                    response = processTcToken(params)
                    return response
                }

                if (showUI) {
                    val requestedUI = params.get("ShowUI")
                    response = processShowUI(requestedUI)
                    return response
                }
            } finally {
                SEMAPHORE.release()
            }
        } else {
            response = BindingResult(BindingResultCode.RESOURCE_LOCKED)
            response.setResultMessage("An authentication process is already running.")
            return response
        }

        response = BindingResult(BindingResultCode.INTERNAL_ERROR)
        response.setResultMessage("Failed to handle request parameters correctly.")
        return response
    }

    /**
     * Open the requested UI if no supported UI element is stated the default view is opened.
     *
     * @param requestedUI String containing the name of the UI component to open. Currently supported UI components are
     * `Settings` and `PINManagement`. All other values are ignored and the default view is opened also if
     * the value is null.
     * @return A [BindingResult] object containing [BindingResultCode.OK] because the UIs do not return
     * results.
     */
    private fun processShowUI(requestedUI: String?): BindingResult {
        val response: BindingResult
        if (requestedUI != null) {
            when (requestedUI) {
                "Settings" -> response = processShowSettings()
                "PINManagement" -> response = processShowPinManagement()
                else -> response = processShowDefault()
            }
        } else {
            // open default gui
            response = processShowDefault()
        }

        return response
    }

    /**
     * Display the default view of the Open eCard App.
     *
     * There is no real default view that's a term used by the eID-Client specification BSI-TR-03124-1 v1.2 so we display
     * the About dialog.
     *
     * @return A [BindingResult] object containing [BindingResultCode.OK] because the gui does not return any
     * result.
     */
    private fun processShowDefault(): BindingResult {
        val defautlViewThread = Thread(Runnable { settingsAndDefaultView!!.showDefaultViewUI() }, "ShowDefaultView")
        defautlViewThread.start()
        return BindingResult(BindingResultCode.OK)
    }

    /**
     * Opens the PINManagement dialog.
     *
     * @return A [BindingResult] object containing [BindingResultCode.OK] because the gui does not return any
     * result.
     */
    private fun processShowPinManagement(): BindingResult {
        // submit thread
        val es =
            Executors.newSingleThreadExecutor(ThreadFactory { action: Runnable -> Thread(action, "ShowPINManagement") })
        val guiThread = es.submit<Void?>(Callable {
            pinManAction!!.execute()
            null
        })

        try {
            guiThread.get()
            return BindingResult(BindingResultCode.OK)
        } catch (ex: InterruptedException) {
            guiThread.cancel(true)
            val result = createInterruptedResult()
            return result
        } catch (ex: ExecutionException) {
            val cause = ex.cause
            if (cause is WSHelper.WSException) {
                val appEx = cause
                val result = appEx.result
                if (minorIsOneOf(
                        result,
                        ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
                        ECardConstants.Minor.IFD.CANCELLATION_BY_USER
                    )
                ) {
                    LOG.info("PIN Management got cancelled.")
                    return asBindingResult(BindingResultCode.INTERRUPTED, result)
                } else if (minorIsOneOf(
                        result, ECardConstants.Minor.IFD.Terminal.WAIT_FOR_DEVICE_TIMEOUT,
                        ECardConstants.Minor.IFD.TIMEOUT_ERROR
                    )
                ) {
                    LOG.info("PIN Management could not wait for a device any longer.")
                    return asBindingResult(BindingResultCode.TIMEOUT, result)
                } else if (minorIsOneOf(result, ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
                    LOG.info("PIN Management lost access via the defining slot.")

                    return asBindingResult(BindingResultCode.INTERRUPTED, result)
                } else {
                    LOG.warn("PIN Management completed with an unknown/internal error.", ex)
                    return asBindingResult(BindingResultCode.INTERNAL_ERROR, result)
                }
            } else if (cause is ThreadTerminateException) {
                return createInterruptedResult()
            }

            // just count as normal error
            LOG.warn("Failed to execute PIN Management.", ex)
            return BindingResult(BindingResultCode.INTERNAL_ERROR)
        } finally {
            // clean up executor
            es.shutdown()
        }
    }

    private fun createInterruptedResult(): BindingResult {
        return asBindingResult(BindingResultCode.INTERRUPTED, ECardConstants.Minor.SAL.CANCELLATION_BY_USER)
    }

    private fun asBindingResult(code: BindingResultCode, source: Result): BindingResult {
        val result = BindingResult(code)
        setMinorResult(result, source.getResultMinor())
        return result
    }

    private fun asBindingResult(code: BindingResultCode, minor: String?): BindingResult {
        val result = BindingResult(code)
        setMinorResult(result, minor)
        return result
    }

    private fun setMinorResult(result: BindingResult, minorReason: String?) {
        result.auxResultData.put(AuxDataKeys.MINOR_PROCESS_RESULT, minorReason)
    }

    /**
     * Opens the Settings dialog.
     *
     * @return A [BindingResult] object containing [BindingResultCode.OK] because the gui does not return any
     * result.
     */
    private fun processShowSettings(): BindingResult {
        val settingsThread = Thread(Runnable { settingsAndDefaultView!!.showSettingsUI() }, "ShowSettings")
        settingsThread.start()
        return BindingResult(BindingResultCode.OK)
    }

    /**
     * Gets a BindingResult object containing the current status of the client.
     *
     * @param body Original RequestBody.
     * @param params Original Parameters.
     * @param attachments Original list of Attachment object.
     * @return A [BindingResult] object containing the current status of the App as XML structure.
     */
    private fun processStatus(
        body: RequestBody?,
        params: MutableMap<String?, String?>?,
        headers: Headers?,
        attachments: MutableList<Attachment?>?
    ): BindingResult {
        val response = statusAction!!.execute(body, params, headers, attachments, null)
        return response
    }

    /**
     * Process the tcTokenURL or the activation object and perform a authentication.
     *
     * @param params Parameters of the request.
     * @return A [BindingResult] representing the result of the authentication.
     */
    private fun processTcToken(params: MutableMap<String?, String?>?): BindingResult {
        var response: BindingResult
        val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
        dynCtx.put(TR03112Keys.COOKIE_MANAGER, CookieManager())

        try {
            try {
                response = tokenHandler!!.handleActivate(params, ctx)
                // Show success message. If we get here we have a valid StartPAOSResponse and a valid refreshURL
                showFinishMessage((response as org.openecard.binding.tctoken.TCTokenResponse?)!!)
            } catch (ex: ActivationError) {
                if (ex is NonGuiException) {
                    // error already displayed to the user so do not repeat it here
                } else {
                    if (ex.message == "Invalid HTTP message received.") {
                        showErrorMessage(lang.translationForKey(ErrorTranslations.ACTIVATION_INVALID_REFRESH_ADDRESS))
                    } else {
                        showErrorMessage(ex.getLocalizedMessage())
                    }
                }
                LOG.error(ex.message)
                LOG.debug(ex.message, ex) // stack trace only in debug level
                LOG.debug("Returning result: \n{}", ex.getBindingResult())
                if (ex is FatalActivationError) {
                    LOG.info("Authentication failed, displaying error in Browser.")
                } else {
                    LOG.info("Authentication failed, redirecting to with errors attached to the URL.")
                }
                response = ex.getBindingResult()
            }
        } catch (e: RuntimeException) {
            if (e is ThreadTerminateException) {
                response = this.createInterruptedResult()
            } else {
                response = BindingResult(BindingResultCode.INTERNAL_ERROR)
            }
            LOG.error(e.message, e)
        }

        return response
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(ActivateAction::class.java)
        private val SEMAPHORE = Semaphore(1)
    }
}
