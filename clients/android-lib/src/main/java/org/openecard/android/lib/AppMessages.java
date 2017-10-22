/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.lib;


/**
 * Contains standard messages.
 *
 * @author Mike Prechtl
 */
public interface AppMessages {

    String APP_RESPONSE_OK = "Initialization successfully finished.";
    String APP_CONTEXT_STD_MSG = "Initialization of App context failed.";
    String APP_API_LEVEL_21_NOT_SUPPORTED = "API Level below 21 not supported.";
    String APP_CONTEXT_ALREADY_INITIALIZED = "AppContext already initialized.";
    String APP_CONTEXT_NFC_NOT_AVAILABLE = "NFC not available.";
    String APP_CONTEXT_NFC_NOT_ENABLED = "NFC not enabled.";
    String APP_CONTEXT_UNABLE_TO_INITIALIZE_TF = "Unable to initialize terminal factory.";
    String APP_CONTEXT_ESTABLISH_CONTEXT_FAIL = "Establish IFD context failed.";
    String APP_CONTEXT_CARD_REC_FAILED = "Unable to initialize card recognition.";
    String APP_CONTEXT_ADD_ON_INIT_FAILED = "Registering of core add-ons failed.";
    String APP_TERMINATE_SUCCESS = "Successfully terminated.";
    String APP_TERMINATE_FAILURE = "Terminate failed, see LOG.";
    String APP_EAC_SERVICE_CONNECTED = "Successfully connected to Eac Gui Service.";
    String APP_EAC_SERVICE_DISCONNECTED = "Successfully disconnected from Eac Gui Service.";
    String BINDING_TASK_STILL_RUNNING = "There is already a running Binding Task. Maybe you have to cancel it.";
    String PLEASE_START_OPENECARD_SERVICE = "Please start the Open eCard Service.";
    String PLEASE_PROVIDE_CONTEXT_WRAPPER = "Please provide a ContextWrapper.";
    String CARD_NOT_PRESENT = "No NPA identity card present.";

}
