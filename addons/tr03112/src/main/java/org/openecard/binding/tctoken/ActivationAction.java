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
import org.openecard.addon.bind.Body;
import org.openecard.binding.tctoken.ex.NonGuiException;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.gui.UserConsent;
import org.openecard.gui.message.DialogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    // Translation constants
    private static final String ERROR_TITLE = "error";
    private static final String SUCCESS_TITLE = "success";
    private static final String SUCCESS_MSG = "success_msg";
    private static final String ERROR_HEADER = "err_header";
    private static final String ERROR_MSG_IND = "err_msg_indicator";
    private static final String ERROR_FOOTER = "err_footer";

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
    public BindingResult execute(Body body, Map<String, String> parameters, List<Attachment> attachments) {
	BindingResult response;
	try {
	    try {
		TCTokenRequest tcTokenRequest = TCTokenRequest.convert(parameters);
		response = tokenHandler.handleActivate(tcTokenRequest);
		// Show success message. If we get here we have a valid StartPAOSResponse and a valid refreshURL
		showSuccessMessage(response);
	    } catch (ActivationError ex) {
		if (ex instanceof NonGuiException) {
		    // error already displayed to the user so do not repeat it here
		} else {
		    gui.obtainMessageDialog().showMessageDialog(generateErrorMessage(ex.getMessage()), 
			    lang.translationForKey(ERROR_TITLE), DialogType.ERROR_MESSAGE);
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
	    }
	} catch (RuntimeException e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
	}
	return response;
    }

    /**
     * Use the {@link UserConsent} to display the success message.
     */
    private void showSuccessMessage(BindingResult response) {
	if (((TCTokenResponse) response).getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
            // Make translateable
            String msg = lang.translationForKey(SUCCESS_MSG);
            gui.obtainMessageDialog().showMessageDialog(msg, lang.translationForKey(SUCCESS_TITLE),
                    DialogType.INFORMATION_MESSAGE);
        }
    }

    private String generateErrorMessage(String errMsg) {
	String baseHeader = lang.translationForKey(ERROR_HEADER);
	String exceptionPart = lang.translationForKey(ERROR_MSG_IND);
	String baseFooter = lang.translationForKey(ERROR_FOOTER);
	String msg = baseHeader + exceptionPart + errMsg + baseFooter;
	return msg;
    }

}
