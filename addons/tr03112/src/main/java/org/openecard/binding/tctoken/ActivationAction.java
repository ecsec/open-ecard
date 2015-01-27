/****************************************************************************
 * Copyright (C) 2013-2014 HS Coburg.
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

package org.openecard.binding.tctoken;

import org.openecard.binding.tctoken.ex.ActivationError;
import org.openecard.binding.tctoken.ex.FatalActivationError;
import java.util.List;
import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.RequestBody;
import org.openecard.binding.tctoken.ex.NonGuiException;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.OpenecardProperties;
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;


/**
 * Implementation of a plugin action performing a client activation with a TCToken.
 *
 * @author Dirk Petrautzki
 * @author Benedikt Biallowons
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class ActivationAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(ActivationAction.class);

    private final I18n lang = I18n.getTranslation("tr03112");

    private TCTokenHandler tokenHandler;
    private UserConsent gui;

    @Override
    public void init(Context ctx) {
	tokenHandler = new TCTokenHandler(ctx);
	gui = ctx.getUserConsent();
    }

    @Override
    public void destroy() {
	tokenHandler = null;
    }

    @Override
    public BindingResult execute(RequestBody body, Map<String, String> params, List<Attachment> attachments) {
	BindingResult response;

	// only continue, when there are known parameters in the request
	if (! (params.isEmpty() || params.containsKey("tcTokenURL") || params.containsKey("activationObject"))) {
	    response = new BindingResult(BindingResultCode.MISSING_PARAMETER);
	    response.setResultMessage("A parameters containing the activation information is missing.");
	    return response;
	}

	try {
	    TCTokenRequest tcTokenRequest = null;
	    try {
		tcTokenRequest = TCTokenRequest.convert(params);
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
		logger.error(ex.getMessage());
		logger.debug(ex.getMessage(), ex); // stack trace only in debug level
		logger.debug("Returning result: \n{}", ex.getBindingResult());
		if (ex instanceof FatalActivationError) {
		    logger.info("Authentication failed, displaying error in Browser.");
		} else {
		    logger.info("Authentication failed, redirecting to with errors attached to the URL.");
		}
		response = ex.getBindingResult();
	    } finally {
		if (tcTokenRequest != null && tcTokenRequest.getTokenContext() != null) {
		    // close connection to tctoken server in case PAOS didn't already perform this action
		    tcTokenRequest.getTokenContext().closeStream();
		}
	    }
	} catch (RuntimeException e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
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

    private void showBackgroundMessage(final String msg, final String title, final DialogType dialogType) {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		gui.obtainMessageDialog().showMessageDialog(msg, title, dialogType);
	    }
	}, "Background_MsgBox").start();
    }

    private void showErrorMessage(String errMsg) {
	String title = lang.translationForKey(ERROR_TITLE);
	String baseHeader = lang.translationForKey(ERROR_HEADER);
	String exceptionPart = lang.translationForKey(ERROR_MSG_IND);
	String removeCard = lang.translationForKey(REMOVE_CARD);
	String msg = String.format("%s\n\n%s\n%s\n\n%s", baseHeader, exceptionPart, errMsg, removeCard);
	showBackgroundMessage(msg, title, DialogType.ERROR_MESSAGE);
    }

}
