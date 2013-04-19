/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.gui.messagebox;


/**
 * Result class of the message box.
 * This class holds the selection the user made and if applicable also the user's input.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class MessageBoxResult {

    private final String userInput;
    private final ReturnType returnValue;

    /**
     * Creates a result with the given user input. If the user input is null the returnValue will automatically be set
     * to {@code MessageBox.CANCEL}, otherwise it will be set to {@code MessageBox.OK}. Should be used if the user is
     * able to enter input into the message box.
     * 
     * @param userInput user input as obtained from the dialog, may be null to signal it was canceled
     */
    public MessageBoxResult(String userInput) {
	this.returnValue = (userInput == null ? ReturnType.CANCEL : ReturnType.OK);
	this.userInput = userInput;
    }

    /**
     * Creates a result with the given return value and sets the input to null. Should be used if the user is NOT able
     * to enter input into the message box.
     * 
     * @param returnValue return value as obtained from the dialog
     */
    public MessageBoxResult(ReturnType returnValue) {
	this.returnValue = returnValue;
	this.userInput = null;
    }

    /**
     * Returns the return value of the message box.
     *
     * @return an integer indicating the option selected by the user
     */
    public ReturnType getReturnValue() {
	return returnValue;
    }

    /**
     * Returns the user's input.
     * 
     * @return if the message box allowed the user to enter something and it was not canceled, this returns the
     *         user's input, otherwise null
     */
    public String getUserInput() {
	return userInput;
    }

}
