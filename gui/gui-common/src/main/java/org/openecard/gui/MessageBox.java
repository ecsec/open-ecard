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

import java.util.List;
import org.openecard.gui.messagebox.DialogType;
import org.openecard.gui.messagebox.MessageBoxResult;
import org.openecard.gui.messagebox.OptionType;


/**
 * Interface for a generic message box.
 * This and the interfaces used in this definition are modeled after Swings {@link javax.swing.JOptionPane}. However it
 * should be abstract enough to fit under other implementations as well.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface MessageBox {

    /**
     * Brings up an information-message dialog with the default title of the implementation.
     *
     * @param message The message displayed in the box.
     * @return MessageBoxResult with no return value.
     */
    MessageBoxResult showMessage(String message);

    /**
     * Brings up a dialog that displays a message using the given title and a default icon determined by the messageType
     * parameter.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @return MessageBoxResult with no return value.
     */
    MessageBoxResult showMessage(String message, String title, DialogType messageType);

    /**
     * Brings up a dialog displaying a message, specifying all parameters.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @param iconData An icon to display in the dialog that helps the user identify the kind of message in the box.
     * @return MessageBoxResult with no return value.
     */
    MessageBoxResult showMessage(String message, String title, DialogType messageType, byte[] iconData);

    /**
     * Brings up a dialog with the options Yes, No and Cancel; with the internationalized version of the title 'Select
     * an Option'.
     *
     * @param message The message displayed in the dialog.
     * @return MessageBoxResult with a result value set conforming to the option selected by the user.
     */
    MessageBoxResult showConfirmDialog(String message);

    /**
     * Brings up a dialog with the given title where the number of choices is determined by the optionType parameter.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param optionType The option type of the dialog.
     * @return MessageBoxResult with a result value set conforming to the option selected by the user.
     */
    MessageBoxResult showConfirmDialog(String message, String title, OptionType optionType);

    /**
     * Brings up a dialog using the given title and a default icon determined by the messageType parameter where the
     * number of choices is determined by the optionType parameter.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param optionType The option type of the dialog.
     * @param messageType The type of message to be displayed.
     * @return MessageBoxResult with a result value set conforming to the option selected by the user.
     */
    MessageBoxResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType);

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
     * @return MessageBoxResult with a result value set conforming to the option selected by the user.
     */
    MessageBoxResult showConfirmDialog(String message, String title, OptionType optionType, DialogType messageType,
	    byte[] iconData);

    /**
     * Shows a question-message dialog requesting input from the user.
     *
     * @param message The message displayed in the dialog.
     * @return a MessageBoxResult with the user's input.
     */
    MessageBoxResult showInputDialog(String message);

    /**
     * Shows a question-message dialog requesting input from the user, with the input value initialized to
     * initialSelectionValue.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param initialSelectionValue The value to initialize the input field.
     * @return a MessageBoxResult with the user's input.
     */
    MessageBoxResult showInputDialog(String message, String initialSelectionValue);

    /**
     * Shows a dialog requesting input from the user with the dialog having the title title and message type
     * messageType.
     *
     * @param message The message displayed in the dialog.
     * @param title The title string of the dialog.
     * @param messageType The type of message to be displayed.
     * @return MessageBoxResult with the user's input.
     */
    MessageBoxResult showInputDialog(String message, String title, DialogType messageType);

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
     * @param selectionValues Nonempty list of Strings that gives the possible selections.
     * @param initialSelectionIndex Index of the initially selected value.
     * @return MessageBoxResult with the user's input.
     */
    MessageBoxResult showInputDialog(String message, String title, DialogType messageType, byte[] iconData,
	    List<String> selectionValues, int initialSelectionIndex);

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
     * @param initialSelectionIndex Index of the initially selected value.
     * @return MessageBoxResult with a result value set conforming to the option selected by the user.
     */
    MessageBoxResult showOptionDialog(String message, String title, OptionType optionType, DialogType messageType,
	    byte[] iconData, List<String> options, int initialSelectionIndex);

}
