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
import javax.annotation.Nullable;
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
 * This implementation wraps the {@link JOptionPane} class from Swing.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class SwingMessageDialog implements MessageDialog {

    private static final Image FRAME_ICON = GUIDefaults.getImage("Frame.icon", 45, 45).getImage();

    @Override
    public MessageDialogResult showMessageDialog(String msg, String title) {
	return showMessageDialog(msg, title, DialogType.INFORMATION_MESSAGE);
    }

    @Override
    public MessageDialogResult showMessageDialog(String msg, String title, DialogType msgType) {
	return showMessageDialog(msg, title, msgType, null);
    }

    @Override
    public MessageDialogResult showMessageDialog(String msg, String title, DialogType msgType,
	    @Nullable byte[] iconData) {
	msg = formatMessage(msg);
	ImageIcon icon = iconData != null ? new ImageIcon(iconData) : null;
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.DEFAULT_OPTION, icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(FRAME_ICON);
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
	return showConfirmDialog(msg, title, optionType, msgType, null);
    }

    @Override
    public MessageDialogResult showConfirmDialog(String msg, String title, OptionType optionType, DialogType msgType,
	    @Nullable byte[] iconData) {
	msg = formatMessage(msg);
	ImageIcon icon = iconData != null ? new ImageIcon(iconData) : null;
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType), icon);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(FRAME_ICON);
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
	msg = formatMessage(msg);
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), JOptionPane.OK_CANCEL_OPTION);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(FRAME_ICON);
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
	msg = formatMessage(msg);
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
	dialog.setIconImage(FRAME_ICON);
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
	msg = formatMessage(msg);
	if (options.length == 0) {
	    throw new IllegalArgumentException("List of options must be given.");
	}
	ImageIcon icon = null;
	if (iconData != null) {
	    icon = new ImageIcon(iconData);
	}
	JOptionPane jop = new JOptionPane(msg, convertDialogType(msgType), convertOptionType(optionType), icon,
		options);
	JDialog dialog = jop.createDialog(title);
	dialog.setIconImage(FRAME_ICON);
	dialog.setVisible(true);
	Object returnValue = jop.getValue();
	if (returnValue == null) {
	    return new MessageDialogResult(ReturnType.CANCEL);
	}
	return new MessageDialogResult((String) returnValue);
    }


    private static int convertOptionType(OptionType optionType) {
	switch (optionType) {
	    case YES_NO_OPTION:        return JOptionPane.YES_NO_OPTION;
	    case YES_NO_CANCEL_OPTION: return JOptionPane.YES_NO_CANCEL_OPTION;
	    case OK_CANCEL_OPTION:     return JOptionPane.OK_CANCEL_OPTION;
	}
	throw new IllegalArgumentException();
    }

    private static int convertDialogType(DialogType dialogType) {
	switch (dialogType) {
	    case ERROR_MESSAGE:       return JOptionPane.ERROR_MESSAGE;
	    case INFORMATION_MESSAGE: return JOptionPane.INFORMATION_MESSAGE;
	    case WARNING_MESSAGE:     return JOptionPane.WARNING_MESSAGE;
	    case QUESTION_MESSAGE:    return JOptionPane.QUESTION_MESSAGE;
	    case PLAIN_MESSAGE:       return JOptionPane.PLAIN_MESSAGE;
	}
	throw new IllegalArgumentException();
    }

    private static ReturnType convertReturnType(int returnValue) {
	switch (returnValue) {
	    case JOptionPane.OK_OPTION:     return ReturnType.OK;
	    case JOptionPane.NO_OPTION:     return ReturnType.NO;
	    case JOptionPane.CANCEL_OPTION: return ReturnType.CANCEL;
	}
	throw new IllegalArgumentException();
    }

    private String formatMessage(String message) {
	StringBuilder builder = new StringBuilder();
	builder.append("<html><body style='width: 450px;'><p>");
	builder.append(message.replace("\n", "<br>"));
	builder.append("</p></body></html>");
	return builder.toString();
    }

}
