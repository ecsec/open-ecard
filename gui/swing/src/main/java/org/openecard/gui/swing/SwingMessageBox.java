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

package org.openecard.gui.swing;

import java.awt.Image;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.openecard.common.I18n;
import org.openecard.gui.MessageBox;
import org.openecard.gui.messagebox.DialogType;
import org.openecard.gui.messagebox.MessageBoxResult;
import org.openecard.gui.messagebox.OptionType;
import org.openecard.gui.messagebox.ReturnType;
import org.openecard.gui.swing.common.GUIDefaults;


/**
 * Swing based MessageBox implementation.
 * This implementation wraps the {@link JOptionPane} class.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingMessageBox implements MessageBox {

    private static final Image frameIcon = GUIDefaults.getImage("Frame.icon", 45, 45).getImage();
    private final I18n lang = I18n.getTranslation("gui");
    private final String DEFAULT_TITLE_INPUT;
    private final String DEFAULT_TITLE_MESSAGE;
    private final String DEFAULT_TITLE_CONFIRM;

    public SwingMessageBox() {
	DEFAULT_TITLE_INPUT = lang.translationForKey("default_title_input");
	DEFAULT_TITLE_MESSAGE = lang.translationForKey("default_title_message");
	DEFAULT_TITLE_CONFIRM = lang.translationForKey("default_title_confirm");
    }

    @Override
    public MessageBoxResult showMessage(String msg) {
	return showMessage(msg, DEFAULT_TITLE_MESSAGE, DialogType.INFORMATION_MESSAGE);
    }

    @Override
    public MessageBoxResult showMessage(String msg, String title, DialogType msgType) {
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType));
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageBoxResult(ReturnType.OK);
    }

    @Override
    public MessageBoxResult showMessage(String msg, String title, DialogType msgType, byte[] iconData) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.DEFAULT_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageBoxResult(ReturnType.OK);
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg) {
	return showConfirmDialog(msg, DEFAULT_TITLE_CONFIRM, OptionType.YES_NO_CANCEL_OPTION);
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg, String title, OptionType optionType) {
	return showConfirmDialog(msg, title, optionType, DialogType.QUESTION_MESSAGE);
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg, String title, OptionType optionType, DialogType msgType) {
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType));
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(ReturnType.CLOSED);
	} else {
	    return new MessageBoxResult(convertReturnType((Integer) returnValue));
	}
    }

    @Override
    public MessageBoxResult showConfirmDialog(String msg, String title, OptionType optionType, DialogType msgType,
	    byte[] iconData) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType), icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(ReturnType.CLOSED);
	} else {
	    return new MessageBoxResult(convertReturnType((Integer) returnValue));
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg) {
	return showInputDialog(msg, "");
    }

    @Override
    public MessageBoxResult showInputDialog(String msg, String initialSelectionValue) {
	JOptionPane jop = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(DEFAULT_TITLE_INPUT);
	dialog.setIconImage(frameIcon);
	jop.setInitialSelectionValue(initialSelectionValue);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageBoxResult((String) null);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg, String title, DialogType msgType) {
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageBoxResult((String) null);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showInputDialog(String msg, String title, DialogType msgType, byte[] iconData,
	    List<String> options, int initialSelectedIndex) {
	if (options.isEmpty()) {
	    throw new IllegalArgumentException("List of options must be given.");
	}
	if (initialSelectedIndex > options.size()) {
	    initialSelectedIndex = 0;
	}
	String initialValue = options.get(initialSelectedIndex);
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.OK_CANCEL_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	jop.setSelectionValues(options.toArray());
	jop.setInitialSelectionValue(initialValue);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if ("uninitializedValue".equals(returnValue) && ! options.contains("uninitializedValue")) {
	    return new MessageBoxResult(ReturnType.CANCEL);
	} else {
	    return new MessageBoxResult((String) returnValue);
	}
    }

    @Override
    public MessageBoxResult showOptionDialog(String msg, String title, OptionType optionType, DialogType msgType,
	    byte[] iconData, List<String> options, int initialSelectedIndex) {
	if (options.isEmpty()) {
	    throw new IllegalArgumentException("List of options must be given.");
	}
	if (initialSelectedIndex > options.size()) {
	    initialSelectedIndex = 0;
	}
	String initialValue = options.get(initialSelectedIndex);
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType), icon,
		options.toArray(), initialValue);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageBoxResult(ReturnType.CLOSED);
	}
	return new MessageBoxResult((String) returnValue);
    }


    private static int convertOptionType(OptionType optionType) {
	switch (optionType) {
	    case YES_NO_OPTION:        return 0;
	    case YES_NO_CANCEL_OPTION: return 1;
	    case OK_CANCEL_OPTION:     return 2;
	}
	throw new IllegalArgumentException();
    }

    private static int convertDialogType(DialogType dialogType) {
	switch (dialogType) {
	    case ERROR_MESSAGE:       return 0;
	    case INFORMATION_MESSAGE: return 1;
	    case WARNING_MESSAGE:     return 2;
	    case QUESTION_MESSAGE:    return 3;
	    case PLAIN_MESSAGE:       return -1;
	}
	throw new IllegalArgumentException();
    }

    private static ReturnType convertReturnType(int returnValue) {
	switch (returnValue) {
	    case 0:  return ReturnType.OK;
	    case 1:  return ReturnType.NO;
	    case 2:  return ReturnType.CANCEL;
	    default: return ReturnType.CLOSED;
	}
    }

}
