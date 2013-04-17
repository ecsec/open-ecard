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

package org.openecard.gui;

import org.openecard.gui.messagebox.MessageBoxResult;


/**
 * Interface for a generic message box.
 * This and the interfaces used in this definition are modeled after Swings {@link javax.swing.JOptionPane}. However it
 * should be abstract enough to fit under other implementations as well.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public interface MessageBox {

    //
    // Option types
    //
    /** Type used for <code>showConfirmDialog</code>. */
    int YES_NO_OPTION = 0;
    /** Type used for <code>showConfirmDialog</code>. */
    int YES_NO_CANCEL_OPTION = 1;
    /** Type used for <code>showConfirmDialog</code>. */
    int OK_CANCEL_OPTION = 2;

    //
    // Message types. Used by the UI to determine what icon to display,
    // and possibly what behavior to give based on the type.
    //
    /** Used for error messages. */
    int ERROR_MESSAGE = 0;
    /** Used for information messages. */
    int INFORMATION_MESSAGE = 1;
    /** Used for warning messages. */
    int WARNING_MESSAGE = 2;
    /** Used for questions. */
    int QUESTION_MESSAGE = 3;
    /** No icon is used. */
    int PLAIN_MESSAGE = -1;

    //
    // Return values.
    //
    /** Return value if YES is chosen. */
    int YES = 0;
    /** Return value if NO is chosen. */
    int NO = 1;
    /** Return value if CANCEL is chosen. */
    int CANCEL = 2;
    /** Return value if OK is chosen. */
    int OK = 0;
    /**
     * Return value if user closes window without selecting anything, more than likely this should be treated as either
     * a <code>CANCEL_OPTION</code> or <code>NO_OPTION</code>.
     */
    int CLOSED = -1;


    /**
     * Brings up an information-message dialog with the default title of the implementation.
     *
     * @param message information-message to display
     * @return a {@link MessageBoxResult} with return value set to {@code OK}
     */
    MessageBoxResult showMessage(String message);

    /**
     * Brings up a dialog that displays a message using the given title and a default icon determined by the messageType
     * parameter.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @return a {@link MessageBoxResult} with return value set to {@code MessageBox.OK}
     */
    MessageBoxResult showMessage(String message, String title, int messageType);

    /**
     * Brings up a dialog displaying a message, specifying all parameters.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @param iconData an icon to display in the dialog that helps the user identify the kind of message that is being
     *     displayed
     * @return a {@link MessageBoxResult} with return value set to {@code MessageBox.OK}
     */
    MessageBoxResult showMessage(String message, String title, int messageType, byte[] iconData);

    /**
     * Brings up a dialog with the options Yes, No and Cancel; with the internationalized version of the title 'Select
     * an Option'.
     *
     * @param message information-message to display
     * @return a {@link MessageBoxResult} with a result value set conforming to the option selected by the user
     */
    MessageBoxResult showConfirmDialog(String message);

    /**
     * Brings up a dialog with the given title where the number of choices is determined by the optionType parameter.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param optionType an int designating the options available on the dialog: {@code YES_NO_OPTION},
     *     {@code YES_NO_CANCEL_OPTION}, or {@code OK_CANCEL_OPTION}
     * @return a {@link MessageBoxResult} with a result value set conforming to the option selected by the user
     */
    MessageBoxResult showConfirmDialog(String message, String title, int optionType);

    /**
     * Brings up a dialog using the given title and a default icon determined by the messageType parameter where the
     * number of choices is determined by the optionType parameter.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param optionType an int designating the options available on the dialog: {@code YES_NO_OPTION},
     *     {@code YES_NO_CANCEL_OPTION}, or {@code OK_CANCEL_OPTION}
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @return a {@link MessageBoxResult} with a result value set conforming to the option selected by the user
     */
    MessageBoxResult showConfirmDialog(String message, String title, int optionType, int messageType);

    /**
     * Brings up a dialog using the given title and a specified icon, where the number of choices is determined by the
     * optionType parameter. The messageType parameter is primarily used to supply a default icon from the
     * implementation.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param optionType an int designating the options available on the dialog: {@code YES_NO_OPTION},
     *     {@code YES_NO_CANCEL_OPTION}, or {@code OK_CANCEL_OPTION}
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @param iconData an icon to display in the dialog that helps the user identify the kind of message that is being
     *     displayed
     * @return a {@link MessageBoxResult} with a result value set conforming to the option selected by the user
     */
    MessageBoxResult showConfirmDialog(String message, String title, int optionType, int messageType, byte[] iconData);

    /**
     * Shows a question-message dialog requesting input from the user.
     *
     * @param message information-message to display
     * @return a {@link MessageBoxResult} with the user's input
     */
    MessageBoxResult showInputDialog(String message);

    /**
     * Shows a question-message dialog requesting input from the user, with the input value initialized to
     * initialSelectionValue.
     *
     * @param message information-message to display
     * @param initialSelectionValue the value used to initialize the input field
     * @return a {@link MessageBoxResult} with the user's input
     */
    MessageBoxResult showInputDialog(String message, String initialSelectionValue);

    /**
     * Shows a dialog requesting input from the user with the dialog having the title title and message type
     * messageType.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @return a {@link MessageBoxResult} with the user's input
     */
    MessageBoxResult showInputDialog(String message, String title, int messageType);

    /**
     * Prompts the user for input where the initial selection, possible selections, and all other
     * options can be specified. The user will be able to choose from selectionValues. initialSelectionValue is the
     * initial value to prompt the user with. It is up to the UI to decide how best to represent the selectionValues,
     * but usually a ComboBox or List will be used.
     *
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @param iconData an icon to display in the dialog that helps the user identify the kind of message that is being
     * 	    displayed
     * @param selectionValues an array of Strings that gives the possible selections
     * @param initialSelectionValue the value used to initialize the input field
     * @return a {@link MessageBoxResult} with the user's input
     */
    MessageBoxResult showInputDialog(String message, String title, int messageType, byte[] iconData,
	    String[] selectionValues, String initialSelectionValue);

    /**
     * Brings up a dialog with a specified icon, where the initial choice is determined by the initialValue parameter
     * and the number of choices is determined by the optionType parameter.
     * If optionType is YES_NO_OPTION, or YES_NO_CANCEL_OPTION and the options parameter is null, then the options are
     * supplied by the implementation.
     * The messageType parameter is primarily used to supply a default icon from the implementation.
     * 
     * @param message information-message to display
     * @param title the title string for the dialog
     * @param optionType an int designating the options available on the dialog: {@code YES_NO_OPTION},
     *     {@code YES_NO_CANCEL_OPTION}, or {@code OK_CANCEL_OPTION}
     * @param messageType the type of message to be displayed: {@code ERROR_MESSAGE}, {@code INFORMATION_MESSAGE},
     *     {@code WARNING_MESSAGE}, {@code QUESTION_MESSAGE}, or {@code PLAIN_MESSAGE}
     * @param iconData an icon to display in the dialog that helps the user identify the kind of message that is being
     *     displayed
     * @param options an array of Strings that gives the possible selections
     * @param initialValue the string that represents the default selection for the dialog
     * @return a {@link MessageBoxResult} with a result value set conforming to the option selected by the user
     */
    MessageBoxResult showOptionDialog(String message, String title, int optionType, int messageType, byte[] iconData,
	    String[] options, String initialValue);

}
