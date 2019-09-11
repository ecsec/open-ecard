/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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

package org.openecard.mobile.system;


/**
 * Contains standard messages which are used in service responses.
 *
 * @author Mike Prechtl
 */
public class ServiceMessages {

    public static final String SERVICE_RESPONSE_OK = "Initialization successfully finished.";
    public static final String SERVICE_RESPONSE_FAILED = "Initialization of App context failed.";
    public static final String BELOW_API_LEVEL_21_NOT_SUPPORTED = "API Level below 21 not supported.";
    public static final String SERVICE_ALREADY_INITIALIZED = "AppContext already initialized.";
    public static final String NO_NFC_CONTEXT = "Please provide NFC capabililties.";
    public static final String NO_APPLICATION_CONTEXT = "Please provide an Application Context.";
    public static final String NFC_NOT_AVAILABLE_FAIL = "NFC not available.";
    public static final String NFC_NOT_ENABLED_FAIL = "NFC not enabled.";
    public static final String NFC_NO_EXTENDED_LENGTH_SUPPORT = "Your smartphone device doesn't support NFC with extended length.";
    public static final String UNABLE_TO_INITIALIZE_TF = "Unable to initialize terminal factory.";
    public static final String ESTABLISH_IFD_CONTEXT_FAILED = "Establish IFD context failed.";
    public static final String CARD_REC_INIT_FAILED = "Unable to initialize card recognition.";
    public static final String ADD_ON_INIT_FAILED = "Registering of core add-ons failed.";
    public static final String SERVICE_TERMINATE_SUCCESS = "Successfully terminated.";
    public static final String SERVICE_TERMINATE_FAILURE = "Terminate failed, see LOG.";
    public static final String EAC_SERVICE_CONNECTED = "Successfully connected to Eac Gui Service.";
    public static final String EAC_SERVICE_DISCONNECTED = "Successfully disconnected from Eac Gui Service.";
    public static final String BINDING_TASK_STILL_RUNNING = "There is already a running Binding Task. Maybe you have to cancel it.";
    public static final String PLEASE_START_OPENECARD_SERVICE = "Please start the Open eCard Service.";
    public static final String PLEASE_PROVIDE_BINDING_RESULT_RECEIVER = "Please provide a receiver for the binding result.";
    public static final String CARD_NOT_PRESENT = "No NPA identity card present.";

}
