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
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.message.DialogType;
import org.openecard.gui.message.MessageDialogResult;
import org.openecard.gui.message.OptionType;
import org.openecard.gui.message.ReturnType;
import org.openecard.gui.swing.common.GUIDefaults;


/**
 * Swing based MessageDialog implementation.
 * This implementation wraps the {@link JOptionPane} class.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingMessageDialog implements MessageDialog {

    private static final Image frameIcon = GUIDefaults.getImage("Frame.icon", 45, 45).getImage();

    @Override
    public MessageDialogResult showMessageDialog(String msg, String title) {
	return showMessageDialog(msg, title, DialogType.INFORMATION_MESSAGE);
    }

    @Override
    public MessageDialogResult showMessageDialog(String msg, String title, DialogType msgType) {
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType));
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showMessageDialog(String msg, String title, DialogType msgType, byte[] iconData) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.DEFAULT_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	return new MessageDialogResult(ReturnType.OK);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String msg, String title) {
	return showConfirmDialog(msg, title, OptionType.YES_NO_CANCEL_OPTION);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String msg, String title, OptionType optionType) {
	return showConfirmDialog(msg, title, optionType, DialogType.QUESTION_MESSAGE);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String msg, String title, OptionType optionType, DialogType msgType) {
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType));
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageDialogResult(ReturnType.CANCEL);
	} else {
	    return new MessageDialogResult(convertReturnType((Integer) returnValue));
	}
    }

    @Override
    public MessageDialogResult showConfirmDialog(String msg, String title, OptionType optionType, DialogType msgType,
	    byte[] iconData) {
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType), icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);

	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageDialogResult(ReturnType.CANCEL);
	} else {
	    return new MessageDialogResult(convertReturnType((Integer) returnValue));
	}
    }

    @Override
    public MessageDialogResult showInputDialog(String msg, String title) {
	return showInputDialog(msg, title, "");
    }

    @Override
    public MessageDialogResult showInputDialog(String msg, String title, String initialValue) {
	return showInputDialog(msg, title, DialogType.QUESTION_MESSAGE, initialValue);
    }

    @Override
    public MessageDialogResult showInputDialog(String msg, String title, DialogType msgType, String initialValue) {
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	jop.setInitialSelectionValue(initialValue);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if (returnValue == null) {
	    return new MessageDialogResult((String) null);
	} else {
	    return new MessageDialogResult((String) returnValue);
	}
    }

    @Override
    public MessageDialogResult showInputDialog(String msg, String title, DialogType msgType, byte[] iconData,
	    int initialSelectedIndex, String... options) {
	List<String> optionsList = Arrays.asList(options);
	if (optionsList.isEmpty()) {
	    throw new IllegalArgumentException("List of options must be given.");
	}
	if (initialSelectedIndex > optionsList.size()) {
	    initialSelectedIndex = 0;
	}
	String initialValue = optionsList.get(initialSelectedIndex);
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.OK_CANCEL_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	jop.setSelectionValues(options);
	jop.setInitialSelectionValue(initialValue);
	jop.setWantsInput(true);
	dialog.setVisible(true);
	Object returnValue = jop.getInputValue();
	if ("uninitializedValue".equals(returnValue) && ! optionsList.contains("uninitializedValue")) {
	    return new MessageDialogResult(ReturnType.CANCEL);
	} else {
	    return new MessageDialogResult((String) returnValue);
	}
    }

    @Override
    public MessageDialogResult showOptionDialog(String msg, String title, OptionType optionType, DialogType msgType,
	    byte[] iconData, String... options) {
	if (options.length == 0) {
	    throw new IllegalArgumentException("List of options must be given.");
	}
	ImageIcon icon = new ImageIcon(iconData);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType), icon,
		options);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(frameIcon);
	dialog.setVisible(true);
	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageDialogResult(ReturnType.CANCEL);
	}
	return new MessageDialogResult((String) returnValue);
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
	}
	throw new IllegalArgumentException();
    }

}
