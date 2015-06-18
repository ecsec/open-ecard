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

import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;


/**
 * Interface for a generic message dialog.
 * This and the interfaces used in this definition are modeled after Swings {@link javax.swing.JOptionPane}. However it
 * should be abstract enough to fit under other implementations as well.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public interface MessageDialog {

    /**
     * Brings up an information-message dialog.
     *
     * @param message The message displayed in the box.
     * @param title The title string of the dialog.
     * @return MessageDialogResult with no return value.
     */
    MessageDialogResult showMessageDialog(String message, String title);

    /**
     * Brings up a dialog that displays a message using the given title and a default icon determined by the messageType
     * parameter.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @return MessageDialogResult with no return value.
     */
    MessageDialogResult showMessageDialog(String message, String title, DialogType messageType);

    /**
     * Brings up a dialog displaying a message, specifying all parameters.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @param iconData An icon to display in the dialog that helps the user identify the kind of message in the box.
     * @return MessageDialogResult with no return value.
     */
    MessageDialogResult showMessageDialog(String message, String title, DialogType messageType, byte[] iconData);

    /**
     * Brings up a dialog with the options Yes, No and Cancel; with the internationalized version of the title 'Select
     * an Option'.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @return MessageDialogResult with a result value set conforming to the option selected by the user.
     */
    MessageDialogResult showConfirmDialog(String message, String title);

    /**
     * Brings up a dialog with the given title where the number of choices is determined by the optionType parameter.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param optionType The option type of the dialog.
     * @return MessageDialogResult with a result value set conforming to the option selected by the user.
     */
    MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType);

    /**
     * Brings up a dialog using the given title and a default icon determined by the messageType parameter where the
     * number of choices is determined by the optionType parameter.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param optionType The option type of the dialog.
     * @param messageType The type of message to be displayed.
     * @return MessageDialogResult with a result value set conforming to the option selected by the user.
     */
    MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType);

    /**
     * Brings up a dialog using the given title and a specified icon, where the number of choices is determined by the
     * optionType parameter.
     * The messageType parameter is primarily used to supply a default icon from the
     * implementation.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param optionType The option type of the dialog.
     * @param messageType The type of message to be displayed.
     * @param iconData An icon to display in the dialog that helps the user identify the kind of message in the box.
     * @return MessageDialogResult with a result value set conforming to the option selected by the user.
     */
    MessageDialogResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType,
	    byte[] iconData);

    /**
     * Shows a question-message dialog requesting input from the user.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @return a MessageDialogResult with the user's input.
     */
    MessageDialogResult showInputDialog(String message, String title);

    /**
     * Shows a question-message dialog requesting input from the user, with the input value initialized to
     * initialSelectionValue.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param initialValue The value to initialize the input field.
     * @return a MessageDialogResult with the user's input.
     */
    MessageDialogResult showInputDialog(String message, String title, String initialValue);

    /**
     * Shows a dialog requesting input from the user with the dialog having the title title and message type
     * messageType.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @param initialValue The value to initialize the input field.
     * @return MessageDialogResult with the user's input.
     */
    MessageDialogResult showInputDialog(String message, String title, DialogType messageType, String initialValue);

    /**
     * Prompts the user for input where the initial selection, possible selections, and all other options can be
     * specified.
     * The user will be able to choose from selectionValues. initialSelectionValue is the initial value to prompt
     * the user with. It is up to the UI to decide how best to represent the selectionValues, but usually a ComboBox or
     * List will be used.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @param iconData An icon to display in the dialog that helps the user identify the kind of message in the box.
     * @param initialSelectionIndex Zero based index of the initially selected value.
     * @param selectionValues Nonempty list of Strings that gives the possible selections.
     * @return MessageDialogResult with the user's input.
     */
    MessageDialogResult showInputDialog(String message, String title, DialogType messageType, byte[] iconData,
	    int initialSelectionIndex, String... selectionValues);

    /**
     * Brings up a dialog with a specified icon, where the initial choice is determined by the initialValue parameter
     * and the number of choices is determined by the optionType parameter.
     * If optionType is YES_NO_OPTION, or YES_NO_CANCEL_OPTION and the options parameter is null, then the options are
     * supplied by the implementation.
     * The messageType parameter is primarily used to supply a default icon from the implementation.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog
     * @param optionType The option type of the dialog.
     * @param messageType The type of message to be displayed.
     * @param iconData An icon to display in the dialog that helps the user identify the kind of message int the box.
     * @param options Nonempty list of Strings that gives the possible options.
     * @return MessageDialogResult with a result value set conforming to the option selected by the user.
     */
    MessageDialogResult showOptionDialog(String message, String title, OptionType optionType, DialogType messageType,
	    byte[] iconData, String... options);

}
