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
 ***************************************************************************/

package org.openecard.addons.activate;

import org.openecard.binding.tctoken.ex.ActivationError;
import org.openecard.binding.tctoken.ex.FatalActivationError;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppExtensionException;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Headers;
import org.openecard.addon.bind.RequestBody;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.binding.tctoken.TCTokenHandler;
import org.openecard.binding.tctoken.TCTokenRequest;
import org.openecard.binding.tctoken.TCTokenResponse;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.binding.tctoken.ex.NonGuiException;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.OpenecardProperties;
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.gui.definition.ViewController;
import org.openecard.common.DynamicContext;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.transport.httpcore.cookies.CookieManager;
import org.openecard.common.interfaces.Dispatcher;


/**
 * Implementation of a plugin action performing a client activation with a TCToken.
 *
 * @author Dirk Petrautzki
 * @author Benedikt Biallowons
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class ActivateAction implements AppPluginAction {

    private static final Logger LOG = LoggerFactory.getLogger(ActivateAction.class);
    private static final Semaphore SEMAPHORE = new Semaphore(1);

    private final I18n lang = I18n.getTranslation("tr03112");

    private TCTokenHandler tokenHandler;
    private AppPluginAction statusAction;
    private AppExtensionAction pinManAction;
    private UserConsent gui;
    private AddonManager manager;
    private ViewController settingsAndDefaultView;
    private Dispatcher dispatcher;
    private Context ctx;

    @Override
    public void init(Context ctx) {
	tokenHandler = new TCTokenHandler(ctx);
	this.ctx = ctx;
	gui = ctx.getUserConsent();
	dispatcher = ctx.getDispatcher();
	manager = ctx.getManager();
	settingsAndDefaultView = ctx.getViewController();
	try {
	    AddonSpecification addonSpecStatus = manager.getRegistry().search("Status");
	    statusAction = manager.getAppPluginAction(addonSpecStatus, "getStatus");
	    AddonSpecification addonSpecPinMngmt = manager.getRegistry().search("PIN-Plugin");
	    pinManAction = manager.getAppExtensionAction(addonSpecPinMngmt, "GetCardsAndPINStatusAction");
	} catch (AddonNotFoundException ex) {
	    // this should never happen because the status and pin plugin are always available
	    String msg = "Failed to get Status or PIN Plugin.";
	    LOG.error(msg, ex);
	    throw new RuntimeException(msg, ex);
	}
    }

    @Override
    public void destroy() {
	tokenHandler = null;
	manager.returnAppPluginAction(statusAction);
	manager.returnAppExtensionAction(pinManAction);
    }

    @Override
    public BindingResult execute(RequestBody body, Map<String, String> params, Headers headers, List<Attachment> attachments) {
	BindingResult response;

	try {
	    if (SEMAPHORE.tryAcquire()) {
		response = checkRequestParameters(body, params, headers, attachments);
	    } else {
		response = new BindingResult(BindingResultCode.RESOURCE_LOCKED);
		response.setResultMessage("An authentication process is already running.");
	    }
	} finally {
	    SEMAPHORE.release();
	    // in some cases an error does not lead to a removal of the dynamic context so remove it here
	    DynamicContext.remove();
	}

	return response;
    }

    private boolean isShowRemoveCard() {
	String str = OpenecardProperties.getProperty("notification.omit_show_remove_card");
	return ! Boolean.valueOf(str);
    }

    /**
     * Use the {@link UserConsent} to display the success message.
     */
    private void showFinishMessage(TCTokenResponse response) {
	// show the finish message just if we have a major ok
	if (ECardConstants.Major.OK.equals(response.getResult().getResultMajor()) && isShowRemoveCard()) {
	    String title = lang.translationForKey(FINISH_TITLE);
	    String msg = lang.translationForKey(REMOVE_CARD);
	    showBackgroundMessage(msg, title, DialogType.INFORMATION_MESSAGE);
	}
    }

    /**
     * Display a dialog in a separate thread.
     *
     * @param msg The message which shall be displayed.
     * @param title Title of the dialog window.
     * @param dialogType Type of the dialog.
     */
    private void showBackgroundMessage(final String msg, final String title, final DialogType dialogType) {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		gui.obtainMessageDialog().showMessageDialog(msg, title, dialogType);
	    }
	}, "Background_MsgBox").start();
    }

    /**
     * Use the {@link UserConsent} to display the given error message.
     *
     * @param errMsg Error message to display.
     */
    private void showErrorMessage(String errMsg) {
	String title = lang.translationForKey(ERROR_TITLE);
	String baseHeader = lang.translationForKey(ERROR_HEADER);
	String exceptionPart = lang.translationForKey(ERROR_MSG_IND);
	String removeCard = lang.translationForKey(REMOVE_CARD);
	String msg = String.format("%s\n\n%s\n%s\n\n%s", baseHeader, exceptionPart, errMsg, removeCard);
	showBackgroundMessage(msg, title, DialogType.ERROR_MESSAGE);
    }

    /**
     * Check the request for correct parameters and invoke their processing if they are ok.
     *
     * @param body The body of the request.
     * @param params The query parameters and their values.
     * @param attachments Attachments of the request.
     * @return A {@link BindingResult} with an error if the parameters are not correct or one depending on the processing
     * of the parameters.
     */
    private BindingResult checkRequestParameters(RequestBody body, Map<String, String> params, Headers headers,
	    List<Attachment> attachments) {
	BindingResult response;
	boolean emptyParms, tokenUrl, status, showUI;
	emptyParms = tokenUrl = status = showUI = false;

	if (params.isEmpty()) {
	    emptyParms = true;
	}

	if (params.containsKey("tcTokenURL")) {
	    tokenUrl = true;
	}

	if (params.containsKey("Status")) {
	    status = true;
	}

	if (params.containsKey("ShowUI")) {
	    showUI = true;
	}

	// only continue, when there are known parameters in the request
	if (emptyParms || !(tokenUrl || status || showUI)) {
	    response = new BindingResult(BindingResultCode.MISSING_PARAMETER);
	    response.setResultMessage(lang.translationForKey(NO_ACTIVATION_PARAMETERS));
	    showErrorMessage(lang.translationForKey(NO_ACTIVATION_PARAMETERS));
	    return response;
	}

	// check illegal parameter combination
	if ((tokenUrl && showUI) || (tokenUrl && status) || (showUI && status)) {
	    response = new BindingResult(BindingResultCode.WRONG_PARAMETER);
	    response.setResultMessage(lang.translationForKey(NO_PARAMS));
	    showErrorMessage(lang.translationForKey(NO_PARAMS));
	    return response;
	}

	return processRequest(body, params, headers, attachments, tokenUrl, showUI, status);
    }

    /**
     * Process the request.
     *
     * @param body Body of the request.
     * @param params Query parameters of the request.
     * @param attachments Attachments of the request.
     * @param tokenUrl {@code TRUE} if {@code params} contains a TCTokenURL.
     * @param showUI {@code TRUE} if {@code params} contains a ShowUI parameter.
     * @param status {@code TRUE} if {@code params} contains a Status parameter.
     * @return A {@link BindingResult} representing the result of the request processing.
     */
    private BindingResult processRequest(RequestBody body, Map<String, String> params, Headers headers,
	    List<Attachment> attachments, boolean tokenUrl, boolean showUI, boolean status) {
	BindingResult response = null;

	if (tokenUrl) {
	    response = processTcToken(params);
	    return response;
	}

	if (status) {
	    response = processStatus(body, params, headers, attachments);
	    return response;
	}

	if (showUI) {
	    String requestedUI = params.get("ShowUI");
	    response = processShowUI(requestedUI);
	    return response;
	}
	return response;
    }

    /**
     * Open the requested UI if no supported UI element is stated the default view is opened.
     *
     * @param requestedUI String containing the name of the UI component to open. Currently supported UI components are
     * {@code Settings} and {@code PINManagement}. All other values are ignored and the default view is opened also if
     * the value is null.
     * @return A {@link BindingResult} object containing {@link BindingResultCode#OK} because the UIs do not return
     * results.
     */
    private BindingResult processShowUI(String requestedUI) {
	BindingResult response;
	if (requestedUI != null) {
	    switch (requestedUI) {
		case "Settings":
		    response = processShowSettings();
		    break;
		case "PINManagement":
		    response = processShowPinManagement();
		    break;
		default:
		    response = processShowDefault();
	    }
	} else {
	    // open default gui
	    response = processShowDefault();
	}

	return response;
    }

    /**
     * Display the default view of the Open eCard App.
     *
     * There is no real default view that's a term used by the eID-Client specification BSI-TR-03124-1 v1.2 so we display
     * the About dialog.
     *
     * @return A {@link BindingResult} object containing {@link BindingResultCode#OK} because the gui does not return any
     * result.
     */
    private BindingResult processShowDefault() {
	Thread defautlViewThread = new Thread(new Runnable() {
	    @Override
	    public void run() {
		settingsAndDefaultView.showDefaultViewUI();
	    }
	}, "ShowDefaultView");
	defautlViewThread.start();
	return new BindingResult(BindingResultCode.OK);
    }

    /**
     * Opens the PINManagement dialog.
     *
     * @return A {@link BindingResult} object containing {@link BindingResultCode#OK} because the gui does not return any
     * result.
     */
    private BindingResult processShowPinManagement() {
	// submit thread
	ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory() {
	    @Override
	    public Thread newThread(Runnable action) {
		return new Thread(action, "ShowPINManagement");
	    }
	});
	Future<Void> guiThread = es.submit(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		pinManAction.execute();
		return null;
	    }
	});

	try {
	    guiThread.get();
	    return new BindingResult(BindingResultCode.OK);
	} catch (InterruptedException ex) {
	    guiThread.cancel(true);
	    return new BindingResult(BindingResultCode.INTERRUPTED);
	} catch (ExecutionException ex) {
	    Throwable cause = ex.getCause();
	    if (cause instanceof AppExtensionException) {
		AppExtensionException appEx = (AppExtensionException) cause;
		if (WSHelper.minorIsOneOf(appEx, ECardConstants.Minor.SAL.CANCELLATION_BY_USER,
			ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		    LOG.info("PIN Management got cancelled.");
		    return new BindingResult(BindingResultCode.INTERRUPTED);
		}
	    } else if (cause instanceof ThreadTerminateException) {
		return new BindingResult(BindingResultCode.INTERRUPTED);
	    }

	    // just count as normal error
	    LOG.warn("Failed to execute PIN Management.", ex);
	    return new BindingResult(BindingResultCode.INTERNAL_ERROR);
	} finally {
	    // clean up executor
	    es.shutdown();
	}
    }

    /**
     * Opens the Settings dialog.
     *
     * @return A {@link BindingResult} object containing {@link BindingResultCode#OK} because the gui does not return any
     * result.
     */
    private BindingResult processShowSettings() {
	Thread settingsThread = new Thread(new Runnable() {
	    @Override
	    public void run() {
		settingsAndDefaultView.showSettingsUI();
	    }
	}, "ShowSettings");
	settingsThread.start();
	return new BindingResult(BindingResultCode.OK);
    }

    /**
     * Gets a BindingResult object containing the current status of the client.
     *
     * @param body Original RequestBody.
     * @param params Original Parameters.
     * @param attachments Original list of Attachment object.
     * @return A {@link BindingResult} object containing the current status of the App as XML structure.
     */
    private BindingResult processStatus(RequestBody body, Map<String, String> params, Headers headers, List<Attachment> attachments) {
	BindingResult response = statusAction.execute(body, params, headers, attachments);
	return response;
    }

    /**
     * Process the tcTokenURL or the activation object and perform a authentication.
     *
     * @param params Parameters of the request.
     * @return A {@link BindingResult} representing the result of the authentication.
     */
    private BindingResult processTcToken(Map<String, String> params) {
	BindingResult response;
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	dynCtx.put(TR03112Keys.COOKIE_MANAGER, new CookieManager());
	
	try {
	    TCTokenRequest tcTokenRequest = null;
	    try {
		tcTokenRequest = TCTokenRequest.convert(params, ctx);
		response = tokenHandler.handleActivate(tcTokenRequest);
		// Show success message. If we get here we have a valid StartPAOSResponse and a valid refreshURL
		showFinishMessage((TCTokenResponse) response);
	    } catch (ActivationError ex) {
		if (ex instanceof NonGuiException) {
		    // error already displayed to the user so do not repeat it here
		} else {
		    if (ex.getMessage().equals("Invalid HTTP message received.")) {
			showErrorMessage(lang.translationForKey(ACTIVATION_INVALID_REFRESH_ADDRESS));
		    } else {
			showErrorMessage(ex.getLocalizedMessage());
		    }
		}
		LOG.error(ex.getMessage());
		LOG.debug(ex.getMessage(), ex); // stack trace only in debug level
		LOG.debug("Returning result: \n{}", ex.getBindingResult());
		if (ex instanceof FatalActivationError) {
		    LOG.info("Authentication failed, displaying error in Browser.");
		} else {
		    LOG.info("Authentication failed, redirecting to with errors attached to the URL.");
		}
		response = ex.getBindingResult();
	    } finally {
		if (tcTokenRequest != null && tcTokenRequest.getTokenContext() != null) {
		    // close connection to tctoken server in case PAOS didn't already perform this action
		    tcTokenRequest.getTokenContext().closeStream();
		}
	    }
	} catch (RuntimeException e) {

	    if(e instanceof ThreadTerminateException){
		response = new BindingResult(BindingResultCode.INTERRUPTED);
	    } else {
		response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    }
	    LOG.error(e.getMessage(), e);
	}

	return response;
    }

}
